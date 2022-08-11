package sg.com.agentapp.find_agent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.find_agent_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sg.com.agentapp.R
import sg.com.agentapp.api.api_clients.RetroAPIClient
import sg.com.agentapp.api.api_models.FindAgentModel
import sg.com.agentapp.global.BaseActivity
import sg.com.agentapp.global.BtnActionHelper
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.MiscHelper

class FindAgent : BaseActivity() {
    // helpers
    private val btnActionHelper = BtnActionHelper()
    private val miscHelper = MiscHelper()

    val adapter = FindAgentAdapter()
    val obsPhoneNumber = ObservableField<String>()

    val NEW_APPT_ACTI_CODE = 1

    private lateinit var binding: sg.com.agentapp.databinding.FindAgentMainBinding
    val searchNum = ObservableInt(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.find_agent_main)
        binding.vm = this

        //setup back btn
        setupToolbar()

        // set contact us link
        binding.tvContactUs.movementMethod = LinkMovementMethod.getInstance()


        // set bg based on search results
        searchNum.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (searchNum.get() == 0) {
                    // no search results, set mid bg
                    setGradientBG(0)

                } else {
                    // got results, set big bg without btm
                    setGradientBG(3)

                }
            }

        })

        //setup adapter for recyclerview
        binding.recyclerView.adapter = adapter
//        subscribeUi(adapter)
        //data
        getDataService("")
        searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                getDataService(s.toString())
            }
        })
    }

    //===== databinding funcs
    fun callbtn(_v: View) {
        btnActionHelper.callPhone(this, obsPhoneNumber.get())
    }

    fun msgbtn(_v: View) {
        btnActionHelper.sendSms(this, obsPhoneNumber.get(), null)
    }

    fun whatsAppbtn(_v: View) {
        val phone = "65${obsPhoneNumber.get()}"
        btnActionHelper.sendWhatsapp(this, phone)
    }

    //===== normal funcs
    fun getDataService(text: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("search_string", text)

        val api = RetroAPIClient.api

        api.getAgent(jsonObject).enqueue(object : Callback<List<FindAgentModel>?> {
            override fun onFailure(call: Call<List<FindAgentModel>?>, t: Throwable) {
                // temp - failure when no search results
                searchNum.set(0)

                miscHelper.retroLogFailure(GlobalVars.TAG1, "getDataService", t)
            }

            override fun onResponse(call: Call<List<FindAgentModel>?>, response: Response<List<FindAgentModel>?>) {
                if (!response.isSuccessful) {
                    miscHelper.retroLogUnsuc(GlobalVars.TAG1, "getDataService", response)
                }

                val resp = response.body()

                if (resp != null) {
                    searchNum.set(resp.size)
                }

                adapter.submitList(resp)

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            NEW_APPT_ACTI_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    finish()
                }
            }
        }
    }
}
