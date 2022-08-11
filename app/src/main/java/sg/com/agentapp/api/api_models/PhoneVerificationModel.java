package sg.com.agentapp.api.api_models;

/**
 * Created by Jay on 16/10/18.
 */

public class PhoneVerificationModel {
    private String prefix;
    private String phone;
    private String token;
    private int resend;

    public PhoneVerificationModel(String prefix, String phone, Integer resend) {
        this.prefix = prefix;
        this.phone = phone;
        this.resend = resend;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
