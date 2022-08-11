package sg.com.agentapp.global.emoji

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import sg.com.agentapp.R

class EmojiAdapter(context: Context) : BaseAdapter() {
    private val emojiPeople = People()
    val mInflator: LayoutInflater = LayoutInflater.from(context)

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View? = this.mInflator.inflate(R.layout.emojipopup_chatlist, parent, false)
        val vh: EmojiHolder
        vh = EmojiHolder(view)

        vh.emojiIcon.text = StringBuilder().appendCodePoint(emojiPeople[position]).toString()
        vh.emojiIcon.setOnClickListener {
            //            var base = ChatRoom.chatEditView?.msg_input
//            var emoji = vh.emojiIcon.text.toString()
//            ChatRoom.chatEditView?.msg_input = "$base$emoji"
//            Log.i("wtf", "$base$emoji")
        }
        return view!!
    }

    override fun getItem(position: Int): Any {
        return emojiPeople[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return emojiPeople.size
    }
}

class EmojiHolder(row: View?) {
    val emojiIcon: TextView = row?.findViewById(R.id.gridtext) as TextView
}

private fun People(): List<Int> {
    return listOf(
            0x1f600,
            0x1f601,
            0x1f602,
            0x1f603,
            0x1f604,
            0x1f605,
            0x1f606,
            0x1f607,
            0x1f609,
            0x1f60a,
            0x1f923,
            0x1f920,
            0x1f921,
            0x1f922,
            0x1f924,
            0x1f925,
            0x1f927,
            0x1f642,
            0x1f643,
            0x263a,
            0x1f60b,
            0x1f60c,
            0x1f60d,
            0x1f618,
            0x1f617,
            0x1f619,
            0x1f61a,
            0x1f61c,
            0x1f61d,
            0x1f61b,
            0x1f911,
            0x1f913,
            0x1f60e,
            0x1f917,
            0x1f60f,
            0x1f636,
            0x1f610,
            0x1f611,
            0x1f612,

            0x1f644,
            0x1f914,
            0x1f633,
            0x1f61e,
            0x1f61f,
            0x1f620,
            0x1f621,
            0x1f614,
            0x1f615,
            0x1f641,
            0x2639,
            0x1f623,
            0x1f616,
            0x1f62b,
            0x1f629,
            0x1f624,
            0x1f62e,
            0x1f631,
            0x1f628,
            0x1f630,
            0x1f62f,
            0x1f626,
            0x1f627,
            0x1f622,
            0x1f625,
            0x1f62a,
            0x1f613,
            0x1f62d,
            0x1f635,
            0x1f632,
            0x1f910,
            0x1f637,
            0x1f912,
            0x1f915,
            0x1f634,
            0x1f4a4,
            0x1f4a9,
            0x1f608,
            0x1f47f,
            0x1f479,
            0x1f47a,
            0x1f480,
            0x1f47b,
            0x1f47d,
            0x1f916,
            0x1f63a,
            0x1f638,
            0x1f639,
            0x1f63b,
            0x1f63c,
            0x1f63d,
            0x1f640,
            0x1f63f,
            0x1f63e,

            0x1f91b,
            0x1f91c,
            0x1f91d,
            0x1f91e,
            0x1f91f,
            0x1f91b,


            0x1f64c,
            0x1f44f,
            0x1f44b,
            0x1f44d,
            0x1f44e,
            0x1f44a,
            0x270a,
            0x270c,
            0x1f44c,
            0x270b,
            0x1f450,
            0x1f4aa,
            0x1f64f,
            0x261d,
            0x1f446,
            0x1f447,
            0x1f448,
            0x1f449,
            0x1f595,
            0x1f590,
            0x1f918,
            0x270d,
            0x1f485,
            0x1f444,
            0x1f445,
            0x1f442,
            0x1f443,
            0x1f441,
            0x1f440,
            0x1f464,
            0x1f465,
            0x1f5e3,
            0x1f476,
            0x1f466,
            0x1f467,
            0x1f468,
            0x1f469,
            0x1f471,

            0x1f474,

            0x1f475,
            0x1f472,
            0x1f473,
            0x1f46e,
            0x1f477,
            0x1f482,
            0x1f575,
            0x1f385,
            0x1f47c,
            0x1f478,
            0x1f470,
            0x1f6b6,
            0x1f3c3,
            0x1f483,
            0x1f46f,
            0x2728,
            0x1f31f,
            0x1f4ab,
            0x1f4a2,

            0x1f46b,
            0x1f46c,
            0x1f46d,
            0x1f647,
            0x1f481,
            0x1f645,
            0x1f646,
            0x1f64b,
            0x1f64e,
            0x1f64d,
            0x1f487,
            0x1f486,
            0x1f491,
            0x1f48f,
            0x1f46a,
            0x1f45a,
            0x1f455,
            0x1f456,
            0x1f454,
            0x1f457,
            0x1f459,
            0x1f458,
            0x1f484,
            0x1f48b,
            0x1f463,
            0x1f460,
            0x1f461,
            0x1f462,
            0x1f45e,
            0x1f45f,
            0x1f452,
            0x1f3a9,
            0x1f393,
            0x1f451,
            0x26d1,
            0xe43a,
            0x1f45d,
            0x1f45b,
            0x1f45c,
            0x1f4bc,
            0x1f453,
            0x1f576,
            0x1f48d,
            0x1f302)
}