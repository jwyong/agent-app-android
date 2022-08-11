package sg.com.agentapp.xmpp.outgoing;


import android.util.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smackx.ping.packet.Ping;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import sg.com.agentapp.global.GlobalVars;
import sg.com.agentapp.xmpp.SmackHelper;

/* Created by chang on 03/07/2017. */
public class SingleChatStanza {
    private static final String TAG = "wtf";
    private StandardExtensionElement standardExtensionElement;
    private SmackHelper smackHelper = SmackHelper.INSTANCE;
    // setThread: chat = "chat", appt = "appointment", restaurant = "restaurant"

    // ping stanza to check if got stream
    public boolean PingStanza() {
        Ping ping = new Ping(SmackHelper.INSTANCE.getXmpptcpConnection().getXMPPServiceDomain());

        try {
            smackHelper.getXmpptcpConnection().createStanzaCollectorAndSend(ping).nextResultOrThrow(15000);

            // successfully got pong - got stream
            return true;

        } catch (InterruptedException | SmackException.NoResponseException | SmackException.NotConnectedException | XMPPException.XMPPErrorException e) {
            return false;

        }
    }

    // normal msg stanza
    public void NormalChatStanza(String Body, String To, String Subject, String uniqueID) {
        EntityBareJid jid;
        try {
            jid = JidCreate.entityBareFrom(To + "@" + GlobalVars.XMPP_DOMAIN);
            ChatManager chatManager = ChatManager.getInstanceFor(smackHelper.getXmpptcpConnection());
            Chat chat = chatManager.chatWith(jid);
            final Message message = new Message();
            message.setStanzaId(uniqueID);
            message.setType(Message.Type.chat);
            message.setBody(Body);
            message.setThread("chat");
            message.setSubject(Subject);
            StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                    "data", "urn:xmpp:soapp")
                    .addAttribute("chat_id", "message")
                    .build();
            message.addExtension(standardExtensionElement);

            chat.send(message);
        } catch (Exception e) {

            Log.e(TAG, "NormalChatStanza: ", e);
        }

    }

    // reply msg stanza
    public void ReplyChatStanza(String Body, String To, String Subject, String uniqueID, String replyMsgBody,
                                String replyMsgID, String replyType, String imgThumb, String replyMsgJid) {
        EntityBareJid jid;
        try {
            jid = JidCreate.entityBareFrom(To + "@" + GlobalVars.XMPP_DOMAIN);
            ChatManager chatManager = ChatManager.getInstanceFor(smackHelper.getXmpptcpConnection());
            Chat chat = chatManager.chatWith(jid);
            final Message message = new Message();
            message.setStanzaId(uniqueID);
            message.setType(Message.Type.chat);
            message.setBody(Body);
            message.setThread("chat");
            message.setSubject(Subject);
            StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                    "data", "urn:xmpp:soapp")
                    .addAttribute("chat_id", "reply")
                    .addAttribute("reply_body", replyMsgBody)
                    .addAttribute("reply_img_thumb", imgThumb)
                    .addAttribute("reply_jid", replyMsgJid) // jid of reply msg
                    .addAttribute("reply_id", replyMsgID)
                    .addAttribute("reply_type", replyType)
                    .build();
            message.addExtension(standardExtensionElement);

            chat.send(message);
        } catch (Exception e) {

            Log.e(TAG, "NormalChatStanza: ", e);
        }

    }

    // forward media stanza
    public void ForwardChatStanza(String Body, String To, String Subject, String uniqueID, String msgType,
                                  String mediaResID, String mediaInfo) {
        EntityBareJid jid;
        try {
            jid = JidCreate.entityBareFrom(To + "@" + GlobalVars.XMPP_DOMAIN);
            ChatManager chatManager = ChatManager.getInstanceFor(smackHelper.getXmpptcpConnection());
            Chat chat = chatManager.chatWith(jid);
            final Message message = new Message();
            message.setStanzaId(uniqueID);
            message.setType(Message.Type.chat);
            message.setBody(Body);
            message.setThread("chat");
            message.setSubject(Subject);
            StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                    "data", "urn:xmpp:soapp")
                    .addAttribute("chat_id", "forward")
                    .addAttribute("msg_type", msgType) //0 = text, 1 = img, 2 = audio, 3 = doc
                    .addAttribute("media_id", mediaResID)
                    .addAttribute("media_info", mediaInfo) // text = null, img = thumb, audio = length, doc = name
                    .build();
            message.addExtension(standardExtensionElement);

            chat.send(message);
        } catch (Exception e) {

            Log.e(TAG, "NormalChatStanza: ", e);
        }

    }

    // delete msg stanza
    public void DeleteChatStanza(String Body, String To, String Subject, String uniqueID, String delUniqueID) {
        EntityBareJid jid;
        try {
            jid = JidCreate.entityBareFrom(To + "@" + GlobalVars.XMPP_DOMAIN);
            ChatManager chatManager = ChatManager.getInstanceFor(smackHelper.getXmpptcpConnection());
            Chat chat = chatManager.chatWith(jid);
            final Message message = new Message();
            message.setStanzaId(uniqueID);
            message.setType(Message.Type.chat);
            message.setBody(Body);
            message.setThread("chat");
            message.setSubject(Subject);
            StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                    "data", "urn:xmpp:soapp")
                    .addAttribute("chat_id", "delete")
                    .addAttribute("message_id", delUniqueID)
                    .build();
            message.addExtension(standardExtensionElement);

            chat.send(message);
        } catch (Exception e) {

            Log.e(TAG, "DeleteChatStanza: ", e);
        }

    }

    // acknowledgement stanza
    public void ackStanza(String toJid, String uniqueID) {
        EntityBareJid jid;
        try {
            jid = JidCreate.entityBareFrom(toJid + "@" + GlobalVars.XMPP_DOMAIN);

            ChatManager chatManager = ChatManager.getInstanceFor(smackHelper.getXmpptcpConnection());
            Chat chat = chatManager.chatWith(jid);
            final Message message = new Message();
            message.setStanzaId(uniqueID);
            standardExtensionElement = new StandardExtensionElement("received", "urn:xmpp:receipts");
            StandardExtensionElement standardExtensionElement = StandardExtensionElement.builder(
                    "received", "urn:xmpp:receipts")
                    .addAttribute("id", uniqueID)
                    .build();
            message.addExtension(standardExtensionElement);

            chat.send(message);
        } catch (Exception e) {
            Log.e(TAG, "ackStanza: ", e);
        }
    }
}