package com.qrzen.app.data.backup

import com.qrzen.app.data.db.AppBlockDao
import com.qrzen.app.data.db.BlockFolderDao
import com.qrzen.app.data.db.TimeBlockDao
import com.qrzen.app.data.model.AppBlock
import com.qrzen.app.data.model.BlockFolder
import com.qrzen.app.data.model.TimeBlock
import org.json.JSONObject
import java.util.UUID

data class ImportResult(
    val foldersImported: Int,
    val blocksImported: Int
)

class BlockBackupManager(
    private val appBlockDao: AppBlockDao,
    private val blockFolderDao: BlockFolderDao,
    private val timeBlockDao: TimeBlockDao
) {
    suspend fun export(): String {
        val folders = blockFolderDao.getAll()
        val blocks = appBlockDao.getAllIncludingArchived()
        val timeBlocksByBlockId = timeBlockDao.getAll().groupBy { it.blockId }

        val backup = BlockBackup(
            exportedAt = System.currentTimeMillis(),
            folders = folders.map { folder ->
                FolderBackup(
                    title = folder.title,
                    isEnabled = folder.isEnabled,
                    isCollapsed = folder.isCollapsed,
                    sortOrder = folder.sortOrder,
                    unlockMethod = sanitizeUnlockMethod(folder.unlockMethod),
                    delayMinutes = folder.delayMinutes,
                    editWindowStart = folder.editWindowStart,
                    editWindowEnd = folder.editWindowEnd,
                    editWindowDays = folder.editWindowDays,
                    masterPasswordEnabled = folder.masterPasswordEnabled,
                    backupId = folder.id
                )
            },
            blocks = blocks.map { block ->
                BlockBackupEntry(
                    title = block.title,
                    appPackages = block.appPackages,
                    isAllowlistMode = block.isAllowlistMode,
                    startTime = block.startTime,
                    endTime = block.endTime,
                    activeDays = block.activeDays,
                    unlockMethod = sanitizeUnlockMethod(block.unlockMethod),
                    delayMinutes = block.delayMinutes,
                    editWindowStart = block.editWindowStart,
                    editWindowEnd = block.editWindowEnd,
                    editWindowDays = block.editWindowDays,
                    masterPasswordEnabled = block.masterPasswordEnabled,
                    isEnabled = block.isEnabled,
                    isArchived = block.isArchived,
                    blockingStyle = block.blockingStyle,
                    scheduleBreakType = block.scheduleBreakType,
                    scheduledAllowanceMinutes = block.scheduledAllowanceMinutes,
                    usageLimitMinutes = block.usageLimitMinutes,
                    usageLimitPeriod = block.usageLimitPeriod,
                    waitTimerWaitMinutes = block.waitTimerWaitMinutes,
                    waitTimerUseMinutes = block.waitTimerUseMinutes,
                    waitTimerAdaptive = block.waitTimerAdaptive,
                    pomodoroDurationMin = block.pomodoroDurationMin,
                    pomodoroBreakMin = block.pomodoroBreakMin,
                    isPomodoroBlock = block.isPomodoroBlock,
                    timerBreakMinutes = block.timerBreakMinutes,
                    showTimer = block.showTimer,
                    autoAddNewApps = block.autoAddNewApps,
                    blockAudio = block.blockAudio,
                    pomodoroLockEditing = block.pomodoroLockEditing,
                    typeOverIsRandom = block.typeOverIsRandom,
                    timeBlocks = timeBlocksByBlockId[block.id].orEmpty().map { timeBlock ->
                        TimeBlockBackup(
                            startTime = timeBlock.startTime,
                            endTime = timeBlock.endTime,
                            activeDays = timeBlock.activeDays
                        )
                    },
                    folderBackupId = block.folderId
                )
            }
        )

        return backup.toJson().toString(2)
    }

    suspend fun import(json: String): ImportResult {
        val backup = BlockBackup.fromJson(JSONObject(json))
        require(backup.version == CURRENT_VERSION) {
            "Unsupported backup version: ${backup.version}"
        }

        val folderIdMap = mutableMapOf<Int, Int>()
        backup.folders.forEach { folder ->
            val newFolderId = blockFolderDao.insert(
                BlockFolder(
                    title = folder.title,
                    isEnabled = folder.isEnabled,
                    pausedUntil = 0L,
                    isCollapsed = folder.isCollapsed,
                    sortOrder = folder.sortOrder,
                    unlockMethod = sanitizeUnlockMethod(folder.unlockMethod),
                    delayMinutes = folder.delayMinutes,
                    blockPassword = "",
                    typeOverText = "",
                    typeOverIsRandom = false,
                    editWindowStart = folder.editWindowStart,
                    editWindowEnd = folder.editWindowEnd,
                    editWindowDays = folder.editWindowDays,
                    lockUntil = 0L,
                    qrSecret = UUID.randomUUID().toString(),
                    masterPasswordEnabled = folder.masterPasswordEnabled
                )
            ).toInt()
            folderIdMap[folder.backupId] = newFolderId
        }

        var importedBlocks = 0
        backup.blocks.forEach { block ->
            val newBlockId = appBlockDao.insert(
                AppBlock(
                    title = block.title,
                    appPackages = block.appPackages,
                    isAllowlistMode = block.isAllowlistMode,
                    startTime = block.startTime,
                    endTime = block.endTime,
                    activeDays = block.activeDays,
                    qrSecret = UUID.randomUUID().toString(),
                    unlockMethod = sanitizeUnlockMethod(block.unlockMethod),
                    delayMinutes = block.delayMinutes,
                    blockPassword = "",
                    typeOverText = "",
                    typeOverIsRandom = block.typeOverIsRandom,
                    editWindowStart = block.editWindowStart,
                    editWindowEnd = block.editWindowEnd,
                    editWindowDays = block.editWindowDays,
                    lockUntil = 0L,
                    masterPasswordEnabled = block.masterPasswordEnabled,
                    pausedUntil = 0L,
                    blockNowUntil = 0L,
                    isEnabled = block.isEnabled,
                    isPomodoroBlock = block.isPomodoroBlock,
                    pomodoroDurationMin = block.pomodoroDurationMin,
                    pomodoroBreakMin = block.pomodoroBreakMin,
                    isArchived = block.isArchived,
                    blockingStyle = block.blockingStyle,
                    scheduleBreakType = block.scheduleBreakType,
                    scheduledAllowanceMinutes = block.scheduledAllowanceMinutes,
                    usageLimitMinutes = block.usageLimitMinutes,
                    usageLimitPeriod = block.usageLimitPeriod,
                    waitTimerWaitMinutes = block.waitTimerWaitMinutes,
                    waitTimerUseMinutes = block.waitTimerUseMinutes,
                    waitTimerAdaptive = block.waitTimerAdaptive,
                    timerBreakMinutes = block.timerBreakMinutes,
                    showTimer = block.showTimer,
                    toggleLockUntil = 0L,
                    autoDisableOnToggleLockExpiry = false,
                    activeUntil = 0L,
                    pomodoroRoundsTotal = 0,
                    pomodoroSessionStartMillis = 0L,
                    pomodoroLockEditing = block.pomodoroLockEditing,
                    autoAddNewApps = block.autoAddNewApps,
                    blockAudio = block.blockAudio,
                    folderId = block.folderBackupId?.let(folderIdMap::get)
                )
            ).toInt()

            if (block.timeBlocks.isNotEmpty()) {
                timeBlockDao.insertAll(
                    block.timeBlocks.map { timeBlock ->
                        TimeBlock(
                            blockId = newBlockId,
                            startTime = timeBlock.startTime,
                            endTime = timeBlock.endTime,
                            activeDays = timeBlock.activeDays
                        )
                    }
                )
            }

            importedBlocks += 1
        }

        return ImportResult(
            foldersImported = backup.folders.size,
            blocksImported = importedBlocks
        )
    }

    private fun sanitizeUnlockMethod(unlockMethod: String): String {
        return if (unlockMethod in secretBackedUnlockMethods) METHOD_NONE else unlockMethod
    }

    companion object {
        private const val CURRENT_VERSION = 1
        private const val METHOD_NONE = "NONE"
        private val secretBackedUnlockMethods = setOf("PASSWORD", "TYPE_OVER_TEXT", "QR_CODE")
    }
}
