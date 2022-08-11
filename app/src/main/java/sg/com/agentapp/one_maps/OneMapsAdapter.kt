package sg.com.agentapp.one_maps

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import sg.com.agentapp.R
import sg.com.agentapp.api.api_models.OneMapsSearchResultsItem
import sg.com.agentapp.databinding.OneMapsSearchItemBinding
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.UIHelper

class OneMapsAdapter : ListAdapter<OneMapsSearchResultsItem, OneMapsAdapter.Holder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder((DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.one_maps_search_item, parent, false) as OneMapsSearchItemBinding?)!!)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setData(getItem(position))
    }

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<OneMapsSearchResultsItem>() {
            override fun areItemsTheSame(
                    oldUser: OneMapsSearchResultsItem, newUser: OneMapsSearchResultsItem): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                //            return oldUser.getChatList().getChatJid().equals(newUser.getChatList().getChatJid());

                return oldUser.equals(newUser)
            }

            override fun areContentsTheSame(
                    oldUser: OneMapsSearchResultsItem, newUser: OneMapsSearchResultsItem): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return oldUser.equals(newUser)
            }
        }
    }

    class Holder internal constructor(internal var binding: OneMapsSearchItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val acti: OneMapsMain

        init {
            acti = itemView.context as OneMapsMain
        }

        fun setData(item: OneMapsSearchResultsItem) {
            binding.data = item

            itemView.setOnClickListener { view ->
                // create and move to marker
                val lat = item.lATITUDE?.toDouble()
                val lng = item.lONGITUDE?.toDouble()
                val name = item.sEARCHVAL

                val blockNum = item.bLKNO
                val roadName = item.rOADNAME

                var address: String?
                if (!blockNum.isNullOrBlank() && !roadName.isNullOrBlank() && !roadName.equals("NIL")) { // got block num/etc
                    address = "$blockNum $roadName"
                } else {
                    address = item.aDDRESS

                    // check if last = singapore then remove
                    val last9 = address?.substring(address.length - 9)
                    Log.d(GlobalVars.TAG1, "Holder, setData: last9 = $last9")

                    if (last9.equals("SINGAPORE")) {
                        address = address?.substring(0, address.length - 9)
                    }
                }

                Log.d(GlobalVars.TAG1, "Holder, setData: block = ${item.bLKNO}, road = ${item.rOADNAME}, add = ${item.aDDRESS}")

                acti.createAndMoveToMarker(lat, lng, name, address)

                // set searchbar value as full address
                acti.obsNeedUpdateSearch.set(false)
                acti.obsSearchEditTxt.set(address)

                // hide search ui and keyboard
                acti.clearSearchUI()
                UIHelper().hideActiKeyboard(acti)
            }
        }
    }
}
