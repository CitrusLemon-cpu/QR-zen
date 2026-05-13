     1	package com.qrzen.app.ui.unlock
     2	
     3	import android.content.Context
     4	import android.content.Intent
     5	import android.os.Bundle
     6	import androidx.activity.result.contract.ActivityResultContracts
     7	import androidx.appcompat.app.AppCompatActivity
     8	import androidx.lifecycle.lifecycleScope
     9	import com.king.zxing.CameraScan
    10	import com.qrzen.app.data.db.AppBlockDao
    11	import com.qrzen.app.data.db.TimeBlockDao
    12	import com.qrzen.app.data.prefs.Prefs
    13	import com.qrzen.app.databinding.ActivityUnlockChallengeBinding
    14	import com.qrzen.app.ui.lock.QrScanActivity
    15	import dagger.hilt.android.AndroidEntryPoint
    16	import kotlinx.coroutines.launch
    17	import javax.inject.Inject
    18	
    19	@AndroidEntryPoint
    20	class UnlockChallengeActivity : AppCompatActivity() {
    21	
    22	    companion object {
    23	        const val EXTRA_BLOCK_ID = "extra_block_id"
    24	        const val EXTRA_ACTION = "extra_action"
    25	
    26	        const val ACTION_PAUSE = "PAUSE"
    27	        const val ACTION_EDIT = "EDIT"
    28	        const val ACTION_TOGGLE = "TOGGLE"
    29	        const val ACTION_DELETE = "DELETE"
    30	        const val ACTION_ARCHIVE = "ARCHIVE"
    31	        const val ACTION_MOVE_TO_FOLDER = "MOVE_TO_FOLDER"
    32	
    33	        fun createIntent(context: Context, blockId: Int, action: String): Intent {
    34	            return Intent(context, UnlockChallengeActivity::class.java).apply {
    35	                putExtra(EXTRA_BLOCK_ID, blockId)
    36	                putExtra(EXTRA_ACTION, action)
    37	            }
    38	        }
    39	    }
    40	
    41	    @Inject lateinit var dao: AppBlockDao
    42	    @Inject lateinit var timeBlockDao: TimeBlockDao
    43	
    44	    private lateinit var binding: ActivityUnlockChallengeBinding
    45	    private lateinit var renderer: UnlockChallengeRenderer
    46	
    47	    private val qrScanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    48	        renderer.handleQrScanResult(result.data?.getStringExtra(CameraScan.SCAN_RESULT))
    49	    }
    50	
    51	    override fun onCreate(savedInstanceState: Bundle?) {
    52	        super.onCreate(savedInstanceState)
    53	        if (Prefs.pauseAllUntil > System.currentTimeMillis()) {
    54	            setResult(RESULT_OK)
    55	            finish()
    56	            return
    57	        }
    58	        binding = ActivityUnlockChallengeBinding.inflate(layoutInflater)
    59	        setContentView(binding.root)
    60	        renderer = UnlockChallengeRenderer(this, binding.challengeContainer, binding.tvError)
    61	        loadBlock()
    62	    }
    63	
    64	    private fun loadBlock() {
    65	        val blockId = intent.getIntExtra(EXTRA_BLOCK_ID, -1)
    66	        lifecycleScope.launch {
    67	            val block = dao.getById(blockId) ?: run {
    68	                setResult(RESULT_CANCELED)
    69	                finish()
    70	                return@launch
    71	            }
    72	            val timeBlocks = timeBlockDao.getByBlockId(block.id)
    73	            binding.tvBlockTitle.text = block.title
    74	            renderer.render(
    75	                block = block,
    76	                timeBlocks = timeBlocks,
    77	                showGoBackButton = true,
    78	                onRequestQrScan = {
    79	                    qrScanLauncher.launch(Intent(this@UnlockChallengeActivity, QrScanActivity::class.java))
    80	                },
    81	                onUnlocked = {
    82	                    setResult(RESULT_OK)
    83	                    finish()
    84	                },
    85	                onGoBack = {
    86	                    setResult(RESULT_CANCELED)
    87	                    finish()
    88	                }
    89	            )
    90	        }
    91	    }
    92	
    93	    override fun onDestroy() {
    94	        if (::renderer.isInitialized) renderer.clear()
    95	        super.onDestroy()
    96	    }
    97	}
    98	