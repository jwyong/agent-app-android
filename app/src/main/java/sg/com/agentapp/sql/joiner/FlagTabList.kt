package sg.com.agentapp.sql.joiner

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.room.Embedded
import sg.com.agentapp.R
import sg.com.agentapp.global.GlideAPI.GlideApp
import sg.com.agentapp.sql.entity.ContactRoster
import sg.com.agentapp.sql.entity.Message

data class FlagTabList(
        @Embedded
        var message: Message,
        @Embedded
        var contactRoster: ContactRoster

) {
    constructor() : this(Message(), ContactRoster())

    // get xxx > xxx displayname for flagTab
    fun getDisplayName(): String {
        when (message.IsSender) {
            0, 10, 12, 20, 22, 24 -> { // outgoing - You > xxx
                return "You > ${contactRoster.CRName}"
            }

            1, 11, 13, 21, 23, 25 -> { // incoming - xxx > You
                return "${contactRoster.CRName} > You"
            }

            else -> {
                return "You > ${contactRoster.CRName}"
            }
        }
    }

}

// glide binding adapter for profile imges
@BindingAdapter("android:proPicByte")
fun setProfilePic(view: ImageView, imgBytes: ByteArray?) {
    GlideApp.with(view)
            .load(imgBytes)
            .placeholder(R.drawable.ic_profile_def_200px)
            .error(R.drawable.ic_profile_def_200px)
            .dontAnimate()
            .circleCrop()
            .into(view)
}
