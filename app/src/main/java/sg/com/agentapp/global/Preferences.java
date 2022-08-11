package sg.com.agentapp.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import sg.com.agentapp.AgentApp;


public class Preferences {

    private static final String PREFS_NAME = "HO_ORIGIN";
    private static Preferences INSTANCE;
    private final String USER_XMPP_JID = "user_xmpp_jid";
    private final String USER_XMPP_PASSWORD = "user_xmpp_password";
    private final String HAD_LOGIN = "had_login";

    private final String DISPLAY_NAME = "DISPLAY_NAME";
    private final String USER_EMAIL = "USER_EMAIL";
    private final String USER_WEBSITE = "USER_WEBSITE";
    private final String USER_DESIGNATION = "USER_DESIGNATION";
    private SharedPreferences mPreferences;

    private Preferences(Context context) {
        mPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static Preferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Preferences(AgentApp.Companion.getInstance().getApplicationContext());
        }
        return INSTANCE;
    }

    public void save(String strPrefName, String strData) {
        Editor editor = mPreferences.edit();
        editor.putString(strPrefName, strData);
        editor.apply();
    }

    public void saveint(String strPrefName, int intData) {
        Editor editor = mPreferences.edit();
        editor.putInt(strPrefName, intData);
        editor.apply();
    }

    public int getIntValue(String strPrefName) {
        return mPreferences.getInt(strPrefName, -1);
    }


    public String getValue(String strPrefName) {
        return mPreferences.getString(strPrefName, "nil");
    }

    public void clearSharedPreference(Context context) {
        Editor editor = mPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void removeValue(Context context, String strPrefName) {
        Editor editor = mPreferences.edit();

        editor.remove(strPrefName);
        editor.apply();
    }

    public String getUserXMPPJid() {
        return mPreferences.getString(GlobalVars.PREF_CEA_NO, "");
    }

    public String getUserXMPPPassword() {
        return mPreferences.getString(GlobalVars.PREF_EJABBERD_PASSWORD, "");
    }

    public void setUserXMPPPassword(String password) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(USER_XMPP_PASSWORD, password);
        editor.apply();
    }

    public String getAgentName() {
        return mPreferences.getString(GlobalVars.PREF_NAME, "");
    }

    public String getAccessToken() {
        return mPreferences.getString(GlobalVars.PREF_ACCESS_TOKEN, "");
    }

    public boolean userAndPasswordNotEmpty() {
        return !getUserXMPPJid().equals("") && !getUserXMPPPassword().equals("");
    }

    public String getDISPLAY_NAME() {
        return mPreferences.getString(DISPLAY_NAME, "");
    }

    public String getUSER_EMAIL() {
        return mPreferences.getString(USER_EMAIL, "");
    }

    public String getUSER_WEBSITE() {
        return mPreferences.getString(USER_WEBSITE, "");
    }

    public String getUSER_DESIGNATION() {
        return mPreferences.getString(USER_DESIGNATION, "");
    }

    public void setdisplay_name(String userXMPPJid) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(DISPLAY_NAME, userXMPPJid);
        editor.apply();
    }

    public void setuser_email(String userXMPPJid) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(USER_EMAIL, userXMPPJid);
        editor.apply();
    }

    public void setuser_website(String userXMPPJid) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(USER_WEBSITE, userXMPPJid);
        editor.apply();
    }

    public void setuser_designation(String userXMPPJid) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(USER_DESIGNATION, userXMPPJid);
        editor.apply();
    }

    public Boolean getHad_Login() {
        return mPreferences.getBoolean(HAD_LOGIN, false);
    }

    public void setHad_Login(boolean b) {
        mPreferences.edit().putBoolean(HAD_LOGIN, b).apply();
    }
}
