package vn.icar.rim.device.actions.executors;

import vn.icar.rim.device.actions.CommandType;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class AppExecutor extends ActionExecutor {

    public AppExecutor(Context context) {

        super(context);
    }

    @Override
    public void execute(CommandType command, String packageName) {

        if (command == CommandType.RELEASE) {
            return;
        }

        Context context = getContext();
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            context.startActivity(intent);
        }
    }

}
