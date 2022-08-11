package sg.com.agentapp.agent_talk;

import android.os.Bundle;

import androidx.annotation.Nullable;

import sg.com.agentapp.R;
import sg.com.agentapp.global.BaseActivity;

public class AgentDetails extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.agent_details);
        setupToolbar();

    }
}
