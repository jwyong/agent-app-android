package sg.com.agentapp.sql.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AgentSql(

        @PrimaryKey(autoGenerate = true)
        var agentRow: Int = 0,

        @ColumnInfo(name = "is_registered")
        var registered: String? = null,

        @ColumnInfo(name = "agent_name")
        var agentName: String? = null,

        @ColumnInfo(name = "phone_number")
        var phoneNumber: String? = null,

        @ColumnInfo(name = "estate_agent_name")
        var estateAgentName: String? = null,

        @ColumnInfo(name = "cea_no")
        var ceaNo: String? = null
) {
    override fun toString(): String {
        return "AgentSql(agentRow=$agentRow, registered=$registered, agentName=$agentName, phoneNumber=$phoneNumber, estateAgentName=$estateAgentName, ceaNo=$ceaNo)"
    }
}
