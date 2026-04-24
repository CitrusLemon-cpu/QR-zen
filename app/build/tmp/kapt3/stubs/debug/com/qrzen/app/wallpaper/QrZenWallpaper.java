package com.qrzen.app.wallpaper;

/**
 * Stub wallpaper service. Having this registered keeps the process
 * alive in some OEM battery-saver implementations (e.g. MIUI, ColorOS)
 * that exempt wallpaper services from aggressive process killing.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\f\u0010\u0003\u001a\u00060\u0004R\u00020\u0001H\u0016\u00a8\u0006\u0005"}, d2 = {"Lcom/qrzen/app/wallpaper/QrZenWallpaper;", "Landroid/service/wallpaper/WallpaperService;", "()V", "onCreateEngine", "Landroid/service/wallpaper/WallpaperService$Engine;", "app_debug"})
public final class QrZenWallpaper extends android.service.wallpaper.WallpaperService {
    
    public QrZenWallpaper() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.service.wallpaper.WallpaperService.Engine onCreateEngine() {
        return null;
    }
}