package sg.com.agentapp.fcm_push

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


data class FcmMessageModel(
        @field:SerializedName("badge")
        val badge: String? = null,

        @field:SerializedName("sender_jid")
        val senderJid: String? = null,

        @field:SerializedName("indi_mute")
        val indiMute: String? = null,

        @field:SerializedName("sound")
        val sound: String? = null,

        @field:SerializedName("body")
        val body: String? = null,

        @field:SerializedName("type")
        val type: String? = null,

        @field:SerializedName("title")
        val title: String? = null
) {


    companion object {
        fun toModel(data: MutableMap<String, String>?): FcmMessageModel {
            val gson = Gson()
            val jsonElement = gson.toJsonTree(data)
            val pojo = gson.fromJson<FcmMessageModel>(jsonElement, FcmMessageModel::class.java)
            return pojo
        }
    }

    override fun toString(): String {
        return "FcmMessageModel(badge=$badge, senderJid=$senderJid, indiMute=$indiMute, sound=$sound, body=$body, type=$type, title=$title)"
    }

}

