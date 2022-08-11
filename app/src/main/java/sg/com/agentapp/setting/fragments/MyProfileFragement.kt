package sg.com.agentapp.setting.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import sg.com.agentapp.R
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.databinding.MyprofileBinding
import sg.com.agentapp.global.*
import java.io.File

class MyProfileFragement : Fragment() {
    lateinit var binding: MyprofileBinding

    // helpers
    private val uiHelper = UIHelper()
    private val cameraIntent = CameraIntent()
    private val galleryIntent = GalleryIntent()
    private val permissionHelper = PermissionHelper()
    private val imgProcessHelper = ImgProcessHelper()
    private val miscHelper = MiscHelper()

    // constant databinding vals
    val name = ObservableField(Preferences.getInstance().agentName)
    val cea = ObservableField(Preferences.getInstance().getValue(GlobalVars.PREF_CEA_NO))
    val mobile = ObservableField(Preferences.getInstance().getValue(GlobalVars.PREF_PHONE))
    val agency = ObservableField(Preferences.getInstance().getValue(GlobalVars.PREF_ESTATE_AGENT))
    val licenseNo = ObservableField(Preferences.getInstance().getValue(GlobalVars.PREF_LICENSE_NO))

    // observable vars
    val obsDisplayName = ObservableField("-")
    val obsEmail = ObservableField("-")
    val obsWebsite = ObservableField("-")
    val obsDesignation = ObservableField("-")
    val obsActiveDate = ObservableField("-")

    // for profile img
    val obsProfileImg = ObservableField<String>()
    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2
    private var profileImgFile: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.myprofile, container, false)
        binding.data = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        obsDisplayName.set(Preferences.getInstance().displaY_NAME)
        obsEmail.set(Preferences.getInstance().useR_EMAIL)
        obsWebsite.set(Preferences.getInstance().useR_WEBSITE)
        obsDesignation.set(Preferences.getInstance().useR_DESIGNATION)
        obsProfileImg.set(Preferences.getInstance().getValue(GlobalVars.PREF_USER_PROFILE))

    }

    //===== runnables
    private val takePhotoFunc = Runnable {
        // check camera permissions
        if (checkPerms(CAMERA_REQUEST_CODE)) {
            // launch camera if already got permissions
            cameraIntent.launchCamera(context!!, this, CAMERA_REQUEST_CODE, 0, true)
        }
    }

    private val openGalleryFunc = Runnable {
        // check camera permissions
        if (checkPerms(GALLERY_REQUEST_CODE)) {
            // launch gallery if already got permissions
            galleryIntent.launchGallery(context!!, this, GALLERY_REQUEST_CODE)
        }
    }

    //===== normal funcs
    // check perms func
    private fun checkPerms(permReqCode: Int): Boolean {
        lateinit var permStrArr: Array<String>
        var permStr = R.string.perm_camera

        when (permReqCode) {
            CAMERA_REQUEST_CODE -> {
                permStrArr = arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                permStr = R.string.perm_camera
            }

            GALLERY_REQUEST_CODE -> {
                permStrArr = arrayOf(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                permStr = R.string.perm_gallery
            }
        }

        if (!permissionHelper.hasPermissions(context!!, *permStrArr)) { // no perm
            permissionHelper.askForPermissions(context!!, this, permStrArr, permReqCode, getString(permStr),
                    null)

            return false

        } else {
            return true
        }
    }

    //===== databinding funcs
    fun profileImgOnClick(v: View) {
        uiHelper.btmDialog2Items(context!!, R.string.camera, R.string.gallery, takePhotoFunc, openGalleryFunc, true)
    }

    fun displayNameOnClick(_v: View) {
        uiHelper.dialog2Btns1EditTxt(context, R.string.et_dname_title, R.string.profile_dname, obsDisplayName, null, true)
    }

    fun emailOnClick(_v: View) {
        uiHelper.dialog2Btns1EditTxt(context, R.string.et_email_title, R.string.profile_email, obsEmail, null, true)
    }

    fun websiteOnClick(_v: View) {
        uiHelper.dialog2Btns1EditTxt(context, R.string.et_website_title, R.string.profile_website, obsWebsite, null, true)
    }

    fun designationOnClick(_v: View) {
        uiHelper.dialog2Btns1EditTxt(context, R.string.et_designation_title, R.string.profile_designation, obsDesignation, null, true)
    }

    //===== results
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d(GlobalVars.TAG1, "MyProfileFragement, onRequestPermissionsResult: ")

        // prep vars based on requestCode
        var deniedPopupMsg = 0
        var grantedFun = Runnable {}
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                deniedPopupMsg = R.string.perm_failed
                grantedFun = takePhotoFunc
            }

            GALLERY_REQUEST_CODE -> {
                deniedPopupMsg = R.string.perm_failed
                grantedFun = openGalleryFunc
            }
        }

        // show "open settings" popup if denied
        permissionHelper.onPermResults(
                context!!,
                grantResults,
                deniedPopupMsg,
                grantedFun
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val file: File?
                when (requestCode) {
                    CAMERA_REQUEST_CODE -> {
                        file = File(GlobalVars.IMAGES_PATH_SENT, GlobalVars.TEMP_IMG_NAME)
                    }

                    GALLERY_REQUEST_CODE -> {
                        file = galleryIntent.galleryImgActiResults(context!!, data)
                    }

                    else -> {
                        file = File(GlobalVars.IMAGES_PATH_SENT, GlobalVars.TEMP_IMG_NAME)
                    }
                }

                if (file != null) {
                    // compress and save profile img, then set path to obs
                    profileImgFile = imgProcessHelper.compressImg(context!!, file, 2, 720, 50) // 2 = profile

                    // get base64 str from compressed file
                    if (profileImgFile != null) {
                        val compFilePath = profileImgFile?.absolutePath

                        // get base64 from file
                        val compByteStr = imgProcessHelper.createBase64FromImg(compFilePath?: "", 75)

                        if (compByteStr != null) {
                            // save img in pref
                            Preferences.getInstance().save(GlobalVars.PREF_USER_PROFILE, compByteStr)
                            obsProfileImg.set(compByteStr)
                        }
                    }

                } else { // intent problem
                    miscHelper.toastMsgInt(context!!, R.string.something_went_wrong, Toast.LENGTH_SHORT)
                }

            }
        }
    }

    fun submitUpdate(v: View) {
        val dialog = UIHelper().loadingDialog(activity!!, "Loading")
        dialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            val header = "Bearer ${Preferences.getInstance().accessToken}"

            // check if got change profileImg
            val fileReqBody: RequestBody
            val gotFileStr: String
            if (profileImgFile == null) {
                fileReqBody = RequestBody.create(MediaType.parse("image/*"), "")
                gotFileStr = "false"
            } else {
                fileReqBody = RequestBody.create(MediaType.parse("image/*"), profileImgFile!!)
                gotFileStr = "true"
            }
            val file = MultipartBody.Part.createFormData("file", "${Preferences.getInstance().userXMPPJid.toUpperCase()}.jpg", fileReqBody)
            val gotFile = MultipartBody.Part.createFormData("got_file", gotFileStr)

            val dispName = MultipartBody.Part.createFormData("display_name", obsDisplayName.get()
                    ?: "")
            val emailAddr = MultipartBody.Part.createFormData("user_email", obsEmail.get() ?: "")
            val websiteAddr = MultipartBody.Part.createFormData("user_website", obsWebsite.get()
                    ?: "")
            val userDesignation = MultipartBody.Part.createFormData("user_designation", obsDesignation.get()
                    ?: "")

            try {
                val resp = RetroAPIClient.api.updateUserProfile(header, file, gotFile, dispName, emailAddr, websiteAddr, userDesignation).execute()

                if (resp.isSuccessful) {
                    Log.d(GlobalVars.I_LOG, "MyProfileFragement submitUpdate:  success")

                    Preferences.getInstance().setdisplay_name(obsDisplayName.get() ?: "")
                    Preferences.getInstance().setuser_email(obsEmail.get() ?: "")
                    Preferences.getInstance().setuser_website(obsWebsite.get() ?: "")
                    Preferences.getInstance().setuser_designation(obsDesignation.get() ?: "")

                    CoroutineScope(Dispatchers.Main).launch {
                        dialog.dismiss()
                        Navigation.findNavController(binding.root).navigateUp()
                    }

                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        dialog.dismiss()
                        miscHelper.toastMsg(context, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT)
                    }
                    Log.d(GlobalVars.I_LOG, "MyProfileFragement submitUpdate:  not success")
                }

            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    dialog.dismiss()
                    miscHelper.toastMsg(context, R.string.onfailure, Toast.LENGTH_SHORT)
                }
                Log.d(GlobalVars.I_LOG, "MyProfileFragement submitUpdate:  failure")
                e.printStackTrace()
            }
        }

    }
}
