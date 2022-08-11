package sg.com.agentapp.agent_talk.chat_room

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedListAdapter
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import sg.com.agentapp.R
import sg.com.agentapp.databinding.*
import sg.com.agentapp.sql.entity.Message

class ChatRoomAdapter : PagedListAdapter<Message, ChatRoomHolder>(DIFF_CALLBACK) {
    // for rv selection
    var tracker: SelectionTracker<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomHolder {
        when (viewType) {
            //===== chat ( < 50 )
            // < 10 - text
            0 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_out_msg,
                    parent, false) as ChatOutMsgBinding)
            1 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_in_msg,
                    parent, false) as ChatInMsgBinding)

            // < 20 - reply
            10 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_out_reply_text,
                    parent, false) as ChatOutReplyTextBinding)
            11 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_in_reply_text,
                    parent, false) as ChatInReplyTextBinding)
            12 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_out_reply_img,
                    parent, false) as ChatOutReplyImgBinding)
            13 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_in_reply_img,
                    parent, false) as ChatInReplyImgBinding)

            // < 30 - media
            // media: images
            20 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_out_img,
                    parent, false) as ChatOutImgBinding)
            21 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_in_img,
                    parent, false) as ChatInImgBinding)

            // media: audio
            22 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_out_audio,
                    parent, false) as ChatOutAudioBinding)
            23 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_in_audio,
                    parent, false) as ChatInAudioBinding)

            // media: doc
            24 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_out_doc,
                    parent, false) as ChatOutDocBinding)
            25 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_in_doc,
                    parent, false) as ChatInDocBinding)

            //===== unselectable
            // >= 30 - delete
            30 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_out_delete,
                    parent, false) as ChatOutDeleteBinding)
            31 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_in_delete,
                    parent, false) as ChatInDeleteBinding)

            // >= 40 - date, unread, etc
            40 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_date,
                    parent, false) as ChatDateBinding)
            // 41 -> unread?
            // 42 -> ...


            //===== appt ( >= 50 )
            50 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_out_appt,
                    parent, false) as ChatOutApptBinding)
            51 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_in_appt,
                    parent, false) as ChatInApptBinding)
            52 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_out_appt_status,
                    parent, false) as ChatOutApptStatusBinding)
            53 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_in_appt_status,
                    parent, false) as ChatInApptStatusBinding)

            54 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_out_appt_update,
                    parent, false) as ChatOutApptUpdateBinding)
            55 -> return ChatRoomHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), R.layout.chat_in_appt_update,
                    parent, false) as ChatInApptUpdateBinding)

            else -> return ChatRoomHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_msg, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ChatRoomHolder, position: Int) {

        tracker?.let {
            holder.setData(getItem(position), it.isSelected(getItemId(position)))
        }
    }

    public override fun getItem(position: Int): Message? {
        return super.getItem(position)
    }

    override fun getItemId(position: Int): Long = getItem(position)!!.MsgRow.toLong()

    override fun getItemViewType(position: Int): Int {
        val viewType = getItem(position)

        return if (viewType != null) getItem(position)!!.IsSender else 99
    }

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(
                    oldUser: Message, newUser: Message): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                //            return oldUser.getChatList().getChatJid().equals(newUser.getChatList().getChatJid());
                return oldUser.MsgRow.equals(newUser.MsgRow)
            }

            override fun areContentsTheSame(
                    oldUser: Message, newUser: Message): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return oldUser.equals(newUser)
            }
        }
    }
}
