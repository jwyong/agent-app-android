package sg.com.agentapp.setting.fragments.blocked_list

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import sg.com.agentapp.R
import sg.com.agentapp.global.UIHelper

class BlockedListHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val context: Context
    private var deleteBtn: ImageView? = null
    private val uiHelper = UIHelper()

    init {

        context = itemView.context
    }

    fun setData() {

        itemView.setOnClickListener {
            //                Intent intent = new Intent(context,AddEditTenantMain.class);
            //                context.startActivity(intent);
        }

        deleteBtn = itemView.findViewById(R.id.delete_btn)
        deleteBtn!!.setOnClickListener { l ->

            //            uiHelper.dialog2btn(context, "Unblock Aaron-R3628893", "Unblock", "Cancel",
//                    unblockAgent, null, false)
        }


    }
}
