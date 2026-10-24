package com.qrzen.app.ui.unlock

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText

/** Text input that allows editing but rejects clipboard paste actions. */
class NoPasteTextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.editTextStyle
) : TextInputEditText(context, attrs, defStyleAttr) {

    override fun onTextContextMenuItem(id: Int): Boolean = when (id) {
        android.R.id.paste,
        android.R.id.pasteAsPlainText -> false
        else -> super.onTextContextMenuItem(id)
    }
}
