package sg.com.agentapp.sql.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import sg.com.agentapp.sql.entity.ContactRoster
import sg.com.agentapp.sql.repo.ContactRosterRepo

class ContactRosterVM internal constructor(
        contactRosterRepo: ContactRosterRepo
) : ViewModel() {

    val crLDList: LiveData<List<ContactRoster>> = contactRosterRepo.getCR()
}