package com.qrzen.app.data.backup

import org.json.JSONArray
import org.json.JSONObject

data class BlockBackup(
    val version: Int = 1,
    val exportedAt: Long,
    val folders: List<FolderBackup>,
    val blocks: List<BlockBackupEntry>
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("version", version)
        put("exportedAt", exportedAt)
        put("folders", JSONArray().apply {
            folders.forEach { put(it.toJson()) }
        })
        put("blocks", JSONArray().apply {
            blocks.forEach { put(it.toJson()) }
        })
    }

    companion object {
        fun fromJson(obj: JSONObject): BlockBackup {
            val foldersArray = obj.optJSONArray("folders") ?: JSONArray()
            val blocksArray = obj.optJSONArray("blocks") ?: JSONArray()
            return BlockBackup(
                version = obj.getInt("version"),
                exportedAt = obj.getLong("exportedAt"),
                folders = List(foldersArray.length()) { index ->
                    FolderBackup.fromJson(foldersArray.getJSONObject(index))
                },
                blocks = List(blocksArray.length()) { index ->
                    BlockBackupEntry.fromJson(blocksArray.getJSONObject(index))
                }
            )
        }
    }
}

data class FolderBackup(
    val title: String,
    val isEnabled: Boolean,
    val isCollapsed: Boolean,
    val sortOrder: Int,
    val unlockMethod: String,
    val delayMinutes: Int,
    val editWindowStart: String,
    val editWindowEnd: String,
    val editWindowDays: String,
    val masterPasswordEnabled: Boolean,
    val backupId: Int
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("title", title)
        put("isEnabled", isEnabled)
        put("isCollapsed", isCollapsed)
        put("sortOrder", sortOrder)
        put("unlockMethod", unlockMethod)
        put("delayMinutes", delayMinutes)
        put("editWindowStart", editWindowStart)
        put("editWindowEnd", editWindowEnd)
        put("editWindowDays", editWindowDays)
        put("masterPasswordEnabled", masterPasswordEnabled)
        put("backupId", backupId)
    }

    companion object {
        fun fromJson(obj: JSONObject): FolderBackup = FolderBackup(
            title = obj.getString("title"),
            isEnabled = obj.getBoolean("isEnabled"),
            isCollapsed = obj.getBoolean("isCollapsed"),
            sortOrder = obj.getInt("sortOrder"),
            unlockMethod = obj.getString("unlockMethod"),
            delayMinutes = obj.getInt("delayMinutes"),
            editWindowStart = obj.getString("editWindowStart"),
            editWindowEnd = obj.getString("editWindowEnd"),
            editWindowDays = obj.getString("editWindowDays"),
            masterPasswordEnabled = obj.getBoolean("masterPasswordEnabled"),
            backupId = obj.getInt("backupId")
        )
    }
}

data class BlockBackupEntry(
    val title: String,
    val appPackages: String,
    val isAllowlistMode: Boolean,
    val startTime: String,
    val endTime: String,
    val activeDays: String,
    val unlockMethod: String,
    val delayMinutes: Int,
    val editWindowStart: String,
    val editWindowEnd: String,
    val editWindowDays: String,
    val masterPasswordEnabled: Boolean,
    val isEnabled: Boolean,
    val isArchived: Boolean,
    val blockingStyle: String,
    val scheduleBreakType: String,
    val scheduledAllowanceMinutes: Int,
    val usageLimitMinutes: Int,
    val usageLimitPeriod: String,
    val waitTimerWaitMinutes: Int,
    val waitTimerUseMinutes: Int,
    val waitTimerAdaptive: Boolean,
    val pomodoroDurationMin: Int,
    val pomodoroBreakMin: Int,
    val isPomodoroBlock: Boolean,
    val timerBreakMinutes: Int,
    val showTimer: Boolean,
    val autoAddNewApps: Boolean,
    val blockAudio: Boolean,
    val pomodoroLockEditing: Boolean,
    val typeOverIsRandom: Boolean,
    val timeBlocks: List<TimeBlockBackup>,
    val folderBackupId: Int?
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("title", title)
        put("appPackages", appPackages)
        put("isAllowlistMode", isAllowlistMode)
        put("startTime", startTime)
        put("endTime", endTime)
        put("activeDays", activeDays)
        put("unlockMethod", unlockMethod)
        put("delayMinutes", delayMinutes)
        put("editWindowStart", editWindowStart)
        put("editWindowEnd", editWindowEnd)
        put("editWindowDays", editWindowDays)
        put("masterPasswordEnabled", masterPasswordEnabled)
        put("isEnabled", isEnabled)
        put("isArchived", isArchived)
        put("blockingStyle", blockingStyle)
        put("scheduleBreakType", scheduleBreakType)
        put("scheduledAllowanceMinutes", scheduledAllowanceMinutes)
        put("usageLimitMinutes", usageLimitMinutes)
        put("usageLimitPeriod", usageLimitPeriod)
        put("waitTimerWaitMinutes", waitTimerWaitMinutes)
        put("waitTimerUseMinutes", waitTimerUseMinutes)
        put("waitTimerAdaptive", waitTimerAdaptive)
        put("pomodoroDurationMin", pomodoroDurationMin)
        put("pomodoroBreakMin", pomodoroBreakMin)
        put("isPomodoroBlock", isPomodoroBlock)
        put("timerBreakMinutes", timerBreakMinutes)
        put("showTimer", showTimer)
        put("autoAddNewApps", autoAddNewApps)
        put("blockAudio", blockAudio)
        put("pomodoroLockEditing", pomodoroLockEditing)
        put("typeOverIsRandom", typeOverIsRandom)
        put("timeBlocks", JSONArray().apply {
            timeBlocks.forEach { put(it.toJson()) }
        })
        put("folderBackupId", folderBackupId)
    }

    companion object {
        fun fromJson(obj: JSONObject): BlockBackupEntry {
            val timeBlocksArray = obj.optJSONArray("timeBlocks") ?: JSONArray()
            return BlockBackupEntry(
                title = obj.getString("title"),
                appPackages = obj.getString("appPackages"),
                isAllowlistMode = obj.getBoolean("isAllowlistMode"),
                startTime = obj.getString("startTime"),
                endTime = obj.getString("endTime"),
                activeDays = obj.getString("activeDays"),
                unlockMethod = obj.getString("unlockMethod"),
                delayMinutes = obj.getInt("delayMinutes"),
                editWindowStart = obj.getString("editWindowStart"),
                editWindowEnd = obj.getString("editWindowEnd"),
                editWindowDays = obj.getString("editWindowDays"),
                masterPasswordEnabled = obj.getBoolean("masterPasswordEnabled"),
                isEnabled = obj.getBoolean("isEnabled"),
                isArchived = obj.getBoolean("isArchived"),
                blockingStyle = obj.getString("blockingStyle"),
                scheduleBreakType = obj.getString("scheduleBreakType"),
                scheduledAllowanceMinutes = obj.getInt("scheduledAllowanceMinutes"),
                usageLimitMinutes = obj.getInt("usageLimitMinutes"),
                usageLimitPeriod = obj.getString("usageLimitPeriod"),
                waitTimerWaitMinutes = obj.getInt("waitTimerWaitMinutes"),
                waitTimerUseMinutes = obj.getInt("waitTimerUseMinutes"),
                waitTimerAdaptive = obj.getBoolean("waitTimerAdaptive"),
                pomodoroDurationMin = obj.getInt("pomodoroDurationMin"),
                pomodoroBreakMin = obj.getInt("pomodoroBreakMin"),
                isPomodoroBlock = obj.getBoolean("isPomodoroBlock"),
                timerBreakMinutes = obj.getInt("timerBreakMinutes"),
                showTimer = obj.getBoolean("showTimer"),
                autoAddNewApps = obj.getBoolean("autoAddNewApps"),
                blockAudio = obj.getBoolean("blockAudio"),
                pomodoroLockEditing = obj.getBoolean("pomodoroLockEditing"),
                typeOverIsRandom = obj.getBoolean("typeOverIsRandom"),
                timeBlocks = List(timeBlocksArray.length()) { index ->
                    TimeBlockBackup.fromJson(timeBlocksArray.getJSONObject(index))
                },
                folderBackupId = if (obj.isNull("folderBackupId")) null else obj.getInt("folderBackupId")
            )
        }
    }
}

data class TimeBlockBackup(
    val startTime: String,
    val endTime: String,
    val activeDays: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("startTime", startTime)
        put("endTime", endTime)
        put("activeDays", activeDays)
    }

    companion object {
        fun fromJson(obj: JSONObject): TimeBlockBackup = TimeBlockBackup(
            startTime = obj.getString("startTime"),
            endTime = obj.getString("endTime"),
            activeDays = obj.getString("activeDays")
        )
    }
}
