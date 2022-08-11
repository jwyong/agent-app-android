package sg.com.agentapp.agent_profile;

import android.os.Bundle;

import sg.com.agentapp.R;
import sg.com.agentapp.global.BaseActivity;

public class AgentProfileMain extends BaseActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myprofile);
        setupToolbar();

    }
}