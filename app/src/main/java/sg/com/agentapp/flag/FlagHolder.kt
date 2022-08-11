package sg.com.agentapp.flag

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import sg.com.agentapp.agent_talk.chat_room.ChatRoom
import sg.com.agentapp.databinding.*
import sg.com.agentapp.sql.entity.ContactRoster
import sg.com.agentapp.sql.entity.Message
import sg.com.agentapp.sql.joiner.FlagTabList

class FlagHolder : RecyclerView.ViewHolder {
    private val TAG = "JAY"

    private val context: Context = itemView.context

    // chat item views
    private var textBinding: FlagItemTextBinding? = null
    private var imgBinding: FlagItemImgBinding? = null
    private var replyTxtBinding: FlagItemReplyTxtBinding? = null
    private var replyImgBinding: FlagItemReplyImgBinding? = null
    private var audioBinding: FlagItemAudioBinding? = null
    private var docBinding: FlagItemDocBinding? = null

    private lateinit var message: Message
    private lateinit var contactRoster: ContactRoster

    internal constructor(itemView: View) : super(itemView)

    constructor(inflate: FlagItemTextBinding) : super(inflate.root) {
        textBinding = inflate
    }

    constructor(inflate: FlagItemImgBinding) : super(inflate.root) {
        imgBinding = inflate
    }

    constructor(inflate: FlagItemReplyTxtBinding) : super(inflate.root) {
        replyTxtBinding = inflate
    }

    constructor(inflate: FlagItemReplyImgBinding) : super(inflate.root) {
        replyImgBinding = inflate
    }

    constructor(inflate: FlagItemAudioBinding) : super(inflate.root) {
        audioBinding = inflate
    }

    constructor(inflate: FlagItemDocBinding) : super(inflate.root) {
        docBinding = inflate
    }

    fun setData(flagTabList: FlagTabList?, isActivated: Boolean) {
        if (flagTabList == null) {
            return
        }

        message = flagTabList.message
        contactRoster = flagTabList.contactRoster

        // set data to databinding based on case
        when (message.IsSender) {
            0, 1 -> { // text
                textBinding?.data = flagTabList
                textBinding?.executePendingBindings()
            }

            10, 11 -> { // reply txt
                replyTxtBinding?.data = flagTabList
                replyTxtBinding?.executePendingBindings()
            }

            12, 13 -> { // reply img
                replyImgBinding?.data = flagTabList
                replyImgBinding?.executePendingBindings()
            }

            20, 21 -> { // img
                imgBinding?.data = flagTabList
                imgBinding?.executePendingBindings()
            }

            22, 23 -> { // audio
                audioBinding?.data = flagTabList
                audioBinding?.executePendingBindings()
            }

            24, 25 -> { // doc
                docBinding?.data = flagTabList
                docBinding?.executePendingBindings()
            }

        }

        // for selection
        itemView.isActivated = isActivated

        // logging
        itemView.setOnClickListener {
            val intent = Intent(context, ChatRoom::class.java)
            val b = Bundle()
            b.putString("jid", contactRoster.CRJid)
            b.putString("name", contactRoster.CRName)
            b.putString("phone", contactRoster.CRPhoneNumber)
            intent.putExtra("b", b)
            context.startActivity(intent)
        }
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }
}