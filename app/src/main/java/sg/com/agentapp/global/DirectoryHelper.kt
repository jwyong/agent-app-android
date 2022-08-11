package sg.com.agentapp.global

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*



class DirectoryHelper {
    private val TAG = "JAY"

    // check if directory exists
    private fun checkAndCreateFolder(type: Int) {
        lateinit var newFolder: File

        // need to use non-concatenated string for path when creating dirs
        when (type) {
            0 -> { // outgoing temp (chat camera) - appDir/imgesDir/sentDir/.nomedia
                newFolder = File(GlobalVars.IMAGES_PATH_SENT, ".nomedia")
            }

            20 -> { // outgoing compressed img
                newFolder = File(GlobalVars.IMAGES_PATH_SENT, ".nomedia")
            }

            21 -> { // incoming img - appDir/imgDir/
//                newFolder = File(GlobalVars.APP_DIR, File(GlobalVars.IMGES_DIR, ".nomedia"))

                newFolder = File(GlobalVars.IMAGES_PATH)
            }

            22 -> { // outgoing audio
                newFolder = File(GlobalVars.AUDIO_PATH_SENT, ".nomedia")
            }

            23 -> { // incoming audio
                newFolder = File(GlobalVars.AUDIO_PATH)
            }

            24 -> { // outgoing doc
                newFolder = File(GlobalVars.DOCS_PATH_SENT, ".nomedia")
            }

            25 -> { // incoming doc
                newFolder = File(GlobalVars.DOCS_PATH)
            }
        }

        // create dir if exist
        if (!newFolder.exists()) {
            val dirs = newFolder.mkdirs()
        }
    }

    // save file to path (check if directory created first
    @Synchronized
    fun saveFile(context: Context, type: Int, fileFormatOrFullName: String): File? {
        // no permissions means return false/null
        if (!checkPerm(context)) {
            // return null if no permission
            return null
        }

        // check if dir exists first
        checkAndCreateFolder(type)

        // save file to path based on type
        val timeStamp: String = SimpleDateFormat(GlobalVars.DEFAULT_FILE_DATE_FORMAT, Locale.getDefault()).format(Date())

        lateinit var file: File
        when (type) {
            0 -> { // outgoing temp (chat camera)
                return File(GlobalVars.IMAGES_PATH_SENT, GlobalVars.TEMP_IMG_NAME)
            }

            20 -> { // outgoing compressed img - nomedia
                file = File(GlobalVars.IMAGES_PATH_SENT, "IMG_$timeStamp.$fileFormatOrFullName")
            }

            21 -> { // incoming img
                file = File(GlobalVars.IMAGES_PATH, "IMG_$timeStamp.$fileFormatOrFullName")
            }

            22 -> { // outgoing audio
                file = File(GlobalVars.AUDIO_PATH_SENT, "AUD_$timeStamp.$fileFormatOrFullName")
            }

            23 -> { // incoming audio
                file = File(GlobalVars.AUDIO_PATH, "AUD_$timeStamp.$fileFormatOrFullName")
            }

            24 -> { // outgoing doc
                file = File(GlobalVars.DOCS_PATH_SENT, fileFormatOrFullName)
            }

            25 -> { // incoming doc
                file = File(GlobalVars.DOCS_PATH, fileFormatOrFullName)
            }
            else -> {
                return null
            }
        }

        // check if file already exists
        return checkFileExist(context, type, fileFormatOrFullName, file)
    }

    // create new file if already exists
    private fun checkFileExist(context: Context, type: Int, fileFormatOrFullName: String, file: File): File? {
        // no need check for docs since same name
        when (type) {
            24, 25 -> return file
        }

        if (file.exists()) {
            return saveFile(context, type, fileFormatOrFullName)

        } else {
            return file
        }
    }

    // check storage permissions
    private fun checkPerm(context: Context): Boolean {
        val permStrArr = arrayOf(
//                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (PermissionHelper().hasPermissions(context, *permStrArr)) {
            return true
        } else {
            // toast permission msg
            Log.d(TAG,"NO WRITE PERM")
//            MiscHelper().toastMsg(context, R.string.perm_write, Toast.LENGTH_SHORT)

            return false
        }
    }
}