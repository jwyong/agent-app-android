package sg.com.agentapp.utilities

import sg.com.agentapp.AgentApp
import sg.com.agentapp.sql.repo.ContactRosterRepo
import sg.com.agentapp.sql.vm_factory.ContactRosterVMF

/**
 * Static methods used to inject classes needed for various Activities and Fragments.
 */
object InjectorUtils {

    private fun getFindAgentRepo(): ContactRosterRepo {
        return ContactRosterRepo.getInstance(AgentApp.database!!.contactRosterDao())
    }

    //
//    private fun getGardenPlantingRepository(context: Context): GardenPlantingRepository {
//        return GardenPlantingRepository.getInstance(
//                AppDatabase.getInstance(context).gardenPlantingDao())
//    }
//
    fun provideContactRosterVMF(): ContactRosterVMF {
        val repository = getFindAgentRepo()
        return ContactRosterVMF(repository)
    }
//
//    fun providePlantListViewModelFactory(context: Context): PlantListViewModelFactory {
//        val repository = getPlantRepository(context)
//        return PlantListViewModelFactory(repository)
//    }
//
//    fun providePlantDetailViewModelFactory(
//        context: Context,
//        plantId: String
//    ): PlantDetailViewModelFactory {
//        return PlantDetailViewModelFactory(getPlantRepository(context),
//                getGardenPlantingRepository(context), plantId)
//    }
}
