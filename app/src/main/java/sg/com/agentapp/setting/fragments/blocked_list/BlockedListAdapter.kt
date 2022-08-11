package sg.com.agentapp.setting.fragments.blocked_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import sg.com.agentapp.R

class BlockedListAdapter : ListAdapter<List<String>, BlockedListHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedListHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.blockedlist_item, parent, false)

        return BlockedListHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedListHolder, position: Int) {
        holder.setData()
    }


    override fun getItemCount(): Int {
        return 5
    }

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<List<String>>() {
            override fun areItemsTheSame(
                    oldUser: List<String>, newUser: List<String>): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                //            return oldUser.getChatList().getChatJid().equals(newUser.getChatList().getChatJid());
                return false
            }

            override fun areContentsTheSame(
                    oldUser: List<String>, newUser: List<String>): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return oldUser == newUser
            }
        }
    }
}
