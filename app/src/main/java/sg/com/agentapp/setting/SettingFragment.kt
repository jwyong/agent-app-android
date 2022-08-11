package sg.com.agentapp.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import sg.com.agentapp.databinding.SettingFragItemBinding
import sg.com.agentapp.global.BtnActionHelper
import sg.com.agentapp.global.Preferences


class SettingFragment : Fragment() {
    private lateinit var binding: SettingFragItemBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, sg.com.agentapp.R.layout.setting_frag_item, container, false)
        binding.data = this

        return binding.root
    }

    // normal fragment navigation action
    fun settingsItemOnClick(view: View, actionID: Int, title: String) {
        Navigation.findNavController(view).navigate(actionID)
        (context as SettingActivity).fragmentTitle.set(title)
    }

    //=== non-fragment navigation actions
    // invite agent
    fun inviteAgentOnClick(_v: View) {
        // define invite msg
        val userName = Preferences.getInstance().agentName
        val inviteMsg = "$userName ${getString(sg.com.agentapp.R.string.invite_agent_msg)}"

        BtnActionHelper().sharePlainTxt(context, inviteMsg, "Invite Agents via:")
    }

    // go to web url
    fun goToWebUrl(view: View) {
        val url = view.tag as String
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
