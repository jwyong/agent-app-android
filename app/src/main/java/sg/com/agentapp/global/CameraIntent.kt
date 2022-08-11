package sg.com.agentapp.global

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import sg.com.agentapp.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraIntent {
    var photoFile: File? = null

    // open camera - cancel if no permissions
    fun launchCamera(context: Context, fragment: Fragment?, requestCode: Int, type: Int, isFrontCamera: Boolean) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(context.packageManager)?.also {
                photoFile = try {
                    // ori cam file should always be saved as temp img
                    // temp img can later be used to create permanent ori file and compressed sent file
                    DirectoryHelper().saveFile(context, type, "jpg")

                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.e("JAY", "CameraIntent launchCamera failed ", ex)
                    null
                }

                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            context,
                            context.getString(R.string.app_fp_name),
                            it
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                    if (isFrontCamera) {
                        intent.putExtra(
                                "android.intent.extras.CAMERA_FACING",
                                android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
                        )
                        intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
                        intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                    } else {
                        intent.putExtra(
                                "android.intent.extras.CAMERA_FACING",
                                android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
                        )
                        intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 0)
                        intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", false)
                    }

                    if (fragment != null) {
                        fragment.startActivityForResult(intent, requestCode)

                    } else {
                        ActivityCompat.startActivityForResult(context as Activity, intent, requestCode, null)
                    }
                }
            }
        }
    }

    // camera activity results
    fun cameraActiResults(context: Context): File {
        // copy ori temp img to imges_path for permanent keeping - temp file is ALWAYS at imges_sent_path/temp.jpg
        var oriFile = File(GlobalVars.IMAGES_PATH_SENT, GlobalVars.TEMP_IMG_NAME)
        val timeStamp: String = SimpleDateFormat(GlobalVars.DEFAULT_FILE_DATE_FORMAT,
                Locale.getDefault()).format(Date())
        oriFile = oriFile.copyTo(File(GlobalVars.IMAGES_PATH, "IMG_$timeStamp.jpg"))

        // send broadcast to gallery
        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(oriFile)))

        return oriFile
    }
}

