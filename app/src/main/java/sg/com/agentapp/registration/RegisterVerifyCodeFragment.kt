package sg.com.agentapp.registration

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sg.com.agentapp.BuildConfig
import sg.com.agentapp.R
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.api.api_models.RegisterFcmModel
import sg.com.agentapp.databinding.RegisterVerifyCodeBinding
import sg.com.agentapp.global.*
import sg.com.agentapp.global.GlobalVars.PREF_ACCESS_TOKEN
import sg.com.agentapp.global.GlobalVars.PREF_ACTIVE_DATE
import sg.com.agentapp.global.GlobalVars.PREF_CEA_NO
import sg.com.agentapp.global.GlobalVars.PREF_EJABBERD_PASSWORD
import sg.com.agentapp.global.GlobalVars.PREF_ESTATE_AGENT
import sg.com.agentapp.global.GlobalVars.PREF_LICENSE_NO
import sg.com.agentapp.global.GlobalVars.PREF_NAME
import java.util.concurrent.TimeUnit

class RegisterVerifyCodeFragment : Fragment() {
    private val TAG = "JAY"

    private val miscHelper = MiscHelper()
    private val regCountdownTimer = RegCountdownTimer(60) // set countdown timer int here

    private lateinit var binding: RegisterVerifyCodeBinding

    var isNextBtnEnabled = ObservableBoolean()
    var isResendBtnEnabled = ObservableBoolean()
    var timerText = ObservableField<String>()
    var otpCodeText = ObservableField<String>()

    var phoneNumber: String? = null
    private var code: String? = null

    private val lmm = LinkMovementMethod.getInstance()

    private var deviceid: String? = MiscHelper.uniquePsuedoID

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.register_verify_code, container, false)
        binding.data = this

        // set observables
        isNextBtnEnabled.set(false)
        isResendBtnEnabled.set(false)
        timerText.set("${getString(R.string.otp_send_another)} in 60 seconds")

        // set for link
        binding.tvContactus.movementMethod = lmm

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // resend otp timer funcs
        setupTimerFuncs()

        // get phone number from intent and otp from sms
        getOTPFromSMS()

        // set next btn based on input code
        otpCodeText.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (otpCodeText.get()?.length == 6) { // got 6 digit code, click next
                    isNextBtnEnabled.set(true)
                } else {
                    isNextBtnEnabled.set(false)
                }
            }

        })
    }

    // onclick funcs
    fun nextBtn(_v: View) {
        val enteredCode = binding.edttxtOtpCode.text.toString()
        // TEMP - bypass registration if dev
        if (BuildConfig.DEBUG) {
            if (enteredCode.equals("111111")) {
                registerPhoneNumber()
            }
        }

        // for AgentApp account (phone number = +651234567891, OTP = )
        Log.d(GlobalVars.TAG1, "RegisterVerifyCodeFragment, nextBtn: phoneNumber = $phoneNumber")
        val agentAppPhone = "+651234567891"
        val agentAppOTP = "040979"
        if (phoneNumber.equals(agentAppPhone) && enteredCode.equals(agentAppOTP)) {
            registerPhoneNumber()
        }

        // ACTUAL - check otp code
        checkOtpCode(enteredCode)
    }

    fun resendOTPBtn(_v: View) {
        Log.d(TAG, "resend OTP")

        // start countdown timer
        regCountdownTimer.startCountdown()

        // do resend funcs
        getOTPFromSMS()
    }

    // auto get otp code from sms
    private fun getOTPFromSMS() {
        phoneNumber = arguments!!.getString("phoneNumber")

        if (phoneNumber != null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber!!, 10, TimeUnit.SECONDS, activity!!,
                    object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                            registerPhoneNumber()
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            Log.e(TAG, "onVerificationFailed: ", e)
                        }

                        override fun onCodeSent(s: String?, forceResendingToken: PhoneAuthProvider.ForceResendingToken?) {
                            super.onCodeSent(s, forceResendingToken)
                            Log.d(TAG, "onCodeSent() called with: s = [" + s + "]," +
                                    " forceResendingToken = [" + forceResendingToken + "]")
                            code = s
                        }

                        override fun onCodeAutoRetrievalTimeOut(s: String?) {
                            super.onCodeAutoRetrievalTimeOut(s)
                            Log.d(TAG, "onCodeAutoRetrievalTimeOut() called with: s = [$s]")
                        }
                    }
            )
        }
    }

    // funcs
    private fun setupTimerFuncs() {
        // observe countdown helper
        regCountdownTimer.obsTimerInt.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val obsTimerInt = regCountdownTimer.obsTimerInt.get()

                if (obsTimerInt == 0) { // send another otp
                    timerText.set(getString(R.string.otp_send_another_u))

                } else { // wait for timer
                    timerText.set("${getString(R.string.otp_send_another)} in ${regCountdownTimer.obsTimerInt.get()} seconds")

                }

            }
        })
        regCountdownTimer.obsIsBtnEnabled.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isResendBtnEnabled.set(regCountdownTimer.obsIsBtnEnabled.get())
            }
        })

        // start countdown timer
        regCountdownTimer.startCountdown()
    }

    // send otp code to server for checking
    private fun checkOtpCode(otpcode: String) {
        isNextBtnEnabled.set(false)

        if (otpcode.equals(code)) { // correct code, register phone number
            registerPhoneNumber()

        } else { // code invalid
            // re-enable btn
            isNextBtnEnabled.set(true)

            miscHelper.toastMsg(context, R.string.otp_invalid, Toast.LENGTH_SHORT)
        }
    }

    // register phone number
    private fun registerPhoneNumber() {
        val dialog = UIHelper().loadingDialog(activity!!, "Loading")
        dialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetroAPIClient.api.register(RegisterFcmModel(phoneNumber = phoneNumber, deviceId = deviceid)).execute()

                if (!response.isSuccessful) {
                    CoroutineScope(Dispatchers.Main).launch {
                        dialog.dismiss()

                        isNextBtnEnabled.set(true)

                        miscHelper.toastMsg(context, R.string.onresponse_unsuccessful, Toast.LENGTH_SHORT)
                    }

                    miscHelper.retroLogUnsuc(TAG, "RegVerifyCodeFrag registerPhoneNumber", response)
                } else {
                    val preferences = Preferences.getInstance()

                    val resBody = response.body()!!
                    if (resBody.profilePic != null) {
                        val pic = DownloadProfileFromUrl().getProfile(resBody.profilePic)
                        val encodeToString = Base64.encodeToString(pic, Base64.DEFAULT)
                        preferences.save(GlobalVars.PREF_USER_PROFILE, encodeToString)
                    }

                    preferences.save(PREF_ACCESS_TOKEN, resBody.accessToken)
                    preferences.save(PREF_EJABBERD_PASSWORD, resBody.ejabberdPassword)
                    preferences.save(PREF_CEA_NO, resBody.ceaNo)
                    preferences.save(PREF_NAME, resBody.name)
                    preferences.save(GlobalVars.PREF_PHONE, phoneNumber)
                    preferences.save(PREF_ESTATE_AGENT, resBody.estateAgent)
                    preferences.save(PREF_LICENSE_NO, resBody.licenseNo)
                    preferences.save(PREF_ACTIVE_DATE, resBody.activeDate)

                    // additional agent data
                    preferences.setuser_email(resBody.email ?: "")
                    preferences.setuser_designation(resBody.designation ?: "")
                    preferences.setdisplay_name(resBody.displayname ?: "")
                    preferences.setuser_website(resBody.websiteLink ?: "")

                    CoroutineScope(Dispatchers.Main).launch {
                        dialog.dismiss()

                        Navigation.findNavController(binding.root).navigate(R.id.action_registerVerifyCodeFragment_to_registerSuccessFragment)
                    }


                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "onFailure: ", e)

                dialog.dismiss()

                isNextBtnEnabled.set(true)

                miscHelper.toastMsg(context, R.string.onfailure, Toast.LENGTH_SHORT)

                miscHelper.retroLogFailure(TAG, "RegVerifyCodeFrag registerPhoneNumber", e)
            }
        }
    }

    override fun onDestroy() {
        regCountdownTimer.stopCountdown(true)

        super.onDestroy()
    }
}
