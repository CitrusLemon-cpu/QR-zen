package com.qrzen.app.ui.lock;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0016\u00a8\u0006\u0007"}, d2 = {"Lcom/qrzen/app/ui/lock/QrScanActivity;", "Lcom/king/zxing/CaptureActivity;", "()V", "onScanResultCallback", "", "result", "Lcom/google/zxing/Result;", "app_debug"})
public final class QrScanActivity extends com.king.zxing.CaptureActivity {
    
    public QrScanActivity() {
        super();
    }
    
    @java.lang.Override()
    public boolean onScanResultCallback(@org.jetbrains.annotations.NotNull()
    com.google.zxing.Result result) {
        return false;
    }
}