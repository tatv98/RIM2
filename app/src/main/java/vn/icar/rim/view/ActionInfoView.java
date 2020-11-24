package vn.icar.rim.view;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import vn.icar.rim.R;
import vn.icar.rim.adapter.AppInfoAdapter;
import vn.icar.rim.adapter.TaskInfoAdapter;
import vn.icar.rim.device.actions.executors.ActionExecutor.ActionType;
import vn.icar.rim.device.actions.executors.MediaExecutor.MediaAction;
import vn.icar.rim.device.actions.executors.VolumeExecutor.VolumeAction;
import vn.icar.rim.device.entitiy.ActionInfo;
import vn.icar.rim.device.entitiy.AppInfo;
import vn.icar.rim.device.entitiy.TaskInfo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

@EViewGroup(R.layout.listview_item_action_info)
public class ActionInfoView extends LinearLayout {

    @ViewById(R.id.buttonInfoEvent) TextView eventView;
    @ViewById(R.id.buttonActionType) Spinner typeView;
    @ViewById(R.id.buttonActionValue) Spinner valueView;

    private ActionInfo entity;

    private ArrayAdapter<String> volumeAdapter;
    private ArrayAdapter<String> mediaAdapter;

    @Bean AppInfoAdapter appAdapter;
    @Bean TaskInfoAdapter taskAdapter;

    public ActionInfoView(Context context) {

        super(context);
    }

    public ActionInfoView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public ActionInfoView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    @AfterInject
    void onAfterInject() {

        volumeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getContext().getResources().getStringArray(R.array.volume_actions));
        volumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mediaAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getContext().getResources().getStringArray(R.array.media_actions));
        mediaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @AfterViews
    void onAfterViews() {

        typeView.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                entity.setActionType(ActionType.values()[pos]);

                valueViewAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        valueView.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                switch (entity.getActionType()) {
                    case VOLUME:
                        entity.setAction(VolumeAction.values()[pos].name());
                        break;
                    case MEDIA:
                        entity.setAction(MediaAction.values()[pos].name());
                        break;
                    case APP:
                        entity.setAction(((AppInfo) valueView.getItemAtPosition(pos)).getPackageInfo().packageName);
                        break;
                    case TASKER:
                        entity.setAction(((TaskInfo) valueView.getItemAtPosition(pos)).getName());
                        break;
                    default:
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

    }

    private void valueViewAdapter() {

        valueView.setVisibility(View.VISIBLE);

        switch (entity.getActionType()) {
            case VOLUME:
                if (valueView.getAdapter() != volumeAdapter) {
                    valueView.setPrompt("");
                    valueView.setAdapter(volumeAdapter);
                    volumeAdapter.notifyDataSetChanged();
                }
                break;
            case MEDIA:
                if (valueView.getAdapter() != mediaAdapter) {
                    valueView.setPrompt("");
                    valueView.setAdapter(mediaAdapter);
                    mediaAdapter.notifyDataSetChanged();
                }
                break;
            case APP:
                if (valueView.getAdapter() != appAdapter) {
                    valueView.setPrompt(getContext().getString(R.string.title_select_application));
                    valueView.setAdapter(appAdapter);
                    appAdapter.notifyDataSetChanged();
                }
                break;
            case TASKER:
                if (valueView.getAdapter() != taskAdapter) {
                    valueView.setPrompt(getContext().getString(R.string.title_select_tasker_tasks));
                    valueView.setAdapter(taskAdapter);
                    taskAdapter.notifyDataSetChanged();
                }
                break;
            default:
                valueView.setVisibility(View.GONE);
                valueView.setAdapter(null);
                break;
        }
    }

    public void setActionInfo(ActionInfo entity) {

        this.entity = entity;

        switch (entity.getEvent()) {
            case CLICK:
                eventView.setText(R.string.click_action);
                break;
            case HOLD:
                eventView.setText(R.string.hold_action);
                break;
        }

        typeView.setSelection(entity.getActionType().ordinal());

        valueViewAdapter();

        if (entity.getAction() != null && !entity.getAction().isEmpty()) {
            switch (entity.getActionType()) {
                case VOLUME:
                    valueView.setSelection(Enum.valueOf(VolumeAction.class, entity.getAction()).ordinal());
                    break;
                case MEDIA:
                    valueView.setSelection(Enum.valueOf(MediaAction.class, entity.getAction()).ordinal());
                    break;
                case APP:
                    valueView.setSelection(0);
                    for (int i = 0; i < valueView.getCount(); i++) {
                        if (((AppInfo) valueView.getItemAtPosition(i)).getPackageInfo().packageName.equals(entity.getAction())) {
                            valueView.setSelection(i);
                            break;
                        }
                    }
                    break;
                case TASKER:
                    valueView.setSelection(0);
                    for (int i = 0; i < valueView.getCount(); i++) {
                        if (((TaskInfo) valueView.getItemAtPosition(i)).getName().equals(entity.getAction())) {
                            valueView.setSelection(i);
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
