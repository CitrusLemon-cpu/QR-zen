package com.qrzen.app.wallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

/**
 * Stub wallpaper service. Having this registered keeps the process
 * alive in some OEM battery-saver implementations (e.g. MIUI, ColorOS)
 * that exempt wallpaper services from aggressive process killing.
 */
class QrZenWallpaper : WallpaperService() {
    override fun onCreateEngine(): Engine = object : Engine() {
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }
}
