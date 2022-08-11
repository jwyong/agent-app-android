package sg.com.agentapp.share_activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import sg.com.agentapp.R
import sg.com.agentapp.databinding.ShareActiMainBinding
import sg.com.agentapp.find_agent.FindAgent
import sg.com.agentapp.global.BaseActivity
import sg.com.agentapp.sql.joiner.ChatTabList
import java.io.Serializable

class ShareActi : BaseActivity() {
    // databinding
    lateinit var binding: ShareActiMainBinding
    val title = ObservableField<String>()

    // recyclerview
    val shareActiAdapter = ShareActiAdapter()

    // sharing list
    lateinit var msgListSerializable: Serializable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.share_acti_main)
        binding.data = this

        //setup toolbar
        setupToolbar()

        // set title
        val b = intent.extras.getBundle("b")
        val type = b.getInt("type")

        when (type) {
            1 -> { // forward msg
                title.set("Forward to...")
            }

            else -> { // default
                title.set("Share to...")
            }
        }

        // add bundle to msglist
        if (intent.hasExtra("msgList")) {
            msgListSerializable = intent.extras["msgList"] as Serializable
        }
//        msgList = b.getSerializable("msgList") as List<Message>

        // setup recycler view with live data
        setupRV()
        setupLiveData()

    }

    // setup recyclerview
    fun setupRV() {
        binding.recyclerView.apply {
            // set a LinearLayoutManager to handle Android
            // RecyclerView behavior
            layoutManager = LinearLayoutManager(context)

            shareActiAdapter.setHasStableIds(true)

            adapter = shareActiAdapter
        }
    }

    // setup live data
    fun setupLiveData() {
        val vm = ViewModelProviders.of(this).get(ShareActiVM::class.java)
        vm.shareActiList.observe(this, Observer<List<ChatTabList>?> { t -> shareActiAdapter.submitList(t) })
    }

    fun findAgentBtn(_v: View) {
        val intent = Intent(this, FindAgent::class.java)

        // need add extra for forwarding
//        val msgListBundle = Bundle()
//        msgListBundle.putSerializable("msgList", msgList as java.io.Serializable)
        intent.putExtra("msgList", msgListSerializable)

        startActivity(intent)
    }
}