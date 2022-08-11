package sg.com.agentapp.flag

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import sg.com.agentapp.databinding.FlagMainBinding
import sg.com.agentapp.global.rv_selection.SelectionKeyProvider
import sg.com.agentapp.global.rv_selection.SelectionState
import sg.com.agentapp.home.HomeActivity
import sg.com.agentapp.sql.DatabaseHelper
import sg.com.agentapp.sql.entity.Message
import sg.com.agentapp.sql.joiner.FlagTabList


class FlagMain : Fragment(), HomeActivity.FlagTabComm {
    //databinding
    lateinit var binding: FlagMainBinding
    private var flagMainView: View? = null
    private var flagAdapter = FlagAdapter()

    // for selection
    private lateinit var vm: FlagTabVM
    var flagTracker: SelectionTracker<Long>? = null
    var homeActivity: HomeActivity? = null
    var selMsgList: List<Message> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        if (flagMainView == null) {
            homeActivity = context as HomeActivity

            binding = DataBindingUtil.inflate(inflater, sg.com.agentapp.R.layout.flag_main, container, false)
            binding.data = this
            flagMainView = binding.root

            // setup recycler view with selection and live data
            setupRV()
            setupRVSelTracker()
            setupLiveData()
        }

        return flagMainView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        (context as HomeActivity).flagTabComm = this
    }

    // setup recyclerview
    fun setupRV() {
        binding.recyclerView.apply {
            // set a LinearLayoutManager to handle Android
            // RecyclerView behavior
            layoutManager = LinearLayoutManager(context)

            flagAdapter.setHasStableIds(true)

            adapter = flagAdapter

            // add separator lines
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        }
    }

    // setup recyclerview selection tracker
    fun setupRVSelTracker() {
        // build tracker
        flagTracker = SelectionTracker.Builder(
                "flagListSel",
                binding.recyclerView,
                SelectionKeyProvider(binding.recyclerView),
                FlagTabItemsLookup(binding.recyclerView),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
                SelectionState(true, false, true)
        ).build()

        // add tracker to adapter
        flagAdapter.tracker = flagTracker

        // add observer to tracker
        flagTracker?.addObserver(
                object : SelectionTracker.SelectionObserver<Long>() {
                    override fun onSelectionChanged() {
                        super.onSelectionChanged()

                        homeActivity?.itemSelected?.set(flagTracker?.selection!!.size())
                    }
                })
    }

    // setup live data
    fun setupLiveData() {
        vm = ViewModelProviders.of(this).get(FlagTabVM::class.java)
        vm.flagTabList.observe(this, Observer<List<FlagTabList>> { t -> flagAdapter.submitList(t) })
    }

    // for clearing tracker from homeActi
    override fun clearTrackerSel() {
        flagTracker?.clearSelection()
    }

    // delete flags (homeActi click delete btn)
    override fun selectionAction(action: Int) {
        // get list of selected msg
        selMsgList = vm.flagTabList.value!!.mapNotNull {
            if (flagTracker!!.selection.contains(it.message.MsgRow.toLong())) {
                it.message
            } else {
                null
            }
        }

        // delete selected msges from flaglist
        when (action) {
            1 -> { // delete
                DatabaseHelper.flagUnflagMsg(selMsgList, 0)

                // clear selection once done
                homeActivity?.clearFragSel()
            }
        }
    }
}
