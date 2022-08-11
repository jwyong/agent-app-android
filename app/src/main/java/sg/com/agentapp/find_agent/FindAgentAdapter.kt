package sg.com.agentapp.find_agent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import sg.com.agentapp.R
import sg.com.agentapp.agent_talk.chat_room.ChatRoom
import sg.com.agentapp.api.api_models.FindAgentModel
import sg.com.agentapp.appt_tab.appt_room.AppointmentNew
import sg.com.agentapp.databinding.FindAgentItemBinding
import sg.com.agentapp.global.GlobalVars
import sg.com.agentapp.global.Preferences
import java.io.Serializable

class FindAgentAdapter : ListAdapter<FindAgentModel, FindAgentAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.find_agent_item, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).let(fun(data: FindAgentModel?) {
            with(holder) {
                bind(data)
            }
        })
    }

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FindAgentModel>() {
            override fun areItemsTheSame(
                    oldUser: FindAgentModel, newUser: FindAgentModel): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                return oldUser.ceaNo.equals(newUser.ceaNo)
//                return false
            }

            override fun areContentsTheSame(
                    oldUser: FindAgentModel, newUser: FindAgentModel): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return oldUser == newUser
            }
        }
    }

    class ViewHolder(
            private val binding: FindAgentItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        var imgv: ImageView = itemView.findViewById(R.id.user_profile)

        fun bind(data: FindAgentModel?) {
            with(binding) {
                vm = data
                executePendingBindings()

                Glide.with(itemView.context)
                        .load(data?.s3Path)
                        .circleCrop()
                        .error(R.drawable.ic_profile_def_200px)
                        .into(imgv)

                itemView.setOnClickListener {
                    val ctx = itemView.context
                    val findAgent = ctx as FindAgent

                    Log.d(GlobalVars.TAG1, "ViewHolder, bind: jid = ${data?.ceaNo}")
                    Log.d(GlobalVars.TAG1, "ViewHolder, bind: myXmppJID = ${Preferences.getInstance().userXMPPJid}")
                    Log.d(GlobalVars.TAG1, "ViewHolder, bind: myXmppPass = ${Preferences.getInstance().userXMPPPassword}")

                    // go to new appt if coming from new appt btn
                    if (findAgent.intent.hasExtra("newAppt")) {
                        val intent = Intent(ctx, AppointmentNew::class.java)
                        intent.putExtra("agentid", data?.ceaNo)
                        intent.putExtra("agentname", data?.agentName)
                        intent.putExtra("agentphone", data?.phoneNumber)
                        intent.putExtra("profile_url", data?.s3Path)

                        ctx.startActivityForResult(intent, ctx.NEW_APPT_ACTI_CODE)

                    } else { // else go to chatroom
                        val intent = Intent(ctx, ChatRoom::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                        val b = Bundle()
                        b.putString("jid", data?.ceaNo)
                        b.putString("name", data?.agentName)
                        b.putString("phone", data?.phoneNumber)
                        b.putString("profile_url", data?.s3Path)

                        // add msgList - for sharing/etc
                        if (findAgent.intent.hasExtra("msgList")) {
                            val msgList = findAgent.intent.extras["msgList"] as Serializable
                            intent.putExtra("msgList", msgList)
                        }
                        intent.putExtra("b", b)

                        ctx.startActivity(intent, b)
                    }

                }

            }
        }
    }
}
