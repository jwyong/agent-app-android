package sg.com.agentapp.agent_talk.chat_room

import android.content.Context
import android.util.Log
import android.view.View
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import sg.com.agentapp.databinding.*
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.sql.entity.Appointment
import sg.com.agentapp.sql.entity.Message

class ChatRoomHolder : RecyclerView.ViewHolder {
    private val TAG = "JAY"

    private val context: Context = itemView.context
    private val activity = context as ChatRoom
    private var isSender: Int = 0

    // chat item views
    private var outMsgBinding: ChatOutMsgBinding? = null
    private var inMsgBinding: ChatInMsgBinding? = null

    private var outReplyTextBinding: ChatOutReplyTextBinding? = null
    private var inReplyTextBinding: ChatInReplyTextBinding? = null
    private var outReplyImgBinding: ChatOutReplyImgBinding? = null
    private var inReplyImgBinding: ChatInReplyImgBinding? = null

    private var outImgBinding: ChatOutImgBinding? = null
    private var inImgBinding: ChatInImgBinding? = null
    private var outAudioBinding: ChatOutAudioBinding? = null
    private var inAudioBinding: ChatInAudioBinding? = null
    private var outDocBinding: ChatOutDocBinding? = null
    private var inDocBinding: ChatInDocBinding? = null

    private var outDeleteBinding: ChatOutDeleteBinding? = null
    private var inDeleteBinding: ChatInDeleteBinding? = null

    private var dateBinding: ChatDateBinding? = null

    //appt item view
    private var inNewApptBind: ChatInApptBinding? = null
    private var outNewApptBind: ChatOutApptBinding? = null
    private var outStatusApptBind: ChatOutApptStatusBinding? = null
    private var inStatusApptBind: ChatInApptStatusBinding? = null
    private var outApptUpdateBinding: ChatOutApptUpdateBinding? = null
    private var inApptUpdateBinding: ChatInApptUpdateBinding? = null

//    // chat item elements
//    private var chatBubble: ImageView? = null

    internal constructor(itemView: View) : super(itemView)

    constructor(inflate: ChatOutMsgBinding) : super(inflate.root) {
        outMsgBinding = inflate
    }

    constructor(inflate: ChatInMsgBinding) : super(inflate.root) {
        inMsgBinding = inflate
    }

    constructor(inflate: ChatOutReplyTextBinding) : super(inflate.root) {
        outReplyTextBinding = inflate
    }

    constructor(inflate: ChatInReplyTextBinding) : super(inflate.root) {
        inReplyTextBinding = inflate
    }

    constructor(inflate: ChatOutReplyImgBinding) : super(inflate.root) {
        outReplyImgBinding = inflate
    }

    constructor(inflate: ChatInReplyImgBinding) : super(inflate.root) {
        inReplyImgBinding = inflate
    }

    constructor(inflate: ChatOutImgBinding) : super(inflate.root) {
        outImgBinding = inflate
    }

    constructor(inflate: ChatInImgBinding) : super(inflate.root) {
        inImgBinding = inflate
    }

    constructor(inflate: ChatOutAudioBinding) : super(inflate.root) {
        outAudioBinding = inflate
    }

    constructor(inflate: ChatInAudioBinding) : super(inflate.root) {
        inAudioBinding = inflate
    }

    constructor(inflate: ChatOutDocBinding) : super(inflate.root) {
        outDocBinding = inflate
    }

    constructor(inflate: ChatInDocBinding) : super(inflate.root) {
        inDocBinding = inflate
    }

    constructor(inflate: ChatOutDeleteBinding) : super(inflate.root) {
        outDeleteBinding = inflate
    }

    constructor(inflate: ChatInDeleteBinding) : super(inflate.root) {
        inDeleteBinding = inflate
    }

    constructor(inflate: ChatDateBinding) : super(inflate.root) {
        dateBinding = inflate
    }

    constructor(inflate: ChatInApptBinding) : super(inflate.root) {
        inNewApptBind = inflate
    }

    constructor(inflate: ChatOutApptBinding) : super(inflate.root) {
        outNewApptBind = inflate
    }

    constructor(inflate: ChatInApptStatusBinding) : super(inflate.root) {
        inStatusApptBind = inflate
    }

    constructor(inflate: ChatOutApptStatusBinding) : super(inflate.root) {
        outStatusApptBind = inflate
    }

    constructor(inflate: ChatInApptUpdateBinding) : super(inflate.root) {
        inApptUpdateBinding = inflate
    }

    constructor(inflate: ChatOutApptUpdateBinding) : super(inflate.root) {
        outApptUpdateBinding = inflate
    }

    fun setData(message: Message?, isActivated: Boolean) {
        if (message != null) {
            isSender = message.IsSender
        }

        // set data to databinding based on case
        when (isSender) {
            0 -> {
                outMsgBinding?.setVar(message)
                outMsgBinding?.executePendingBindings()
            }

            1 -> {
                inMsgBinding?.setVar(message)
                inMsgBinding?.executePendingBindings()
            }

            10 -> {
                outReplyTextBinding?.setVar(message)
                outReplyTextBinding?.chatroom = activity
                outReplyTextBinding?.executePendingBindings()
            }

            11 -> {
                inReplyTextBinding?.setVar(message)
                inReplyTextBinding?.chatroom = activity
                inReplyTextBinding?.executePendingBindings()
            }

            12 -> {
                outReplyImgBinding?.setVar(message)
                outReplyImgBinding?.chatroom = activity
                outReplyImgBinding?.executePendingBindings()
            }

            13 -> {
                inReplyImgBinding?.setVar(message)
                inReplyImgBinding?.chatroom = activity
                inReplyImgBinding?.executePendingBindings()
            }

            20 -> {
                outImgBinding?.setVar(message)
                outImgBinding?.executePendingBindings()
            }

            21 -> {
                inImgBinding?.setVar(message)
                inImgBinding?.executePendingBindings()
            }

            22 -> {
                outAudioBinding?.setVar(message)
                outAudioBinding?.chatroom = activity
                outAudioBinding?.executePendingBindings()
            }

            23 -> {
                inAudioBinding?.setVar(message)
                inAudioBinding?.chatroom = activity
                inAudioBinding?.executePendingBindings()
            }

            24 -> {
                outDocBinding?.setVar(message)
                outDocBinding?.executePendingBindings()
            }

            25 -> {
                inDocBinding?.setVar(message)
                inDocBinding?.executePendingBindings()
            }

            30 -> {
                outDeleteBinding?.setVar(message)
                outDeleteBinding?.executePendingBindings()
            }

            31 -> {
                inDeleteBinding?.setVar(message)
                inDeleteBinding?.executePendingBindings()
            }

            40 -> {
                dateBinding?.setVar(message)
                dateBinding?.executePendingBindings()
            }

            50 -> {
                val apptdetail = DatabaseHelper.getApptDetail(message!!.MsgMediaResID!!)
                outNewApptBind?.data = apptdetail
                outNewApptBind?.chatroom = activity
                outNewApptBind?.msgDate = message.getFormattedDate(message.MsgDate)
                outNewApptBind?.executePendingBindings()

            }
            51 -> {
                val apptdetail = DatabaseHelper.getApptDetail(message!!.MsgMediaResID!!)
                inNewApptBind?.data = apptdetail
                inNewApptBind?.chatroom = activity
                inNewApptBind?.msgDate = message.getFormattedDate(message.MsgDate)
                inNewApptBind?.executePendingBindings()

            }

            52 -> {
                val apptdetail = DatabaseHelper.getApptDetail(message!!.MsgMediaResID!!)
                outStatusApptBind?.data = apptdetail
                outStatusApptBind?.msg = message
                outStatusApptBind?.executePendingBindings()
            }

            53 -> {
                val apptdetail = DatabaseHelper.getApptDetail(message!!.MsgMediaResID!!)
                inStatusApptBind?.data = apptdetail
                inStatusApptBind?.msg = message
                inStatusApptBind?.executePendingBindings()

            }

            54 -> {
                val apptdetail = DatabaseHelper.getApptDetail(message!!.MsgMediaResID!!)
                val gson = Gson()
                val appointmentOld: Appointment = gson.fromJson(message.MsgMediaInfo, Appointment::class.java)

                outApptUpdateBinding?.data = message
                outApptUpdateBinding?.oldDateTimeLong = appointmentOld.ApptDateTime
                outApptUpdateBinding?.newDateTimeLong = apptdetail.ApptDateTime
                outApptUpdateBinding?.executePendingBindings()
            }

            55 -> {
                val apptdetail = DatabaseHelper.getApptDetail(message!!.MsgMediaResID!!)
                val gson = Gson()
                val appointmentOld: Appointment = gson.fromJson(message.MsgMediaInfo, Appointment::class.java)

                inApptUpdateBinding?.data = message
                inApptUpdateBinding?.oldDateTimeLong = appointmentOld.ApptDateTime
                inApptUpdateBinding?.newDateTimeLong = apptdetail.ApptDateTime
                inApptUpdateBinding?.chatroom = activity
                inApptUpdateBinding?.executePendingBindings()
            }
        }

        // for selection
        itemView.isActivated = isActivated

        // logging
        itemView.setOnClickListener {
            Log.d("JAY", "MsgMediaResID = ${message?.MsgMediaResID}" +
                    ", msgMediaInfo size = ${message?.MsgMediaInfo?.length}" +
                    ", replyMsgJid = ${message?.MsgReplyJid}" +
                    ", replyMediaInfo size = ${message?.MsgReplyMediaInfo?.length}")
        }
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }

    // return msg item of holder
    fun getIsSender(): Int {
        return isSender
    }
}