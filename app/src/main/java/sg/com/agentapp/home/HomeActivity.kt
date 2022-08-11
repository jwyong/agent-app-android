package sg.com.agentapp.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableInt
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import sg.com.agentapp.R
import sg.com.agentapp.agent_talk.chat_tab.ChatTabMain
import sg.com.agentapp.appt_tab.appt_tab.AppointmentMain
import sg.com.agentapp.databinding.HomeBinding
import sg.com.agentapp.find_agent.FindAgent
import sg.com.agentapp.flag.FlagMain
import sg.com.agentapp.global.UIHelper
import sg.com.agentapp.setting.SettingActivity

class HomeActivity : AppCompatActivity() {
    // databinding
    lateinit var binding: HomeBinding

    // fragment tabs
    private var tab: TabLayout? = null
    private var tabArrays: IntArray? = null
    private var viewPager: ViewPager? = null

    // item selection in fragments
    var chatTabComm: ChatTabComm? = null
    var flagTabComm: FlagTabComm? = null
    var itemSelected = ObservableInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.home)
        binding.data = this

        UIHelper().gradientBgWhiteBig(this, findViewById(R.id.gradient_bg), 3)

        viewPager = findViewById(R.id.viewPager)
        tab = findViewById(R.id.tabs)

        tabArrays = intArrayOf(R.string.agentTalk, R.string.appointment, R.string.flag)

        setupViewPager(viewPager!!)
        tab?.setupWithViewPager(viewPager)
        setTabLabel()

        // setup tabchange listener
        tab?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                // clear selections if go to other fragment
                clearFragSel()
            }

        })
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = HomeAdapter(this.supportFragmentManager)

        adapter.addFragment(ChatTabMain())
        adapter.addFragment(AppointmentMain())
        adapter.addFragment(FlagMain())

        viewPager.adapter = adapter
    }

    fun setTabLabel() {
        for (i in 0 until tab!!.tabCount) {
            if (i < 2) {
                tab?.getTabAt(i)!!.setText(tabArrays!![i])
            } else {
                val currentTab = tab?.getTabAt(i)!!
                currentTab.setIcon(R.drawable.ic_flag_200px)
                currentTab.icon?.setTint(ContextCompat.getColor(this, R.color.red))
                tab?.setTabWidthAsWrapContent(i)
            }
        }
    }

    fun TabLayout.setTabWidthAsWrapContent(tabPosition: Int) {
        val layout = (this.getChildAt(0) as LinearLayout).getChildAt(tabPosition) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0f
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        layout.layoutParams = layoutParams
    }


    //===btn onclick functions
    fun findAgentBtnOnclick(_v: View) {
        val intent = Intent(this, FindAgent::class.java)
        startActivity(intent)
    }

    fun settingsBtnOnclick(_v: View) {
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
    }

    fun searchBtnOnclick(_v: View) {

    }

    // selection delete btn (for chattab and flagtab)
    fun selectionDelBtn(_v: View) {
        val deleteAction = Runnable {
            when (tab?.selectedTabPosition) {
                0 -> { // chat tab
                    chatTabComm?.selectionAction(1)

                }

                2 -> { // flag tab
                    flagTabComm?.selectionAction(1)

                }
            }
        }

        UIHelper().dialog2btn(this, getString(R.string.delete_item_confirm),
                getString(sg.com.agentapp.R.string.ok), getString(R.string.cancel), deleteAction,
                null, true)

    }

    // clear selections
    fun clearFragSel() {
        itemSelected.set(0)

        when (tab?.selectedTabPosition) {
            0 -> { // chat tab
                chatTabComm?.clearTrackerSel()

            }

            2 -> { // flag tab
                flagTabComm?.clearTrackerSel()

            }
        }
    }

    // for fragment variables communicating
    interface ChatTabComm {
        fun clearTrackerSel()

        fun selectionAction(action: Int)
    }

    interface FlagTabComm {
        fun clearTrackerSel()

        fun selectionAction(action: Int)
    }

    override fun onBackPressed() {
        if (itemSelected.get() > 0) { // got items selected
            clearFragSel()

        } else {
            super.onBackPressed()
        }
    }
}