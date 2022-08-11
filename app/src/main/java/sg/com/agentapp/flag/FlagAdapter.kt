package sg.com.agentapp.flag

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import sg.com.agentapp.R
import sg.com.agentapp.databinding.*
import sg.com.agentapp.sql.joiner.FlagTabList

class FlagAdapter : ListAdapter<FlagTabList, FlagHolder>(DIFF_CALLBACK) {
    // for rv selection
    var tracker: SelectionTracker<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlagHolder {
        when (viewType) {
            // < 10 - text
            0, 1 -> return FlagHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.flag_item_text,
                    parent, false) as FlagItemTextBinding)

            // < 20 - reply
            // reply: text
            10, 11 -> return FlagHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.flag_item_reply_txt,
                    parent, false) as FlagItemReplyTxtBinding)

            // reply: img
            12, 13 -> return FlagHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.flag_item_reply_img,
                    parent, false) as FlagItemReplyImgBinding)

            // < 30 - media
            // media: images
            20, 21 -> return FlagHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.flag_item_img,
                    parent, false) as FlagItemImgBinding)

            // media: audio
            22, 23 -> return FlagHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.flag_item_audio,
                    parent, false) as FlagItemAudioBinding)

            // media: doc
            24, 25 -> return FlagHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.flag_item_doc,
                    parent, false) as FlagItemDocBinding)

            else -> return FlagHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_msg, parent, false))
        }
    }

    override fun onBindViewHolder(holder: FlagHolder, position: Int) {
        tracker?.let {
            holder.setData(getItem(position), it.isSelected(getItemId(position)))
        }
    }

    public override fun getItem(position: Int): FlagTabList? {
        return super.getItem(position)
    }

    override fun getItemId(position: Int): Long = getItem(position)!!.message.MsgRow.toLong()

    override fun getItemViewType(position: Int): Int {
        val viewType = getItem(position)

        return if (viewType != null) getItem(position)!!.message.IsSender else 99
    }

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FlagTabList>() {
            override fun areItemsTheSame(
                    oldUser: FlagTabList, newUser: FlagTabList): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                //            return oldUser.getChatList().getChatJid().equals(newUser.getChatList().getChatJid());
                return oldUser.message.MsgRow.equals(newUser.message.MsgRow)
            }

            override fun areContentsTheSame(
                    oldUser: FlagTabList, newUser: FlagTabList): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return oldUser.equals(newUser)
            }
        }
    }
}
