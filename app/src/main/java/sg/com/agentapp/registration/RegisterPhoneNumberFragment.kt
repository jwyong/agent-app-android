package sg.com.agentapp.registration

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sg.com.agentapp.R
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.api.api_models.ApiCheckPhone
import sg.com.agentapp.databinding.RegisterPhoneNumberBinding
import sg.com.agentapp.global.MiscHelper


class RegisterPhoneNumberFragment : Fragment() {
    private val TAG = "JAY"

    private val miscHelper = MiscHelper()

    private lateinit var binding: RegisterPhoneNumberBinding

    var showErrorMsg = ObservableInt()
    var showContactUs = ObservableBoolean()
    var isBtnEnabled = ObservableBoolean()

    private val lmm = LinkMovementMethod.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.register_phone_number, container, false)
        binding.data = this

        // set observable values
        showErrorMsg.set(0)
        showContactUs.set(true)
        isBtnEnabled.set(true)

        binding.executePendingBindings()

        // set link clickable
        binding.tvContactUs.movementMethod = lmm
        binding.tvTerms.movementMethod = lmm
        binding.tvAgentNotFound.movementMethod = lmm

        return binding.root
    }

    //===== onclick funcs
    fun sendOTPBtn(_v: View) {
        // disable btn
        isBtnEnabled.set(false)

        checkPhoneNumber(binding.edttxtMobileNo.text.toString())
    }


    // functions
    private fun checkPhoneNumber(phoneNumber: String) {
        if (phoneNumber.length < 6) { // invalid number
            isBtnEnabled.set(true) // re-enable btn
            showErrorMsg.set(1) // show error msg
            showContactUs.set(true) // show contact us

        } else { // post
            isBtnEnabled.set(false)
            showErrorMsg.set(0)
            showContactUs.set(true)

            // set bundle first
            val b = Bundle()
            b.putString("phoneNumber", phoneNumber)

            val api = RetroAPIClient.api
            api.checkPhone(ApiCheckPhone(phoneNumber, "")).enqueue(object : Callback<ApiCheckPhone> {
                override fun onResponse(call: Call<ApiCheckPhone>, response: Response<ApiCheckPhone>) {
                    if (!response.isSuccessful) {

                        miscHelper.retroLogUnsuc(TAG, "RegisterPhoneNumberFragment checkPhoneNumber", response)

                        isBtnEnabled.set(true)
                        showErrorMsg.set(2)
                        showContactUs.set(false)

                        return
                    }

                    // navigate to next screen
                    Navigation.findNavController(view!!).navigate(R.id.action_registerPhoneNumberFragment_to_registerVerifyCodeFragment, b)
                }

                override fun onFailure(call: Call<ApiCheckPhone>, t: Throwable) {
                    miscHelper.toastMsg(context, R.string.onfailure, Toast.LENGTH_SHORT)

                    isBtnEnabled.set(true)
                    showErrorMsg.set(0)
                    showContactUs.set(true)
                }
            })
        }
    }
}
