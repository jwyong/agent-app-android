package sg.com.agentapp.registration


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.firebase.iid.FirebaseInstanceId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sg.com.agentapp.R
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.api.api_models.BasicResponseModel
import sg.com.agentapp.api.api_models.SendFcmToken
import sg.com.agentapp.databinding.RegisterSuccessBinding
import sg.com.agentapp.global.InternetHelper
import sg.com.agentapp.global.MiscHelper
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.home.HomeActivity
import sg.com.agentapp.setting.SettingActivity

class RegisterSuccessFragment : Fragment() {
    private val TAG = "JAY"
    private lateinit var binding: RegisterSuccessBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.register_success, container, false)
        binding.data = this

        // init smack first after registration successful
        InternetHelper.checkForegrndInt(false, true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendFcmToken()

    }

    // onclick funcs
    fun updateProfileBtn(_v: View) {
        // start home acti first
        startActivity(Intent(context, HomeActivity::class.java))

        // then start settings acti and go to profile frag
        val intent = Intent(context, SettingActivity::class.java)
        intent.putExtra("fragActionID", R.id.action_settingFragment_to_myProfileFragement2)
        startActivity(intent)

        // finish reg acti
        activity?.finish()
    }

    fun skipBtn(_v: View) {
        startActivity(Intent(context, HomeActivity::class.java))

        activity?.finish()
    }

    private fun sendFcmToken() {
        val miscHelper = MiscHelper()

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            RetroAPIClient.api.sendFcmToken(SendFcmToken(token = instanceIdResult.token),
                    """Bearer ${Preferences.getInstance().accessToken}"""
            ).enqueue(object : Callback<BasicResponseModel?> {
                override fun onFailure(call: Call<BasicResponseModel?>, t: Throwable) {
                    miscHelper.retroLogFailure(TAG, "RegSucFrag sendFcmToken", t)
                }

                override fun onResponse(call: Call<BasicResponseModel?>, response: Response<BasicResponseModel?>) {
                    val res = response.body()
                    if (!response.isSuccessful) {
                        miscHelper.retroLogUnsuc(TAG, "RegSucFrag sendFcmToken", response)

                        return
                    }

                    if (res?.success.equals("ok")) {
                        Log.i("wtf", "fcm token sent: ")
                    } else {
                        Log.i("wtf", "splash: is token sent?")
                    }

                }
            })
        }
    }
}
