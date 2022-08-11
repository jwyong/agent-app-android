package sg.com.agentapp.sql.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import sg.com.agentapp.sql.entity.ContactRoster

@Dao
interface ContactRosterDao {
    //get list of agents from contact roster
    @Query("SELECT * FROM ContactRoster ORDER BY CRName")
    fun getCR(): LiveData<List<ContactRoster>>

    @Insert
    fun insertCR(contactRoster: ContactRoster): Long


//
//    @Query("SELECT * FROM plants WHERE growZoneNumber = :growZoneNumber ORDER BY name")
//    fun getPlantsWithGrowZoneNumber(growZoneNumber: Int): LiveData<List<Plant>>
//
//    @Query("SELECT * FROM plants WHERE id = :plantId")
//    fun getPlant(plantId: String): LiveData<Plant>
//
}
