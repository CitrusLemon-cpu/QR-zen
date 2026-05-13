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
    11	import com.qrzen.app.data.db.BlockFolderDao
    12	import com.qrzen.app.data.db.TimeBlockDao
    13	import com.qrzen.app.data.model.AppBlock
    14	import com.qrzen.app.data.model.BlockFolder
    15	import com.qrzen.app.data.prefs.Prefs
    16	import com.qrzen.app.databinding.ActivityUnlockChallengeBinding
    17	import com.qrzen.app.ui.lock.QrScanActivity
    18	import dagger.hilt.android.AndroidEntryPoint
    19	import kotlinx.coroutines.launch
    20	import javax.inject.Inject
    21	
    22	@AndroidEntryPoint
    23	class UnlockChallengeActivity : AppCompatActivity() {
    24	
    25	    companion object {
    26	        const val EXTRA_BLOCK_ID = "extra_block_id"
    27	        const val EXTRA_FOLDER_ID = "extra_folder_id"
    28	        const val EXTRA_ACTION = "extra_action"
    29	
    30	        const val ACTION_PAUSE = "PAUSE"
    31	        const val ACTION_EDIT = "EDIT"
    32	        const val ACTION_TOGGLE = "TOGGLE"
    33	        const val ACTION_DELETE = "DELETE"
    34	        const val ACTION_ARCHIVE = "ARCHIVE"
    35	        const val ACTION_MOVE_TO_FOLDER = "MOVE_TO_FOLDER"
    36	        const val ACTION_FOLDER_PAUSE = "FOLDER_PAUSE"
    37	        const val ACTION_FOLDER_EDIT = "FOLDER_EDIT"
    38	        const val ACTION_FOLDER_TOGGLE = "FOLDER_TOGGLE"
    39	        const val ACTION_FOLDER_DELETE = "FOLDER_DELETE"
    40	
    41	        fun createIntent(context: Context, blockId: Int, action: String): Intent {
    42	            return Intent(context, UnlockChallengeActivity::class.java).apply {
    43	                putExtra(EXTRA_BLOCK_ID, blockId)
    44	                putExtra(EXTRA_ACTION, action)
    45	            }
    46	        }
    47	
    48	        fun createFolderIntent(context: Context, folderId: Int, action: String): Intent {
    49	            return Intent(context, UnlockChallengeActivity::class.java).apply {
    50	                putExtra(EXTRA_FOLDER_ID, folderId)
    51	                putExtra(EXTRA_ACTION, action)
    52	            }
    53	        }
    54	    }
    55	
    56	    @Inject lateinit var dao: AppBlockDao
    57	    @Inject lateinit var blockFolderDao: BlockFolderDao
    58	    @Inject lateinit var timeBlockDao: TimeBlockDao
    59	
    60	    private lateinit var binding: ActivityUnlockChallengeBinding
    61	    private lateinit var renderer: UnlockChallengeRenderer
    62	
    63	    private val qrScanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    64	        renderer.handleQrScanResult(result.data?.getStringExtra(CameraScan.SCAN_RESULT))
    65	    }
    66	
    67	    override fun onCreate(savedInstanceState: Bundle?) {
    68	        super.onCreate(savedInstanceState)
    69	        if (Prefs.pauseAllUntil > System.currentTimeMillis()) {
    70	            setResult(RESULT_OK)
    71	            finish()
    72	            return
    73	        }
    74	        binding = ActivityUnlockChallengeBinding.inflate(layoutInflater)
    75	        setContentView(binding.root)
    76	        renderer = UnlockChallengeRenderer(this, binding.challengeContainer, binding.tvError)
    77	        loadChallenge()
    78	    }
    79	
    80	    private fun loadChallenge() {
    81	        val folderId = intent.getIntExtra(EXTRA_FOLDER_ID, -1)
    82	        if (folderId != -1) {
    83	            loadFolder(folderId)
    84	            return
    85	        }
    86	
    87	        val blockId = intent.getIntExtra(EXTRA_BLOCK_ID, -1)
    88	        lifecycleScope.launch {
    89	            val block = dao.getById(blockId) ?: run {
    90	                setResult(RESULT_CANCELED)
    91	                finish()
    92	                return@launch
    93	            }
    94	            val timeBlocks = timeBlockDao.getByBlockId(block.id)
    95	            binding.tvBlockTitle.text = block.title
    96	            renderer.render(
    97	                block = block,
    98	                timeBlocks = timeBlocks,
    99	                showGoBackButton = true,
   100	                onRequestQrScan = {
   101	                    qrScanLauncher.launch(Intent(this@UnlockChallengeActivity, QrScanActivity::class.java))
   102	                },
   103	                onUnlocked = {
   104	                    setResult(RESULT_OK)
   105	                    finish()
   106	                },
   107	                onGoBack = {
   108	                    setResult(RESULT_CANCELED)
   109	                    finish()
   110	                }
   111	            )
   112	        }
   113	    }
   114	
   115	    private fun loadFolder(folderId: Int) {
   116	        lifecycleScope.launch {
   117	            val folder = blockFolderDao.getById(folderId) ?: run {
   118	                setResult(RESULT_CANCELED)
   119	                finish()
   120	                return@launch
   121	            }
   122	            binding.tvBlockTitle.text = folder.title
   123	            renderer.render(
   124	                block = folder.asUnlockChallengeBlock(),
   125	                timeBlocks = emptyList(),
   126	                showGoBackButton = true,
   127	                onRequestQrScan = {
   128	                    qrScanLauncher.launch(Intent(this@UnlockChallengeActivity, QrScanActivity::class.java))
   129	                },
   130	                onUnlocked = {
   131	                    setResult(RESULT_OK)
   132	                    finish()
   133	                },
   134	                onGoBack = {
   135	                    setResult(RESULT_CANCELED)
   136	                    finish()
   137	                }
   138	            )
   139	        }
   140	    }
   141	
   142	    private fun BlockFolder.asUnlockChallengeBlock(): AppBlock {
   143	        return AppBlock(
   144	            title = title,
   145	            unlockMethod = unlockMethod,
   146	            delayMinutes = delayMinutes,
   147	            blockPassword = blockPassword,
   148	            typeOverText = typeOverText,
   149	            typeOverIsRandom = typeOverIsRandom,
   150	            editWindowStart = editWindowStart,
   151	            editWindowEnd = editWindowEnd,
   152	            editWindowDays = editWindowDays,
   153	            lockUntil = lockUntil,
   154	            qrSecret = qrSecret
   155	        )
   156	    }
   157	
   158	    override fun onDestroy() {
   159	        if (::renderer.isInitialized) renderer.clear()
   160	        super.onDestroy()
   161	    }
   162	}
   163	