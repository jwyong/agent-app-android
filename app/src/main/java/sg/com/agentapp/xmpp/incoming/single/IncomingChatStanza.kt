package sg.com.agentapp.xmpp.incoming.single

import android.util.Log
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.StandardExtensionElement
import org.jivesoftware.smackx.delay.packet.DelayInformation
import org.json.JSONObject
import org.jxmpp.util.XmppStringUtils
import retrofit2.Response
import sg.com.agentapp.AgentApp
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.api.api_models.CreateApptReq
import sg.com.agentapp.api.api_models.CreateApptRes
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.xmpp.outgoing.SingleChatStanza


class IncomingChatStanza {
    val TAG = "jay"
    fun filterByChatIDType(stanza: Message) {
        // prep basic variables
        val jid = XmppStringUtils.parseLocalpart(stanza.from.toString())
        val msgID = stanza.stanzaId

        // just ack if duplicated msg
        if (DatabaseHelper.checkMsgExist(jid, msgID)) {
            // ack msg
            SingleChatStanza().ackStanza(jid, msgID)
            return
        }

        // get extension based on stanza
        val extension = stanza.getExtension("urn:xmpp:soapp") as StandardExtensionElement
        val chatIDType = extension.getAttributeValue("chat_id")?.toString()

        // get timestamp
        val cTime: Long
        if (stanza.hasExtension("delay")) { // delayed, use delayed date
            val delayInformation = stanza.extensions[1] as DelayInformation
            cTime = delayInformation.stamp.time
        } else {
            cTime = System.currentTimeMillis()
        }

        // do actions based on chatIDType (text, img, audio, etc)
        when (chatIDType) {
            "message" -> { // text
                DatabaseHelper.inTextMsg(jid, cTime, msgID, stanza.body, 0)
            }

            "reply" -> { // reply
                val replyType = extension.getAttributeValue("reply_type").toString()
                val isSender: Int
                isSender = when (replyType) {
                    "0" -> { // text
                        11
                    }

                    "1" -> { // img
                        13
                    }

                    else -> {
                        11
                    }
                }
                val replyMsgData = extension.getAttributeValue("reply_body").toString()
                val replyMsgJid = extension.getAttributeValue("reply_jid").toString()
                val replyMsgID = extension.getAttributeValue("reply_id").toString()
                val replyImgThumb = extension.getAttributeValue("reply_img_thumb").toString()

                DatabaseHelper.replyMessage(jid, stanza.body, cTime, msgID, isSender, replyMsgData,
                        replyMsgID, replyImgThumb, replyMsgJid)
            }

            "forward" -> {
                val msgType = extension.getAttributeValue("msg_type").toString()
                val mediaResID = extension.getAttributeValue("media_id").toString()
                val mediaInfo = extension.getAttributeValue("media_info").toString()

                when (msgType) {
                    "0" -> { // text
                        DatabaseHelper.inTextMsg(jid, cTime, msgID, stanza.body, 1)
                    }

                    "1" -> { // img
                        DatabaseHelper.inMedia(jid, msgID, cTime, GlobalVars.MEDIA_IMG_RECEIVED, mediaInfo,
                                mediaInfo, mediaResID, 21, -3, 1)
                    }

                    "2" -> { // audio
                        DatabaseHelper.inMedia(jid, msgID, cTime, GlobalVars.MEDIA_AUDIO_RECEIVED, null,
                                mediaInfo, mediaResID, 23, -3, 1)
                    }

                    "3" -> { // doc
                        DatabaseHelper.inMedia(jid, msgID, cTime, GlobalVars.MEDIA_DOC_RECEIVED, null,
                                mediaInfo, mediaResID, 25, -3, 1)
                    }
                }
            }

            "image_id" -> { // img (issender = 21, msgOffline = -3 (not downloaded))
                val imgThumbStr = extension.getAttributeValue("image_thumbnail").toString()
                val imgResID = extension.getAttributeValue("resource_id").toString()

                // put imgThumb in mediaFilPath for loading
                // put imgThumb in mediaInfo for storing
                DatabaseHelper.inMedia(jid, msgID, cTime, GlobalVars.MEDIA_IMG_RECEIVED, imgThumbStr,
                        imgThumbStr, imgResID, 21, -3, 0)
            }

            "audio_id" -> { // img (issender = 23, msgOffline = -3 (not downloaded))
                val resID = extension.getAttributeValue("resource_id").toString()

                // set fileInfo (audio length) to empty first)
                DatabaseHelper.inMedia(jid, msgID, cTime, GlobalVars.MEDIA_AUDIO_RECEIVED, null,
                        GlobalVars.MEDIA_AUDIO_LENGTH, resID, 23, -3, 0)
            }

            "doc_id" -> { // img (issender = 25, msgOffline = -3 (not downloaded))
                val resID = extension.getAttributeValue("resource_id").toString()
                val fileName = extension.getAttributeValue("file_name").toString()

                // set fileInfo (audio length) to empty first)
                DatabaseHelper.inMedia(jid, msgID, cTime, GlobalVars.MEDIA_DOC_RECEIVED, null,
                        fileName, resID, 25, -3, 0)
            }

            "delete" -> { // delete msg
                val delMsgID = extension.getAttributeValue("message_id").toString()

                // delete from sqlite (isSender = 30)
                DatabaseHelper.deleteMessage(AgentApp.instance?.applicationContext, jid, 31,
                        delMsgID, msgID)
            }

            "new_appointment" -> { // text
                val apptId = extension.getAttributeValue("appointment_id")?.toString()
                getAppt(apptId, jid, msgID, 1, cTime)

            }

            "update_appointment" -> { // text
                val apptId = extension.getAttributeValue("appointment_id")?.toString()
                getAppt(apptId, jid, msgID, 2, cTime)
            }

            "accept_appointment" -> { // text
                val apptId = extension.getAttributeValue("appointment_id")?.toString()
                statusChg(apptId, jid, msgID, 1, cTime)
            }

            "reject_appointment" -> { // text
                val apptId = extension.getAttributeValue("appointment_id")?.toString()
                statusChg(apptId, jid, msgID, 3, cTime)
            }

            else -> {
                Log.e(TAG, "leftover chat id type = $stanza")
            }
        }
    }

    @Synchronized
    fun getAppt(apptId: String?, jid: String, msgId: String, case: Int, cTime: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = sendReq(CreateApptRes(apptId))
                if (result.isSuccessful) {
                    val resultbody = result.body()
                    resultbody!!.appointmentId = apptId

                    var statusInt: Int = 0
                    when (resultbody.status) {
                        "0" -> {
                            statusInt = 2
                        }
                        "1" -> {
                            statusInt = 1
                        }
                        "2" -> {
                            statusInt = 3
                        }
                        else -> {
                        }
                    }

                    when (case) {
                        1 -> { // new appt
                            DatabaseHelper.insertNewAppt(resultbody, jid, statusInt, false)
                            DatabaseHelper.insertApptToMSg(resultbody.appointmentId!!, resultbody.date.toString(), jid, false, cTime, msgId)
                            SingleChatStanza().ackStanza(jid, msgId)
                        }
                        2 -> { // update appt
                            if (DatabaseHelper.checkApptExist(apptId!!)) {
                                // check if appt already expired
                                if (DatabaseHelper.checkApptExpired(apptId)) {
                                    SingleChatStanza().ackStanza(jid, msgId)

                                } else {
                                    val apptDetail = result.body()!!
                                    var apptOldDetail = DatabaseHelper.getApptDetail(apptDetail.appointmentId!!)
                                    val gson = GsonBuilder().create()
                                    val apptOldDetJson = gson.toJson(apptOldDetail)

                                    DatabaseHelper.updateAppt(apptId, apptDetail.date!!.toLong(), apptDetail.type!!,
                                            apptDetail.roomType!!, null, 0, jid, 2, false)

                                    DatabaseHelper.updateApptMsg(apptDetail.appointmentId!!, jid, false,
                                            cTime, msgId, apptOldDetJson, apptDetail.date.toLong())

                                    SingleChatStanza().ackStanza(jid, msgId)
                                }
                            } else {
                                DatabaseHelper.insertNewAppt(resultbody, jid, statusInt, false)
                                SingleChatStanza().ackStanza(jid, msgId)
                            }
                        }
                    }

                } else {
                    val errJson = JSONObject(result.errorBody()!!.string())
                    Log.e(TAG, "$errJson: ")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun statusChg(apptId: String?, jid: String, msgID: String, status: Int, cTime: Long) {
        if (DatabaseHelper.checkApptExist(apptId!!)) {
            DatabaseHelper.upateApptStatus(apptId, status, false, cTime, msgID)
        }

        SingleChatStanza().ackStanza(jid, msgID)
    }

    private suspend fun sendReq(apptId: CreateApptRes): Response<CreateApptReq> = RetroAPIClient.api.getAppointment(
            "Bearer " + Preferences.getInstance().accessToken, apptId
    )
}