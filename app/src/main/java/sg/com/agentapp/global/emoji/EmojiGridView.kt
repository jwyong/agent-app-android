package sg.com.agentapp.global.emoji

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.emojipopup_chat.view.*

class EmojiGridView {

    fun emojiGridView(context: Context): View? {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val emojiGrid = layoutInflater.inflate(sg.com.agentapp.R.layout.emojipopup_chat, null)

        emojiGrid.mainGrid.adapter = EmojiAdapter(context)
        return emojiGrid
    }

}

