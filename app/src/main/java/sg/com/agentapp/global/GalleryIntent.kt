package sg.com.agentapp.global

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import sg.com.agentapp.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

class GalleryIntent {
    private val TAG = "JAY"

    // open camera - cancel if no permissions
    fun launchGallery(context: Context, fragment: Fragment?, requestCode: Int) {
        lateinit var intent: Intent

        // set gallery file type based on type
        when (requestCode) {
            2 -> { // img
                intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
            }

            3 -> { // docs
                intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"

                // set mimetypes (imges, pdf, word, excel, ppt, access only)
                val mimeTypes = arrayOf(
                        "image/*", // imges

                        // pdf
                        "application/pdf", // .pdf

                        // word
                        "application/msword", // .doc
                        "application/vnd.ms-word.document.macroenabled.12", // .docm
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx

                        // excel
                        "application/vnd.ms-excel", // .xls
                        "application/vnd.ms-excel.addin.macroenabled.12", // .xlam
                        "application/vnd.ms-excel.sheet.binary.macroenabled.12", // .xlsb
                        "application/vnd.ms-excel.template.macroenabled.12", // .xltm
                        "application/vnd.ms-excel.sheet.macroenabled.12", // .xlsm

                        // ppt
                        "application/vnd.ms-powerpoint", // .ppt
                        "application/vnd.ms-powerpoint.addin.macroenabled.12", // .ppam
                        "application/vnd.ms-powerpoint.slide.macroenabled.12", // .sldm
                        "application/vnd.ms-powerpoint.presentation.macroenabled.12", // .pptm
                        "application/vnd.ms-powerpoint.slideshow.macroenabled.12", // .ppsm
                        "application/vnd.ms-powerpoint.template.macroenabled.12", // .potm

                        // access
                        "application/x-msaccess" // .mdb
                )
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
        }

        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode)

        } else {
            startActivityForResult(context as Activity, intent, requestCode, null)

        }
    }

    // gallery activity results (img)
    fun galleryImgActiResults(context: Context, data: Intent?): File? {
        Log.d(TAG, "img gallery data = ${data}")

        val fileUri = data?.data
        var oriFile: File? = null

        // only continue if file uri is not null
        if (fileUri != null) {
            val filePath = getFilePathFromUri(context, fileUri)

            // get file based on uri
            if (filePath != null) {
                oriFile = File(filePath)

                Log.d(TAG, "1 path = $filePath")

            } else {
                oriFile = File(fileUri.path)

                Log.d(TAG, "2 path = ${fileUri.path}")
            }
        }

        return oriFile
    }

    // gallery (docs) activity results
    fun galleryDocsActiResults(context: Context, data: Intent?): File? {
        Log.d(TAG, "file picker data = ${data}")
        val oriFileUri = data?.data

        // check scheme (content or file)
        var outFile: File? = null
        when (data?.scheme) {
            "content" -> { // content - get file via input stream
                // get file and fileName
                val oriFileName = DocumentFile.fromSingleUri(context, oriFileUri!!)?.name
                Log.d(TAG, "content fileName = $oriFileName")

                // prepare input stream
                val contentResolver = context.contentResolver

                try {
                    val inputStream = contentResolver.openInputStream(oriFileUri)

                    // prepare output stream based on sent file path
                    outFile = DirectoryHelper().saveFile(context, 24, oriFileName!!)

                    Log.d(TAG, "outFile = ${outFile?.absolutePath}")
                    val outputStream = FileOutputStream(outFile)

                    // copy input stream to output stream
                    inputStream.copyTo(outputStream, 1024)
                } catch (e: FileNotFoundException) {
                    MiscHelper().toastMsg(context, R.string.file_not_exist, Toast.LENGTH_SHORT)
                }
            }

            "file" -> {
                Log.d(TAG, "file filePath = ${oriFileUri?.path}")

                val oriFile = File(oriFileUri?.path)
                val oriFileName = oriFile.name

                outFile = DirectoryHelper().saveFile(context, 24, oriFileName)

                // only need copy file if doesn't already exist
//                if (!outFile!!.exists()) {
                oriFile.copyTo(outFile!!)
//                }
            }
        }

        Log.d(TAG, "filePath = ${outFile?.absolutePath}")

        return outFile
    }

    // get file path from gallery picker
    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null)

        var filePath: String? = null
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            filePath = cursor.getString(columnIndex)
        }
        cursor?.close()

        return filePath
    }

}

