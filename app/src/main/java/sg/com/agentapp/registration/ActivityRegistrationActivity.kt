package sg.com.agentapp.registration

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

import sg.com.agentapp.R
import sg.com.agentapp.global.UIHelper

class ActivityRegistrationActivity : AppCompatActivity() {

    private var activityReg: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // set bg gradient
        UIHelper().gradientBgWhiteBig(this, findViewById(R.id.activity_reg), 0)

        activityReg = supportFragmentManager.findFragmentById(R.id.activity_reg)
    }

}
