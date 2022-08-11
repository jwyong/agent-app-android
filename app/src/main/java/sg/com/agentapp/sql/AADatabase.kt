package sg.com.agentapp.sql

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import sg.com.agentapp.sql.dao.ContactRosterDao
import sg.com.agentapp.sql.entity.*
import sg.com.agentapp.sql.general_queries.GeneralDeleteQuery
import sg.com.agentapp.sql.general_queries.GeneralInsertQuery
import sg.com.agentapp.sql.general_queries.GeneralSelectQuery
import sg.com.agentapp.sql.general_queries.GeneralUpdateQuery

//tables
//IMPORTANT - remember to reset version before going LIVE
@Database(entities = [Message::class, ContactRoster::class, AgentSql::class, ChatList::class,
    PushNotification::class, Appointment::class, ApptWorkUUID::class], version = 1, exportSchema = false)
abstract class AADatabase : RoomDatabase() {
    abstract fun contactRosterDao(): ContactRosterDao

    abstract fun selectQuery(): GeneralSelectQuery

    abstract fun updateQuery(): GeneralUpdateQuery


    abstract fun insertQuery(): GeneralInsertQuery

    abstract fun deleteQuery(): GeneralDeleteQuery

    companion object {
        @Volatile
        private var INSTANCE: AADatabase? = null

        @Synchronized
        fun getInstance(context: Context): AADatabase? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AADatabase::class.java,
                        "AgentAppDB")
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
            }

            return INSTANCE
        }
    }
}
