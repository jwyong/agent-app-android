package sg.com.agentapp.share_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import sg.com.agentapp.AgentApp
import sg.com.agentapp.sql.joiner.ChatTabList

class ShareActiVM : ViewModel() {
    val shareActiList: LiveData<List<ChatTabList>> = AgentApp.database!!.selectQuery().getChatList()
}