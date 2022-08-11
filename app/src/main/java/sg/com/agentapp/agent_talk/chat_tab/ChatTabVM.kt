package sg.com.agentapp.agent_talk.chat_tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import sg.com.agentapp.AgentApp
import sg.com.agentapp.sql.joiner.ChatTabList

class ChatTabVM : ViewModel() {
    val chatTabList: LiveData<List<ChatTabList>> = AgentApp.database!!.selectQuery().getChatList()
}