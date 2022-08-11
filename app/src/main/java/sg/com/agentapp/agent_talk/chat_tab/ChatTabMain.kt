package sg.com.agentapp.agent_talk.chat_tab


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import sg.com.agentapp.R
import sg.com.agentapp.databinding.ChatlistMainBinding
import sg.com.agentapp.global.rv_selection.SelectionKeyProvider
import sg.com.agentapp.global.rv_selection.SelectionState
import sg.com.agentapp.home.HomeActivity
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.sql.joiner.ChatTabList

class ChatTabMain : Fragment(), HomeActivity.ChatTabComm {
    private val TAG = "JAY"

    // for recyclerview
    lateinit var binding: ChatlistMainBinding
    val chatTabAdapter: ChatTabAdapter = ChatTabAdapter()
    private var chatTabView: View? = null

    // for rv selection
    private lateinit var chatTabVM: ChatTabVM
    private var chatTabTracker: SelectionTracker<Long>? = null
    private var homeActivity: HomeActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (chatTabView == null) {
            homeActivity = context as HomeActivity

            binding = DataBindingUtil.inflate(inflater, R.layout.chatlist_main, container, false)
            binding.data = this
            chatTabView = binding.root

            // setup recycler view with selection and live data
            setupRV()
//            setupRVSelTracker()
            setupLiveData()
        }

        return chatTabView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        (context as HomeActivity).chatTabComm = this
    }

    // setup recyclerview
    fun setupRV() {
        binding.chatListRv.apply {
            // set a LinearLayoutManager to handle Android
            // RecyclerView behavior
            layoutManager = LinearLayoutManager(context)

            chatTabAdapter.setHasStableIds(true)

            adapter = chatTabAdapter
        }
    }

    // setup recyclerview selection tracker
    fun setupRVSelTracker() {
        // build tracker
        chatTabTracker = SelectionTracker.Builder<Long>(
                "chatListSel",
                binding.chatListRv,
                SelectionKeyProvider(binding.chatListRv),
                ChatTabItemsLookup(binding.chatListRv),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
                SelectionState(true, false, true)
        ).build()

        // add tracker to adapter
        chatTabAdapter.tracker = chatTabTracker

        // add observer to tracker
        chatTabTracker?.addObserver(
                object : SelectionTracker.SelectionObserver<Long>() {
                    override fun onSelectionChanged() {
                        super.onSelectionChanged()

                        homeActivity?.itemSelected?.set(chatTabTracker?.selection!!.size())
                    }
                })
    }

    // setup live data
    fun setupLiveData() {
        chatTabVM = ViewModelProviders.of(this).get(ChatTabVM::class.java)
        chatTabVM.chatTabList.observe(this, object : Observer<List<ChatTabList>?> {
            override fun onChanged(t: List<ChatTabList>?) {
                chatTabAdapter.submitList(t)
            }
        })
    }

    // for clearing tracker from homeActi
    override fun clearTrackerSel() {
        chatTabTracker?.clearSelection()
    }

    // delete flags (homeActi click delete btn)
    override fun selectionAction(action: Int) {
        Log.d(TAG, "chattab del btn clicked")

        val selJidList = chatTabVM.chatTabList.value!!.mapNotNull {
            if (chatTabTracker!!.selection.contains(it.chatList.chatRow!!.toLong())) {
                it.chatList.chatJid
            } else {
                null
            }
        }

        // delete selected msges from flaglist
        when (action) {
            1 -> { // delete
                DatabaseHelper.deleteChatRoom(selJidList)

                // clear selection once done
                homeActivity?.clearFragSel()
            }
        }
    }

    fun deleteChatRooms(_v: View) {

    }
}
