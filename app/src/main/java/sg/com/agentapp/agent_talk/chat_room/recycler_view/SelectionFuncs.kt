package sg.com.agentapp.agent_talk.chat_room.recycler_view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import kotlinx.android.synthetic.main.chat_main.*
import sg.com.agentapp.R
import sg.com.agentapp.agent_talk.chat_room.ChatRoom
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.MiscHelper
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.global.UIHelper
import sg.com.agentapp.global.rv_selection.SelectionKeyProvider
import sg.com.agentapp.global.rv_selection.SelectionState
import sg.com.agentapp.share_activity.ShareActi
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.sql.entity.Message
import sg.com.agentapp.xmpp.outgoing.SingleChatStanza
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


class SelectionFuncs(context: Context) {
    private val TAG = "JAY"

    // basics
    private val chatroom = context as ChatRoom
    private val ctx = context
    var chatRoomTracker: SelectionTracker<Long>? = null

    // class vars
    private val jid = chatroom.jid

    // helper
    private val singleChatStanza = SingleChatStanza()
    private val miscHelper = MiscHelper()

    // selection lists
    var selectedMsgList: List<Message> = ArrayList()
    private var selIssenderList: List<Int> = ArrayList()

    // setup recyclerview selection
    fun setupRVSelTracker() {
        // build tracker
        chatRoomTracker = SelectionTracker
                .Builder(
                        "chatRoomSel",
                        chatroom.chat_room_rv,
                        SelectionKeyProvider(chatroom.chat_room_rv),
                        ChatRoomItemsLookup(chatroom.chat_room_rv),
                        StorageStrategy.createLongStorage()
                )
                .withSelectionPredicate(SelectionState(true, false, true))
                .build()

        // add tracker to adapter
        chatroom.chatRoomAdapter.tracker = chatRoomTracker

        // add observer to tracker
        chatRoomTracker?.addObserver(
                object : SelectionTracker.SelectionObserver<Long>() {
                    override fun onSelectionChanged() {
                        super.onSelectionChanged()

                        // set selection size to observable int
                        chatroom.itemSelected.set(chatRoomTracker?.selection!!.size())

                        // check which btns to show/hide when multi-select
                        showHideBtnsWhenSel()
                    }
                }
        )
    }

    // show/hide btns when multi-select
    fun showHideBtnsWhenSel() {
        // add items to selection List
        addSelItemsToList()

        // show/hide items based on selection
        if (chatroom.itemSelected.get() > 0) {
            // hide ALL selections if got unselectables ( >= 30 )
            if (selIssenderList.distinct().any { it >= 30 }) {
                setBtnObservables(0, false)
                setBtnObservables(1, false)
                setBtnObservables(2, false)
                chatroom.showFlagBtn.set(-1)
                setBtnObservables(4, false)

                return
            }

            showReplyBtn() // done
            showDeleteBtn() // done
            showCopyBtn() // done
            showFlagBtn() // done - only need do flagTab
            showForwardBtn() // pending reply cases
        }
    }

    // add selected items to list
    private fun addSelItemsToList() {
        // map tracker list to list of Message (it = itemID = row in sqlite)
        selectedMsgList = chatroom.chatRoomMainVM.messageList?.value!!.mapNotNull {
            if (chatRoomTracker!!.selection.contains(it.MsgRow.toLong())) {
                it
            } else {
                null
            }
        }

        // map other lists (issender, etc)
        selIssenderList = selectedMsgList.map {
            it.IsSender
        }
    }

    //--- show/hide btns funcs
    private fun showReplyBtn() {
        // SINGLE - text, img, audio, docs, reply
        setBtnObservables(0, canReply())
    }

    // return boolean based on can reply or not
    fun canReply(): Boolean {
        if (chatroom.itemSelected.get() == 1) { // single
            when (selIssenderList[0]) {
                0, 1, 10, 11, 12, 13, 20, 21 -> { // can reply (refer chat room adapter)
                    return true
                }

                else -> {
                    return false
                }
            }
        } else { // multi - can't reply
            // also hide replyUI if multi
            chatroom.showReplyUI.set(false)

            return false
        }
    }

    private fun showDeleteBtn() {
        // SINGLE - OUTGOING ONLY
        if (chatroom.itemSelected.get() == 1) { // single
            when (selIssenderList[0]) {
                0, 20, 22, 24, 10, 12 -> { // outgoing
                    setBtnObservables(1, true)
                }

                else -> {
                    setBtnObservables(1, false)
                }
            }

        } else {
            setBtnObservables(1, false)

        }
    }

    private fun showCopyBtn() {
        // SINGLE - text/reply cases only (copy msgbody only)
        if (chatroom.itemSelected.get() == 1) { // single
            if (selIssenderList[0] < 14) { // text, reply msges
                setBtnObservables(2, true)

            } else {
                setBtnObservables(2, false)

            }

        } else {
            setBtnObservables(2, false)

        }
    }

    private fun showFlagBtn() {
        // MULTI - all selectables
        val flaggedList = selectedMsgList.map {
            it.MsgFlagDate
        }
        if (flaggedList.contains(0)) { // got unflagged, show flag
            chatroom.showFlagBtn.set(1)

        } else { // all flagged, show unflag
            chatroom.showFlagBtn.set(0)

        }
    }

    private fun showForwardBtn() {
        // MULTI - text, media, reply (as text)
        // only for already downloaded media (msgoffline = 1)
        if (selIssenderList.distinct().any { it == 21 || it == 23 || it == 25 }) { // inMedia cases - check if downloaded
            val selMsgOfflineList = selectedMsgList.map {
                it.MsgOffline
            }

            if (selMsgOfflineList.distinct().any { it < 1 }) { // got < 1 msgOffline, means hvnt download
                setBtnObservables(4, false)

            } else { // all downloaded
                setBtnObservables(4, true)
            }
        } else {
            setBtnObservables(4, true)
        }
    }


    //===== long click toolbar btns
    fun replyBtnOnClick() {
        // SINGLE ONLY
        // show reply UI
        chatroom.showReplyUI.set(true)

        // set replyUI name
        when (selIssenderList[0]) {
            0, 10, 12, 20, 22, 24 -> { // outgoing - self's name
                chatroom.replyMsgName.set("You")
            }

            else -> { // incoming - room jid
                chatroom.replyMsgName.set(chatroom.name)
            }
        }

        // set replyUI thumb
        when (selIssenderList[0]) {
            20, 21 -> { // imges
                chatroom.replyMsgImgThumb.set(selectedMsgList[0].MsgMediaInfo)
            }

            else -> { // non-img
                chatroom.replyMsgImgThumb.set(null)
            }
        }

        // set reply msg body
        chatroom.replyMsgBody.set(selectedMsgList[0].MsgData)

        // set reply msg list
        chatroom.selectedReplyMsg = selectedMsgList[0]

        // clear selection
        clearSel()
    }

    fun deleteBtnOnClick() {
        // show confirmation dialog
        val deleteAction = Runnable {
            // SINGLE + OUTGOING ONLY
            // get variables
            val currentTime = System.currentTimeMillis()
            val selectedTime = selectedMsgList[0].MsgDate
            val selectedMsgID = selectedMsgList[0].MsgUniqueId

            Log.d(TAG, "currentTime = $currentTime, selTime = $selectedTime")

            // only allow deletion if less than 1hr diff
            if (currentTime - selectedTime <= 3600000) { // can delete
                val uuid = UUID.randomUUID().toString()
                // send stanza
                singleChatStanza.DeleteChatStanza(GlobalVars.MSG_DELETED, jid, Preferences.getInstance().agentName,
                        uuid, selectedMsgID)

                // delete from sqlite (isSender = 30)
                DatabaseHelper.deleteMessage(ctx, jid, 30, selectedMsgID!!, uuid)

            } else { // can't delete
                miscHelper.toastMsg(ctx, sg.com.agentapp.R.string.cant_delete_msg, Toast.LENGTH_SHORT)
            }

            // clear selection
            clearSel()
        }

        UIHelper().dialog2btn(ctx, ctx.getString(sg.com.agentapp.R.string.deleted_msg_confirm),
                ctx.getString(sg.com.agentapp.R.string.ok), ctx.getString(sg.com.agentapp.R.string.cancel), deleteAction,
                null, true)


    }

    fun copyBtnOnClick() {
        // copy msgdata to clipboard
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("Chat Message", selectedMsgList[0].MsgData)
        clipboard!!.primaryClip = clip

        // toast user
        miscHelper.toastMsg(ctx, R.string.copied_msg, Toast.LENGTH_SHORT)

        clearSel()
    }

    fun flagBtnOnClick() {
        // add to flagList
        DatabaseHelper.flagUnflagMsg(selectedMsgList, chatroom.showFlagBtn.get())

        // clear selection
        clearSel()
    }

    fun forwardBtn() {
        val intent = Intent(ctx, ShareActi::class.java)

        val b = Bundle()
        b.putInt("type", 1) // forward msg
        // add msgList - for sharing/etc
        intent.putExtra("msgList", selectedMsgList as Serializable)
        intent.putExtra("b", b)

        ctx.startActivity(intent)
    }


    //===== observable funcs
    private fun setBtnObservables(type: Int, boolean: Boolean) {
        when (type) {
            0 -> { // reply
                chatroom.showReplyBtn.set(boolean)
            }

            1 -> { // delete
                chatroom.showDeleteBtn.set(boolean)
            }

            2 -> { // copy
                chatroom.showCopyBtn.set(boolean)
            }

            4 -> { // forward
                chatroom.showForwardBtn.set(boolean)
            }
        }
    }

    // clear selections
    fun clearSel() {
        chatroom.itemSelected.set(0)
        chatRoomTracker?.clearSelection()
    }
}
