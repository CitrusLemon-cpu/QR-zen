package com.qrzen.app.ui.unlock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.king.zxing.CameraScan
import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.db.BlockFolderDao
import com.qrzen.app.data.db.TimeBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockFolder
import com.qrzen.app.data.prefs.Prefs
import com.qrzen.app.databinding.ActivityUnlockChallengeBinding
import com.qrzen.app.ui.lock.QrScanActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UnlockChallengeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCK_ID = "extra_block_id"
        const val EXTRA_FOLDER_ID = "extra_folder_id"
        const val EXTRA_ACTION = "extra_action"

        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_EDIT = "EDIT"
        const val ACTION_TOGGLE = "TOGGLE"
        const val ACTION_DELETE = "DELETE"
        const val ACTION_ARCHIVE = "ARCHIVE"
        const val ACTION_MOVE_TO_FOLDER = "MOVE_TO_FOLDER"
        const val ACTION_FOLDER_PAUSE = "FOLDER_PAUSE"
        const val ACTION_FOLDER_EDIT = "FOLDER_EDIT"
        const val ACTION_FOLDER_TOGGLE = "FOLDER_TOGGLE"
        const val ACTION_FOLDER_DELETE = "FOLDER_DELETE"

        fun createIntent(context: Context, blockId: Int, action: String): Intent {
            return Intent(context, UnlockChallengeActivity::class.java).apply {
                putExtra(EXTRA_BLOCK_ID, blockId)
                putExtra(EXTRA_ACTION, action)
            }
        }

        fun createFolderIntent(context: Context, folderId: Int, action: String): Intent {
            return Intent(context, UnlockChallengeActivity::class.java).apply {
                putExtra(EXTRA_FOLDER_ID, folderId)
                putExtra(EXTRA_ACTION, action)
            }
        }
    }

    @Inject lateinit var dao: AppBlockDao
    @Inject lateinit var blockFolderDao: BlockFolderDao
    @Inject lateinit var timeBlockDao: TimeBlockDao

    private lateinit var binding: ActivityUnlockChallengeBinding
    private lateinit var renderer: UnlockChallengeRenderer

    private val qrScanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        renderer.handleQrScanResult(result.data?.getStringExtra(CameraScan.SCAN_RESULT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Prefs.pauseAllUntil > System.currentTimeMillis()) {
            setResult(RESULT_OK)
            finish()
            return
        }
        binding = ActivityUnlockChallengeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        renderer = UnlockChallengeRenderer(this, binding.challengeContainer, binding.tvError)
        loadChallenge()
    }

    private fun loadChallenge() {
        val folderId = intent.getIntExtra(EXTRA_FOLDER_ID, -1)
        if (folderId != -1) {
            loadFolder(folderId)
            return
        }

        val blockId = intent.getIntExtra(EXTRA_BLOCK_ID, -1)
        lifecycleScope.launch {
            val block = dao.getById(blockId) ?: run {
                setResult(RESULT_CANCELED)
                finish()
                return@launch
            }
            val timeBlocks = timeBlockDao.getByBlockId(block.id)
            binding.tvBlockTitle.text = block.title
            renderer.render(
                block = block,
                timeBlocks = timeBlocks,
                showGoBackButton = true,
                onRequestQrScan = {
                    qrScanLauncher.launch(Intent(this@UnlockChallengeActivity, QrScanActivity::class.java))
                },
                onUnlocked = {
                    setResult(RESULT_OK)
                    finish()
                },
                onGoBack = {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            )
        }
    }

    private fun loadFolder(folderId: Int) {
        lifecycleScope.launch {
            val folder = blockFolderDao.getById(folderId) ?: run {
                setResult(RESULT_CANCELED)
                finish()
                return@launch
            }
            binding.tvBlockTitle.text = folder.title
            renderer.render(
                block = folder.asUnlockChallengeBlock(),
                timeBlocks = emptyList(),
                showGoBackButton = true,
                onRequestQrScan = {
                    qrScanLauncher.launch(Intent(this@UnlockChallengeActivity, QrScanActivity::class.java))
                },
                onUnlocked = {
                    setResult(RESULT_OK)
                    finish()
                },
                onGoBack = {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            )
        }
    }

    private fun BlockFolder.asUnlockChallengeBlock(): AppBlock {
        return AppBlock(
            title = title,
            unlockMethod = unlockMethod,
            delayMinutes = delayMinutes,
            blockPassword = blockPassword,
            typeOverText = typeOverText,
            typeOverIsRandom = typeOverIsRandom,
            editWindowStart = editWindowStart,
            editWindowEnd = editWindowEnd,
            editWindowDays = editWindowDays,
            lockUntil = lockUntil,
            qrSecret = qrSecret
        )
    }

    override fun onDestroy() {
        if (::renderer.isInitialized) renderer.clear()
        super.onDestroy()
    }
}
