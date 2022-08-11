package sg.com.agentapp.sql.vm_factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import sg.com.agentapp.sql.repo.ContactRosterRepo
import sg.com.agentapp.sql.view_models.ContactRosterVM

class ContactRosterVMF(
        private val repository: ContactRosterRepo
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContactRosterVM(repository) as T
    }
}