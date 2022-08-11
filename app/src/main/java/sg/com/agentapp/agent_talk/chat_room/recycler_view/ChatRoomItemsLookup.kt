package sg.com.agentapp.agent_talk.chat_room.recycler_view

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import sg.com.agentapp.agent_talk.chat_room.ChatRoomHolder

class ChatRoomItemsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            val viewHolder = recyclerView.getChildViewHolder(view) as ChatRoomHolder
            // only allow selectable cases (< 30)
//            if (viewHolder.getIsSender() < 30) { // selectables
                return viewHolder.getItemDetails()
//            } else {
////                return null
//
//                return object : ItemDetailsLookup.ItemDetails<Long>() {
//                    override fun getPosition(): Int = -1
//                    override fun getSelectionKey(): Long? = -1
//                }
//            }
        }
        return null
    }
}