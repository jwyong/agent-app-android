package sg.com.agentapp.flag

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import sg.com.agentapp.AgentApp
import sg.com.agentapp.sql.joiner.FlagTabList

class FlagTabVM : ViewModel() {

    val flagTabList: LiveData<List<FlagTabList>>


    init {
        flagTabList = AgentApp.database!!.selectQuery().getFlagList()
    }

}