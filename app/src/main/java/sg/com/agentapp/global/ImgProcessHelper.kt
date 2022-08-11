package sg.com.agentapp.global

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.util.Base64
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File


class ImgProcessHelper {
    // compress img using library
    fun compressImg(
            context: Context,
            oriImgFile: File,
            type: Int,
            maxReso: Int,
            quality: Int
    ): File? {
        // check if file exists
        if (oriImgFile.exists()) {

            // get ori img sizes
            val oriImgSize = getImgSize(oriImgFile)
            val oriWidth = oriImgSize.outWidth
            val oriHeight = oriImgSize.outHeight

            // check if need compress first
            if (needCompress(oriWidth, oriHeight, maxReso)) {
                // set different width/height for hori/verti
                lateinit var compFile: File
                if (oriWidth > oriHeight) { // hori: height = maxReso
                    compFile = Compressor(context)
                            .setMaxHeight(maxReso)
                            .setQuality(quality)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setDestinationDirectoryPath(getOutputPath(type))
                            .compressToFile(oriImgFile)

                } else { // verti: width = maxReso
                    compFile = Compressor(context)
                            .setMaxWidth(maxReso)
                            .setQuality(quality)
                            .setCompressFormat(Bitmap.CompressFormat.JPEG)
                            .setDestinationDirectoryPath(getOutputPath(type))
                            .compressToFile(oriImgFile)
                }

                return compFile

            } else { // no need compress, return oriImgFile

                return oriImgFile
            }
        } else {
            return null
        }
    }

    // get img size
    private fun getImgSize(file: File): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)

        return options
    }

    // check if need compress
    private fun needCompress(width: Int, height: Int, maxReso: Int): Boolean {
        if (width > height) { // hori - height 1080
            return height > maxReso
        } else { // verti - width 1080
            return width > maxReso
        }
    }

    // get output path
    private fun getOutputPath(type: Int): String {
        when (type) {
            1 -> { // outgoing compressed img
                return GlobalVars.IMAGES_PATH_SENT
            }

            2 -> { // profile img
                return GlobalVars.IMAGE_PATH_PROFILE
            }

            else -> {
                return GlobalVars.IMAGES_PATH_SENT
            }
        }
    }

    // create base64 str from img (not thumb)
    fun createBase64FromImg(filePath: String, quality: Int): String? {
        if (File(filePath).exists()) {

            val bitmap = BitmapFactory.decodeFile(filePath)

            // get bytes from bitmap
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // endcode bytes to base64 string
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        } else {
            return null
        }
    }

    // create base64 thumbnail str
    fun createBase64Thumb(filePath: String, quality: Int): String? {
        // check if file exists first
        if (File(filePath).exists()) {
            // get bitmap thumb from file
            var bitmap = BitmapFactory.decodeFile(filePath)
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 100, 100)

            // get bytes from bitmap
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // endcode bytes to base64 string
            return Base64.encodeToString(byteArray, Base64.DEFAULT)

        } else {
            return null
        }
    }
}