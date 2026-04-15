package com.qrzen.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.qrzen.app.data.prefs.Prefs

class QrZenNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (Prefs.removeNotifications) {
            sbn?.let { cancelNotification(it.key) }
        }
    }
}
