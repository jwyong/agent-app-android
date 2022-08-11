package sg.com.agentapp.global

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.streams.asSequence


class MiscHelper {

    fun retroLogUnsuc(TAG: String, funcName: String, response: Response<*>) {
        try {
            val errorMsg = "retroUnsuc: $funcName, errorMsg: ${response.errorBody()?.string()}"
            Log.e(TAG, errorMsg)

        } catch (ignore: IOException) {
        }

    }

    //log response failure + send to crashlytics
    fun retroLogFailure(TAG: String, funcName: String, throwable: Throwable) {
        val errorMsg = "retroFailed: $funcName"
        Log.e(TAG, errorMsg, throwable)

    }

    fun getScreenWidthAndHeight(activity: Activity): Point {
        val outPoint = Point()
        val display = activity.windowManager.defaultDisplay
        display.getSize(outPoint)
        return outPoint
    }

    fun getTitleTextSize(context: Context): Int {
        val deviceDensity = context.resources.displayMetrics.density
        return (60 / deviceDensity).toInt()
        //        if (DEVICEDENSITY > 2.6) {
        //            return 22;
        //        } else {
        //            return 20;
        //        }
    }

    companion object {
        /**
         * Return pseudo unique ID
         *
         * @return ID
         */
        // If all else fails, if the user does have lower than API 9 (lower
        // than Gingerbread), has reset their device or 'Secure.ANDROID_ID'
        // returns 'null', then simply the ID returned will be solely based
        // off their Android device information. This is where the collisions
        // can happen.
        // Thanks http://www.pocketmagic.net/?p=1662!
        // Try not to use DISPLAY, HOST or ID - these items could change.
        // If there are collisions, there will be overlapping data
        // Thanks to @Roman SL!
        // https://stackoverflow.com/a/4789483/950427
        // Only devices with API >= 9 have android.os.Build.SERIAL
        // http://developer.android.com/reference/android/os/Build.html#SERIAL
        // If a user upgrades software or roots their device, there will be a duplicate entry
        // Go ahead and return the serial for api => 9
        // String needs to be initialized
        // some value
        // Thanks @Joe!
        // https://stackoverflow.com/a/2853253/950427
        // Finally, combine the values we have found by using the UUID class to create a unique identifier
        val uniquePsuedoID: String
            get() {
                val m_szDevIDShort = "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 + Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10
                var serial: String? = null
                try {
                    serial = android.os.Build::class.java.getField("SERIAL").get(null).toString()
                    return UUID(m_szDevIDShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
                } catch (exception: Exception) {
                    serial = "serial"
                }

                return UUID(m_szDevIDShort.hashCode().toLong(), serial!!.hashCode().toLong()).toString()
            }
    }

    fun getRandomString(length: Long): String {
        val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return java.util.Random().ints(length, 0, source.length)
                    .asSequence()
                    .map(source::get)
                    .joinToString("")

        } else return "NA"
    }

    fun toastMsg(context: Context?, stringID: Int, duration: Int) {
        if (context == null) {
            return
        }

        (context as Activity).runOnUiThread {
            Toast.makeText(context, stringID, duration).show()
        }
    }

    fun toastMsgInt(context: Context?, stringID: Int, duration: Int) {
        if (context == null) {
            return
        }

        (context as Activity).runOnUiThread {
            Toast.makeText(context, stringID, duration).show()
        }
    }

    // open link in web browser
    fun openURLBrowser(context: Context, uriString: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uriString)
        startActivity(context, intent, null)

    }

    fun getHumanTimeFormatFromMilliseconds(millisecondS: Long): String {
        var message = ""
        val milliseconds = millisecondS
        val seconds = (milliseconds / 1000).toInt() % 60
        val minutes = (milliseconds / (1000 * 60) % 60).toInt()
        if (milliseconds >= 1000) {
//            val seconds = (milliseconds / 1000).toInt() % 60
//            val minutes = (milliseconds / (1000 * 60) % 60).toInt()
            val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()
            val days = (milliseconds / (1000 * 60 * 60 * 24)).toInt()
            if (days == 0 && hours != 0) {
                message = String.format("%d hours %d minutes %d seconds", hours, minutes, seconds)
            } else if (hours == 0 && minutes != 0) {
                message = String.format("%d minutes %d seconds", minutes, seconds)
            } else if (days == 0 && hours == 0 && minutes == 0) {
                message = String.format("%d seconds ago", seconds)
            } else {
                message = String.format("%d days %d hours %d minutes %d seconds", days, hours, minutes, seconds)
            }
        } else {
            message = "- $minutes min $seconds sec"
        }
        return message
    }

}
