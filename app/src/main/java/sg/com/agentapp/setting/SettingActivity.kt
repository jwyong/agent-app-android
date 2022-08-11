package sg.com.agentapp.setting

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import sg.com.agentapp.R
import sg.com.agentapp.databinding.SettingMainBinding
import sg.com.agentapp.global.BaseActivity

class SettingActivity : BaseActivity() {
    private val TAG = "JAY"
    private lateinit var binding: SettingMainBinding

    private var activityReg: Fragment? = null
    val fragmentTitle = ObservableField<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.setting_main)
        binding.data = this

        // setup toolbar and title
        setupToolbar()
        fragmentTitle.set(getString(R.string.settings))

        // set gradient bg (white big)
        setGradientBG(2)

        activityReg = supportFragmentManager.findFragmentById(R.id.main_frag)

        // go to xxx frag if needed
        if (intent.hasExtra("fragActionID")) {
            val fragActionID = intent.getIntExtra("fragActionID", 0)
            Navigation.findNavController(activityReg?.view!!).navigate(fragActionID)
            fragmentTitle.set(getString(R.string.settings_profile))
        }
    }

    override fun onBackPressed() {
        if (Navigation.findNavController(activityReg?.view!!).navigateUp()) { // close fragment
            fragmentTitle.set(getString(R.string.settings))

        } else { // close activity
            finish()

        }
    }

    //    public void onClickRadio(View view) {
    //        TextView textView = findViewById(R.id.tv_default);
    //        RadioGroup radioGroup = findViewById(R.id.radio_group);
    //        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
    //            boolean checked = ((RadioButton) view).isChecked();
    //            switch (view.getId()) {
    //                case R.id.radio_nopopup:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_on:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_off:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_ontime:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_5min:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_30min:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_1hour:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_2hour:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_1day:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_2day:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_1week:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_none:
    //                    if (checked)
    //                        break;
    //
    //                case R.id.radio_15min:
    //                    if (checked) {
    //                        textView.setVisibility(View.VISIBLE);
    //                    } else if (!checked) {
    //                        textView.setVisibility(View.GONE);
    //                    }
    //                    break;
    //            }
    //        });
    //
    //    }
}
