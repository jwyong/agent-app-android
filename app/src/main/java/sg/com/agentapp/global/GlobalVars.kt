package sg.com.agentapp.global

import android.os.Environment

object GlobalVars {

    //LOG
    const val I_LOG = "wtf"

    // TAGs
    const val TAG1 = "JAY"

    // preferences strings
    const val PREF_ACCESS_TOKEN = "PREF_ACCESS_TOKEN"
    const val PREF_EJABBERD_PASSWORD = "PREF_EJABBERD_PASSWORD"
    const val PREF_CEA_NO = "PREF_CEA_NO"
    const val PREF_NAME = "PREF_NAME"
    const val PREF_PHONE = "PREF_PHONE"
    const val PREF_ESTATE_AGENT = "PREF_ESTATE_AGENT"
    const val PREF_LICENSE_NO = "PREF_LICENSE_NO"
    const val PREF_ACTIVE_DATE = "PREF_ACTIVE_DATE"
    const val PREF_USER_PROFILE = "PREF_USER_PROFILE"

    // xmpp
    const val XMPP_HOST = "agent.soappchat.com"
    const val XMPP_DOMAIN = "xmpp.soappchat.com"
    const val XMPP_PORT = 5222
    const val XMPP_RESOURCE = "Android"

    //=== directory paths
    val DEFAULT_FILE_DATE_FORMAT = "yyyyMMdd_HHmmss"
    val TEMP_IMG_NAME = "temp.jpg"

    // folder names
    val APP_DIR = "AgentApp"
    val SENT_DIR = "Sent"
    val IMGES_DIR = "AA_Images"
    val AUDIO_DIR = "AA_Audio_Clips"
    val DOCS_DIR = "AA_Documents"
    val PROFILE_DIR = "AA_Profile"

    //media: imges
    val IMAGES_PATH = Environment.getExternalStorageDirectory().toString() + "/$APP_DIR/$IMGES_DIR/"
    val IMAGES_PATH_SENT = Environment.getExternalStorageDirectory().toString() + "/$APP_DIR/$IMGES_DIR/$SENT_DIR/"
    val IMAGE_PATH_PROFILE = Environment.getExternalStorageDirectory().toString() + "/$APP_DIR/$PROFILE_DIR/"

    //media: audio
    val AUDIO_PATH = Environment.getExternalStorageDirectory().toString() + "/$APP_DIR/$AUDIO_DIR/"
    val AUDIO_PATH_SENT = Environment.getExternalStorageDirectory().toString() + "/$APP_DIR/$AUDIO_DIR/$SENT_DIR/"

    //media: docs
    val DOCS_PATH = Environment.getExternalStorageDirectory().toString() + "/$APP_DIR/$DOCS_DIR/"
    val DOCS_PATH_SENT = Environment.getExternalStorageDirectory().toString() + "/$APP_DIR/$DOCS_DIR/$SENT_DIR/"

    //=== msg bodies (push/media display msg)
    //--- media
    const val MEDIA_AUDIO_LENGTH = "00:00"

    const val MEDIA_IMG_RECEIVED = "Image Received"
    const val MEDIA_AUDIO_RECEIVED = "Audio Clip Received"
    const val MEDIA_DOC_RECEIVED = "Document Received"

    const val MEDIA_SENT = "Media File"
    const val MEDIA_IMG_SENT = "Image"
    const val MEDIA_AUDIO_SENT = "Audio Clip"
    const val MEDIA_DOC_SENT = "Document"

    //--- delete
    const val MSG_DELETED = "This message has been deleted"

    //===== One Map
    const val ONE_MAP_ACC_CODE = "pk.eyJ1IjoiamFzb25mb28iLCJhIjoiY2ptcHl3eHltMWtrOTNrcGR1b3BnNDRwYSJ9.NUWEndjEp1YNaWy80_-Hzg"
}