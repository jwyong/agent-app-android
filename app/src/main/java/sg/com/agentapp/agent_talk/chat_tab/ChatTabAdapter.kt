package sg.com.agentapp.agent_talk.chat_tab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sg.com.agentapp.R
import sg.com.agentapp.agent_talk.chat_room.ChatRoom
import sg.com.agentapp.databinding.ChatlistItemBinding
import sg.com.agentapp.sql.joiner.ChatTabList

class ChatTabAdapter : ListAdapter<ChatTabList, ChatTabAdapter.Holder>(DIFF_CALLBACK) {
    // for rv selection
    var tracker: SelectionTracker<Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder((DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.chatlist_item, parent, false) as ChatlistItemBinding?)!!)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
//        tracker?.let {
//            holder.setData(getItem(position), it.isSelected(getItemId(position)))
//        }
        holder.setData(getItem(position), false)

    }

    override fun getItemId(position: Int): Long = getItem(position).chatList.chatRow!!.toLong()

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

    class Holder internal constructor(internal var chatlistItemBinding: ChatlistItemBinding) : RecyclerView.ViewHolder(chatlistItemBinding.root) {
        private val context: Context

        init {
            context = itemView.context
        }

        fun setData(item: ChatTabList, isActivated: Boolean) {
            chatlistItemBinding.data = item

//            if (item.contactRoster.CRProfileThumb != null && item.contactRoster.CRProfileThumb!!.size ==0) {
            Glide.with(context)
                    .load(item.contactRoster.CRProfileThumb)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile_def_200px)
                    .error(R.drawable.ic_profile_def_200px)
                    .dontAnimate()
                    .into(itemView.findViewById(R.id.user_profile))
//        }else{
//                Glide.with(context)
//                        .load(profilePicUrl)
//                        .placeholder(R.drawable.ic_profile_def_200px)
//                        .error(R.drawable.ic_profile_def_200px)
//                        .circleCrop()
//                        .dontAnimate()
//                        .into(findViewById(R.id.user_profile))
//            }


            itemView.setOnClickListener { view ->
                val intent = Intent(context, ChatRoom::class.java)
                val b = Bundle()
                b.putString("jid", item.contactRoster.CRJid)
                b.putString("name", item.contactRoster.CRName)
                b.putString("phone", item.contactRoster.CRPhoneNumber)
                intent.putExtra("b", b)
                context.startActivity(intent)

            }

            // disable longclick trigger onclick
            itemView.setOnLongClickListener { view ->
                true
            }

            // for selection
            itemView.isActivated = isActivated
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
                object : ItemDetailsLookup.ItemDetails<Long>() {
                    override fun getPosition(): Int = adapterPosition
                    override fun getSelectionKey(): Long? = itemId
                }

    }
}
