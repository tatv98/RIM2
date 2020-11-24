package vn.icar.rim.device.actions.executors;

import vn.icar.rim.device.actions.CommandType;

import android.content.Context;

public abstract class ActionExecutor {

    public enum ActionType {

        NONE, VOLUME, MEDIA, APP, TASKER

    }

    private Context context;

    public ActionExecutor(Context context) {

        this.context = context;
    }

    public Context getContext() {

        return context;
    }

    public abstract void execute(CommandType command, String action);

}
