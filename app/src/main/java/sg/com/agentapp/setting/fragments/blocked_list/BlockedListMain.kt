package sg.com.agentapp.setting.fragments.blocked_list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import sg.com.agentapp.R

class BlockedListMain : Fragment() {

    private var ctx: Context? = null
    private var v: View? = null
    private var recyclerView: RecyclerView? = null
    private var blockedListAdapter: BlockedListAdapter? = null
    private var layoutManager: LinearLayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ctx = context

        if (v == null) {
            v = inflater.inflate(R.layout.blocked_main, container, false)

            recyclerView = v!!.findViewById(R.id.recyclerView)
            blockedListAdapter = BlockedListAdapter()

            layoutManager = LinearLayoutManager(ctx)
            layoutManager!!.orientation = RecyclerView.VERTICAL

            //            chatListAdapter.setHasStableIds(true);

            recyclerView!!.layoutManager = layoutManager
            //            recyclerView.setItemAnimator();
            recyclerView!!.adapter = blockedListAdapter
        }

        return v
    }

}
