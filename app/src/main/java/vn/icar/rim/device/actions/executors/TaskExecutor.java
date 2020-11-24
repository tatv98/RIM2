package vn.icar.rim.device.actions.executors;

import net.dinglisch.android.tasker.TaskerIntent;
import net.dinglisch.android.tasker.TaskerIntent.Status;

import vn.icar.rim.device.actions.CommandType;

import android.content.Context;
import android.widget.Toast;

public class TaskExecutor extends ActionExecutor {

    public TaskExecutor(Context context) {

        super(context);
    }

    @Override
    public void execute(CommandType command, String task) {

        if (command == CommandType.RELEASE) {
            return;
        }

        Context context = getContext();
        Status status = TaskerIntent.testStatus(context);
        switch (status) {
            case OK:
                TaskerIntent intent = new TaskerIntent(task);
                if (intent != null) {
                    context.sendBroadcast(intent);
                }
                break;
            case AccessBlocked:
                getContext().startActivity(TaskerIntent.getExternalAccessPrefsIntent());
            default:
                Toast.makeText(getContext(), "Tasker: " + status, Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
