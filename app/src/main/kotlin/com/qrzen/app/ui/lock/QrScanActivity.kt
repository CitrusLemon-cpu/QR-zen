package com.qrzen.app.ui.lock

import android.app.Activity
import android.content.Intent
import com.google.zxing.Result
import com.king.zxing.CameraScan
import com.king.zxing.CaptureActivity

class QrScanActivity : CaptureActivity() {

    override fun onScanResultCallback(result: Result): Boolean {
        val intent = Intent()
        intent.putExtra(CameraScan.SCAN_RESULT, result.text)
        setResult(Activity.RESULT_OK, intent)
        finish()
        return true
    }
}
