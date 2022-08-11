package sg.com.agentapp.sql.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class PushNotification {

    @PrimaryKey(autoGenerate = true)
    private Integer pnRow;

    private String senderJid;
    private String apptId;
    private String senderName;
    private String pushBody;
    private String pushType;


    public PushNotification() {
    }

    @Ignore
    public PushNotification(String senderJid, String senderName, String pushBody, String pushType, String apptId) {
        this.senderJid = senderJid;
        this.senderName = senderName;
        this.pushBody = pushBody;
        this.pushType = pushType;
        this.apptId = apptId;
    }

    public Integer getPnRow() {
        return pnRow;
    }

    public void setPnRow(Integer pnRow) {
        this.pnRow = pnRow;
    }

    public String getSenderJid() {
        return senderJid;
    }

    public void setSenderJid(String senderJid) {
        this.senderJid = senderJid;
    }


    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getPushBody() {
        return pushBody;
    }

    public void setPushBody(String pushBody) {
        this.pushBody = pushBody;
    }

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }

    public String getApptId() {
        return apptId;
    }

    public void setApptId(String apptId) {
        this.apptId = apptId;
    }

    @Override
    public String toString() {
        return "PushNotification{" +
                "pnRow=" + pnRow +
                ", senderJid='" + senderJid + '\'' +
                ", apptId='" + apptId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", pushBody='" + pushBody + '\'' +
                ", pushType='" + pushType + '\'' +
                '}';
    }
}
