package sg.com.agentapp.global

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.emoji.widget.EmojiEditText
import sg.com.agentapp.global.emoji.EmojiPopup

class EmojiHelper {
    private lateinit var popup: EmojiPopup

    fun emojiBtnOnClick(context: Context, msgInput: EmojiEditText) {
        if (!popup.isShowing) {
            if (popup.isKeyBoardOpen()!!) {
                popup.showAtBottom()
            } else {
                msgInput.isFocusableInTouchMode = true
                msgInput.requestFocus()
                popup.showAtBottomPending()
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(msgInput, InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            popup.dismiss()
        }
    }


    fun setupEmojiFunc(view: View, msgInput: EmojiEditText) {
        //setup emojipopup
        popup = EmojiPopup(view, view.context)

        // onclick interface function
        val onItemClickInterface: EmojiPopup.OnSoftKeyboardOpenCloseListener = object : EmojiPopup.OnSoftKeyboardOpenCloseListener {
            override fun onKeyboardOpen(keyBoardHeight: Int) {
            }

            override fun onKeyboardClose() {
                if (popup.isShowing) {
                    popup.dismiss()
                }
            }
        }

        val onBackSpaceInterface: EmojiPopup.OnEmojiconBackspaceClickedListener = object : EmojiPopup.OnEmojiconBackspaceClickedListener {
            override fun onEmojiconBackspaceClicked(v: View) {
                val event = KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL)
                msgInput.dispatchKeyEvent(event)
            }
        }

        // setup emoji function
        popup.setSizeForSoftKeyboard()
        popup.setOnSoftKeyboardOpenCloseListener(onItemClickInterface)
        popup.setOnEmojiconBackspaceClickedListener(onBackSpaceInterface)

    }
}