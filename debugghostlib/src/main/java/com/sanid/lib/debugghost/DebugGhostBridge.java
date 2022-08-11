package com.sanid.lib.debugghost;

import android.content.Context;


public class DebugGhostBridge extends AbstractDebugGhostBridge {

    public DebugGhostBridge(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, databaseVersion);
        startDebugGhost();
        initCommands();
    }

    private void initCommands() {
        // add your custom commands here which are derived from AbstractGhostCommand
        // e.g.
        // addGhostCommand(new ShowToastCommand(mContext, "Show toast in app", "show_toast", " "));
    }
}