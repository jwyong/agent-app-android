package sg.com.agentapp.sql.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity
class ContactRoster {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CRRow")
    var CRRow: Int = 0
    @ColumnInfo(name = "CRJid")
    var CRJid: String? = null
    @ColumnInfo(name = "CRName")
    var CRName: String? = null
    @ColumnInfo(name = "CRPhoneNumber")
    var CRPhoneNumber: String? = null
    @ColumnInfo(name = "CRResourceID")
    var CRResourceID: String? = null
    @ColumnInfo(name = "CRProfilePic")
    var CRProfileThumb: ByteArray? = null
    @ColumnInfo(name = "CRBlocked")
    var CRBlocked: Int? = 0

    constructor()

    @Ignore
    constructor(CRJid: String, CRName: String?, CRPhoneNumber: String?, CRResourceID: String?,
                CRProfileThumb: ByteArray?, CRProfileFull: ByteArray?, CRBlocked: Int?) {
        this.CRJid = CRJid
        this.CRName = CRName
        this.CRPhoneNumber = CRPhoneNumber
        this.CRResourceID = CRResourceID
        this.CRProfileThumb = CRProfileThumb
        this.CRBlocked = CRBlocked
    }

    @Ignore
    constructor(CRJid: String?, CRName: String?, CRPhoneNumber: String?) {
        this.CRJid = CRJid
        this.CRName = CRName
        this.CRPhoneNumber = CRPhoneNumber
    }

    @Ignore
    constructor(CRJid: String?, CRName: String?, CRPhoneNumber: String?, CRProfileThumb: ByteArray) {
        this.CRJid = CRJid
        this.CRName = CRName
        this.CRPhoneNumber = CRPhoneNumber
        this.CRProfileThumb = CRProfileThumb
    }

    override fun toString(): String {
        return "ContactRoster(CRRow=$CRRow, CRJid=$CRJid, CRName=$CRName, CRPhoneNumber=$CRPhoneNumber, CRResourceID=$CRResourceID, CRProfileThumb=${Arrays.toString(CRProfileThumb)}, CRBlocked=$CRBlocked)"
    }


}
