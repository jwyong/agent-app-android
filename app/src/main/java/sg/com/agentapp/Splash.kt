package sg.com.agentapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.api.api_models.BasicResponseModel
import sg.com.agentapp.api.api_models.SendFcmToken
import sg.com.agentapp.global.Preferences
import sg.com.agentapp.home.HomeActivity
import sg.com.agentapp.registration.ActivityRegistrationActivity

class Splash : AppCompatActivity() {

    private val send_btn: TextView? = null


    lateinit var intent2: Intent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Preferences.getInstance().userXMPPJid != null && !Preferences.getInstance().userXMPPJid.isEmpty()) {
            intent2 = Intent(this, HomeActivity::class.java)
        } else {
            intent2 = Intent(this, ActivityRegistrationActivity::class.java)
        }
        startActivity(intent2)

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->

            Log.d("smack", "fcm getinstance")

            RetroAPIClient.api.sendFcmToken(SendFcmToken(token = instanceIdResult.token),
                    """Bearer ${Preferences.getInstance().accessToken}"""
            ).enqueue(object : Callback<BasicResponseModel?> {
                override fun onFailure(call: Call<BasicResponseModel?>, t: Throwable) {
                    Log.e("wtf fcm failed", "$localClassName: ", t)
                }

                override fun onResponse(call: Call<BasicResponseModel?>, response: Response<BasicResponseModel?>) {
                    val res = response.body()
                    if (!response.isSuccessful) {
//                        var err = response.errorBody()?.string()

                        Log.i("wtf", "send:${response.errorBody()?.string()} ")
                        Log.i("wtf", "send:${res?.message} ")

                        return
                    }

                    if (res?.success.equals("ok")) {

                        Log.i("wtf", "fcm token sent: ${instanceIdResult.token}")
                    } else {
                        Log.i("wtf", "splash: is token sent?")
                    }

                }
            })

            Log.d("wtf", "token" + instanceIdResult.token + "]")
//            Toast.makeText(this, "token" + instanceIdResult.token, Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    override fun onResume() {

        super.onResume()

        Log.d("wtf", "onResume: ")
    }

}