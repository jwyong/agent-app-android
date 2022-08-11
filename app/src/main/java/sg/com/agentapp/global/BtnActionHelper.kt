package sg.com.agentapp.global

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

class BtnActionHelper {
    fun callPhone(context: Context?, phone: String?) {
        // show whatsapp/phone bottom sheet
        val phonecallFunc = Runnable {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone")
            context?.startActivity(intent)
        }

        val whatsappFunc = Runnable {
            sendWhatsapp(context, phone)
        }
        UIHelper().btmDialog2Items(context!!, sg.com.agentapp.R.string.phone_call, sg.com.agentapp.R.string.whatsapp_call, phonecallFunc, whatsappFunc, true)

    }

    // send sms
    fun sendSms(context: Context?, phone: String?, smsBody: String?) {
        val uri = Uri.parse("smsto:$phone")
        val it = Intent(Intent.ACTION_SENDTO, uri)
        it.putExtra("sms_body", smsBody)
        context?.startActivity(it)
    }

    // send text to whatsapp
    fun sendWhatsapp(context: Context?, phone: String?) {
        val packageID = "com.whatsapp"
        try {
            val uri = Uri.parse("smsto:$phone")
            val i = Intent(Intent.ACTION_SENDTO, uri)
            i.setPackage(packageID)
            context?.startActivity(i)
        } catch (ex: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageID"))
            context?.startActivity(intent)
        }
    }

    // navigate
    fun navigate(context: Context?, lat: String?, lng: String?) {
        val url = "waze://?ll=$lat, $lng&navigate=yes"
        val intentWaze = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intentWaze.setPackage("com.waze");

        val uriGoogle = "google.navigation:q=$lat,$lng"
        val intentGoogleNav = Intent(Intent.ACTION_VIEW, Uri.parse(uriGoogle))
        intentGoogleNav.setPackage("com.google.android.apps.maps");

        val title = "Navigate via:"
        val chooserIntent = Intent.createChooser(intentGoogleNav, title)
        val arr = arrayOfNulls<Intent>(1)
        arr[0] = intentWaze
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arr)
        context?.startActivity(chooserIntent)
    }

    // share text to external apps
    fun sharePlainTxt(context: Context?, msgStr: String?, shareLabel: String) {
        val sendIntent = Intent(Intent.ACTION_SEND)

        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, msgStr)

        context?.startActivity(Intent.createChooser(sendIntent, shareLabel))
    }
}