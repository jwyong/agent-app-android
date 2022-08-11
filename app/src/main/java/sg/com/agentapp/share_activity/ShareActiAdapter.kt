package sg.com.agentapp.share_activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sg.com.agentapp.agent_talk.chat_room.ChatRoom
import sg.com.agentapp.databinding.ShareActiItemBinding
import sg.com.agentapp.sql.joiner.ChatTabList


class ShareActiAdapter : ListAdapter<ChatTabList, ShareActiAdapter.Holder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder((DataBindingUtil.inflate(LayoutInflater.from(parent.context), sg.com.agentapp.R.layout.share_acti_item, parent, false) as ShareActiItemBinding?)!!)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setData(getItem(position))
    }

    override fun getItemId(position: Int): Long = position.toLong()

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatTabList>() {
            override fun areItemsTheSame(
                    oldUser: ChatTabList, newUser: ChatTabList): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                //            return oldUser.getChatList().getChatJid().equals(newUser.getChatList().getChatJid());

                return oldUser.chatList.chatRow!!.equals(newUser.chatList.chatRow)
            }

            override fun areContentsTheSame(
                    oldUser: ChatTabList, newUser: ChatTabList): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return oldUser.equals(newUser)
            }
        }
    }

    class Holder internal constructor(internal var itemBinding: ShareActiItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        private val context: Context = itemView.context
        private val acti = context as ShareActi

        fun setData(item: ChatTabList) {
            itemBinding.data = item

            Glide.with(context)
                    .load(item.contactRoster.CRProfileThumb)
                    .circleCrop()
                    .placeholder(sg.com.agentapp.R.drawable.ic_profile_def_200px)
                    .error(sg.com.agentapp.R.drawable.ic_profile_def_200px)
                    .dontAnimate()
                    .into(itemView.findViewById(sg.com.agentapp.R.id.user_profile))

            itemView.setOnClickListener { view ->
                val intent = Intent(context, ChatRoom::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                val b = Bundle()
                b.putString("jid", item.contactRoster.CRJid)
                b.putString("name", item.contactRoster.CRName)
                b.putString("phone", item.contactRoster.CRPhoneNumber)
                b.putString("profile_url", "")

                // add msgList - for sharing/etc
//                b.putSerializable("msgList", acti.msgList as java.io.Serializable)

                intent.putExtra("msgList", acti.msgListSerializable)
                intent.putExtra("b", b)
                context.startActivity(intent)
            }
        }
    }
}
