package sg.com.agentapp.sql.repo

import sg.com.agentapp.sql.dao.ContactRosterDao


class ContactRosterRepo private constructor(private val contactRosterDao: ContactRosterDao) {

    fun getCR() = contactRosterDao.getCR()

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: ContactRosterRepo? = null

        fun getInstance(contactRosterDao: ContactRosterDao) =
                instance ?: synchronized(this) {
                    instance ?: ContactRosterRepo(contactRosterDao).also { instance = it }
                }
    }
}