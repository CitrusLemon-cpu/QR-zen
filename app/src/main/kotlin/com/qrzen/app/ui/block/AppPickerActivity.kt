package com.qrzen.app.ui.block

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.qrzen.app.R
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.databinding.ActivityAppPickerBinding
import com.qrzen.app.databinding.ItemAppPickerBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AppPickerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRESELECTED = "extra_preselected"
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_IS_ALLOWLIST = "extra_is_allowlist"
        const val REQ_CODE = 2001
    }

    private lateinit var binding: ActivityAppPickerBinding
    private lateinit var adapter: AppPickerAdapter
    private var allApps: List<AppItem> = emptyList()

    @Inject
    lateinit var dao: AppBlockDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (intent.getBooleanExtra(EXTRA_IS_ALLOWLIST, false)) {
            getString(R.string.edit_block_select_allowed_apps)
        } else {
            getString(R.string.app_picker_title)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        val preselected = intent.getStringExtra(EXTRA_PRESELECTED)
            ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?.toSet() ?: emptySet()

        adapter = AppPickerAdapter(preselected.toMutableSet())
        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString().orEmpty())
            }
        })
        binding.btnImportFromBlock.setOnClickListener { showImportFromBlockDialog() }

        binding.btnDone.setOnClickListener {
            val result = adapter.getSelectedPackages().joinToString(",")
            setResult(RESULT_OK, Intent().putExtra(EXTRA_RESULT, result))
            finish()
        }

        loadApps()
    }

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val pm = packageManager
                val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                pm.queryIntentActivities(launcherIntent, 0)
                    .mapNotNull { resolveInfo ->
                        val ai = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null
                        val pkg = ai.packageName
                        if (pkg == packageName) return@mapNotNull null
                        AppItem(
                            packageName = pkg,
                            label = resolveInfo.loadLabel(pm).toString(),
                            icon = resolveInfo.loadIcon(pm)
                        )
                    }
                    .distinctBy { it.packageName }
                    .sortedBy { it.label.lowercase() }
            }
            allApps = apps
            filterApps(binding.etSearch.text?.toString().orEmpty())
        }
    }

    private fun filterApps(query: String) {
        if (query.isBlank()) {
            adapter.submitList(allApps)
            return
        }
        val lower = query.lowercase()
        adapter.submitList(
            allApps.filter {
                it.label.lowercase().contains(lower) || it.packageName.lowercase().contains(lower)
            }
        )
    }

    private fun showImportFromBlockDialog() {
        lifecycleScope.launch {
            val blocks = withContext(Dispatchers.IO) {
                dao.getAll().filter { it.appPackages.isNotBlank() }
            }
            if (blocks.isEmpty()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.app_picker_no_blocks),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@launch
            }
            val blockNames = blocks.map { it.title.ifBlank { "Untitled" } }.toTypedArray()
            val checkedItems = BooleanArray(blocks.size) { false }
            AlertDialog.Builder(this@AppPickerActivity)
                .setTitle(getString(R.string.app_picker_import_title))
                .setMultiChoiceItems(blockNames, checkedItems) { _, which, isChecked ->
                    checkedItems[which] = isChecked
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val packagesToImport = mutableSetOf<String>()
                    checkedItems.forEachIndexed { index, checked ->
                        if (checked) {
                            blocks[index].appPackages.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .forEach { packagesToImport.add(it) }
                        }
                    }
                    if (packagesToImport.isNotEmpty()) {
                        adapter.addPackages(packagesToImport)
                        adapter.notifyDataSetChanged()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    data class AppItem(
        val packageName: String,
        val label: String,
        val icon: android.graphics.drawable.Drawable
    )

    class AppPickerAdapter(
        private val selected: MutableSet<String>
    ) : ListAdapter<AppItem, AppPickerAdapter.ViewHolder>(DIFF) {

        companion object {
            val DIFF = object : DiffUtil.ItemCallback<AppItem>() {
                override fun areItemsTheSame(a: AppItem, b: AppItem) = a.packageName == b.packageName
                override fun areContentsTheSame(a: AppItem, b: AppItem) = a == b
            }
        }

        inner class ViewHolder(val binding: ItemAppPickerBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(item: AppItem) {
                binding.ivAppIcon.setImageDrawable(item.icon)
                binding.tvAppName.text = item.label
                binding.tvPackageName.text = item.packageName
                binding.cbSelected.isChecked = selected.contains(item.packageName)
                binding.root.setOnClickListener {
                    if (selected.contains(item.packageName)) {
                        selected.remove(item.packageName)
                    } else {
                        selected.add(item.packageName)
                    }
                    binding.cbSelected.isChecked = selected.contains(item.packageName)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(ItemAppPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(getItem(position))

        fun getSelectedPackages(): Set<String> = selected

        fun addPackages(packages: Set<String>) {
            selected.addAll(packages)
        }
    }
}
