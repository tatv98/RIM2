package vn.icar.rim.view;

import lombok.val;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import vn.icar.rim.R;
import vn.icar.rim.RemoteInputsMgr;
import vn.icar.rim.device.actions.ActionExecutorFactory;
import vn.icar.rim.device.actions.CommandType;
import vn.icar.rim.device.actions.executors.ActionExecutor;
import vn.icar.rim.device.actions.executors.ActionExecutor.ActionType;
import vn.icar.rim.device.actions.executors.MediaExecutor.MediaAction;
import vn.icar.rim.device.actions.executors.VolumeExecutor.VolumeAction;
import vn.icar.rim.device.entitiy.ActionInfo;
import vn.icar.rim.device.entitiy.ActionInfo.EventType;
import vn.icar.rim.device.entitiy.AppInfo;
import vn.icar.rim.device.entitiy.ButtonInfo;
import vn.icar.rim.device.entitiy.TaskInfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

@EViewGroup(R.layout.listview_item_button_info)
public class ButtonInfoView extends LinearLayout {

    @App RemoteInputsMgr app;

    @Bean ActionExecutorFactory actionFactory;

    @ViewById(R.id.buttonInfoValue) TextView valueView;
    @ViewById(R.id.buttonInfoClickAction) TextView clickView;
    @ViewById(R.id.buttonInfoHoldAction) TextView holdView;

    private Drawable taskerDrawable;

    public ButtonInfoView(Context context) {

        super(context);
    }

    public ButtonInfoView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public ButtonInfoView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void setButton(ButtonInfo entity) {

        setId((int) entity.getId());

        valueView.setText(String.format("%04d", entity.getValue()) + " " + getContext().getString(R.string.button_error) + " " + entity.getError());

        for (final ActionInfo actionInfo : entity.getActions()) {
            TextView textView = null;
            switch (actionInfo.getEvent()) {
                case CLICK:
                    textView = clickView;
                    break;
                case HOLD:
                    textView = holdView;
                    break;
            }

            textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            textView.setText("None");

            OnClickListener listener = new OnClickListener() {

                private CommandType prevState = CommandType.RELEASE;

                @Override
                public void onClick(View view) {

                    ActionExecutor executor = actionFactory.getExecutor(actionInfo.getActionType());
                    if (executor != null) {
                        if (actionInfo.getEvent() == EventType.HOLD) {
                            executor.execute(prevState = (prevState == CommandType.HOLD ? CommandType.RELEASE : CommandType.HOLD), actionInfo.getAction());
                        } else {
                            executor.execute(CommandType.CLICK, actionInfo.getAction());
                        }
                    }
                }
            };

            if (actionInfo.getAction() != null && !actionInfo.getAction().isEmpty()) {
                switch (actionInfo.getActionType()) {
                    case VOLUME:
                        String volumeLabel = getResources().getStringArray(R.array.action_types)[ActionType.VOLUME.ordinal()];
                        volumeLabel += ": ";
                        volumeLabel += getResources().getStringArray(R.array.volume_actions)[Enum.valueOf(VolumeAction.class, actionInfo.getAction()).ordinal()];
                        textView.setText(volumeLabel);
                        textView.setOnClickListener(listener);
                        break;
                    case MEDIA:
                        String mediaLabel = getResources().getStringArray(R.array.action_types)[ActionType.MEDIA.ordinal()];
                        mediaLabel += ": ";
                        mediaLabel += getResources().getStringArray(R.array.media_actions)[Enum.valueOf(MediaAction.class, actionInfo.getAction()).ordinal()];
                        textView.setText(mediaLabel);
                        textView.setOnClickListener(listener);
                        break;
                    case APP:
                        for (AppInfo appInfo : app.getPackages()) {
                            if (appInfo.getPackageInfo().packageName.equals(actionInfo.getAction())) {
                                textView.setCompoundDrawablesWithIntrinsicBounds(appInfo.getSDrawable(), null, null, null);
                                textView.setText(appInfo.getTitle());
                                textView.setOnClickListener(listener);
                                break;
                            }
                        }
                        break;
                    case TASKER:
                        for (TaskInfo taskInfo : app.getTasks()) {
                            if (taskInfo.getName().equals(actionInfo.getAction())) {
                                textView.setCompoundDrawablesWithIntrinsicBounds(getTaskerDrawable(), null, null, null);
                                textView.setText(taskInfo.getName());
                                textView.setOnClickListener(listener);
                                break;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

    }

    public Drawable getTaskerDrawable() {

        if (taskerDrawable == null) {
            Drawable drawable = getContext().getResources().getDrawable(R.drawable.ic_tasker);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            taskerDrawable = new BitmapDrawable(getContext().getResources(), Bitmap.createScaledBitmap(bitmap, 24, 24, true));
        }

        return taskerDrawable;
    }

}
