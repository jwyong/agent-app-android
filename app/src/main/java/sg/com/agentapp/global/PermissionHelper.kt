package sg.com.agentapp.global

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import sg.com.agentapp.R


class PermissionHelper {
    // check if got permissions based on array of permissions strings
    fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    // show dialogs asking for permissions, then ask based on user's input
    fun askForPermissions(context: Context, fragment: Fragment?, permissions: Array<String>, requestCode: Int, permPopupBody: String, negativeAction: Runnable?): Dialog {
        var dialog: Dialog? = null

        val positiveAction = Runnable {
            if (fragment != null) { // is fragment
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    fragment.requestPermissions(permissions, requestCode)
                } else {
                    Log.e(GlobalVars.TAG1, "PermissionHelper, askForPermissions: API 21")
                }
            } else {
                ActivityCompat.requestPermissions(context as Activity, permissions, requestCode)
            }
            dialog?.dismiss()
        }

        dialog = UIHelper().dialog2btn(context,
                permPopupBody,
                context.getString(R.string.ok),
                context.getString(R.string.cancel),
                positiveAction,
                negativeAction,
                false)

        return dialog
    }

    // process permission results
    fun onPermResults(context: Context, grantResults: IntArray, permDeniedPopupBodyInt: Int, permGrantedFunc: Runnable?) {
        // show "permissions settings" dialog if got false
        if (grantResults.contains(PackageManager.PERMISSION_DENIED)) { // got permission denied, show dialog
            Log.d(GlobalVars.TAG1, "PermissionHelper, onPermResults: denied")

            val positiveAction = Runnable {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", (context as Activity).packageName, null)
                intent.data = uri

                context.startActivity(intent)
            }

            UIHelper().dialog2btn(
                    context,
                    context.getString(permDeniedPopupBodyInt),
                    context.getString(R.string.settings),
                            context.getString(R.string.cancel),
                    positiveAction,
                    null,
                    false
            )

        } else { // permission granted, run function
            Log.d(GlobalVars.TAG1, "PermissionHelper, onPermResults: granted")

            permGrantedFunc?.run()
        }
    }
}