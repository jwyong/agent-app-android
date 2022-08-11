package sg.com.agentapp.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import sg.com.agentapp.AgentApp
import sg.com.agentapp.sql.entity.Message

class ChatRoomMainVM : ViewModel() {

    var messageList: LiveData<PagedList<Message>>? = null

    fun loadUsers(jid: String) {
        Log.i("wtf", jid)
        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(15)
                .setPageSize(30)
                .setInitialLoadSizeHint(30)
                .build()
        messageList = LivePagedListBuilder(AgentApp.database!!.selectQuery().getMessageList(jid), pagedListConfig)
                .build()

    }
}