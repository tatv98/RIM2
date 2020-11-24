package vn.icar.rim.fragment;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import lombok.val;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.UiThread.Propagation;
import org.androidannotations.annotations.ViewById;
import vn.icar.rim.R;
import vn.icar.rim.RemoteInputsMgr;
import vn.icar.rim.activity.MainActivity;
import vn.icar.rim.device.DBFactory;
import vn.icar.rim.device.entitiy.ActionInfo;
import vn.icar.rim.device.entitiy.ActionInfo.EventType;
import vn.icar.rim.device.entitiy.ButtonInfo;
import vn.icar.rim.view.ActionInfoView;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

@EFragment(R.layout.dialog_button_settings)
public class ButtonInfoDialogFragment extends DialogFragment {

    private static final String TAG = ButtonInfoDialogFragment.class.getSimpleName();

    @FragmentArg long buttonId = -1;

    @Bean DBFactory dbFactory;

    @ViewById(R.id.buttonValue) TextView buttonValue;
    @ViewById(R.id.buttonError) Spinner buttonError;
    @ViewById(R.id.buttonInfoCnt) LinearLayout buttonInfoCnt;

    private ButtonInfo button;

    private List<ActionInfo> actions;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setCanceledOnTouchOutside(false);

        if (buttonId != -1) {
            try {
                button = dbFactory.getButtonDao().queryForId(buttonId);
            } catch (SQLException e) {
                Log.e(TAG, "failed getting button with id: " + buttonId, e);
                return null;
            }
            dialog.setTitle(R.string.title_edit_button_settings);
        } else {
            button = new ButtonInfo();
            dialog.setTitle(R.string.title_create_button_settings);
        }

        return dialog;
    }

    @AfterViews
    void onInit() {

        buttonValue.setText("[" + String.format("%04d", button.getValue()) + "]");

        if (button.getId() > 0) {
            buttonError.setSelection(button.getError());
        } else {
            buttonError.setSelection(5);
        }

        if (button.getActions() == null) {
            actions = new LinkedList<ActionInfo>();
            actions.add(new ActionInfo(EventType.CLICK));
            actions.add(new ActionInfo(EventType.HOLD));
        } else {
            actions = new LinkedList<ActionInfo>(button.getActions());
        }

        for (ActionInfo action : actions) {
//            ActionInfoView view = ActionInfoView.build(getActivity());
            ActionInfoView view = new ActionInfoView(getActivity());
            buttonInfoCnt.addView(view, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            view.setActionInfo(action);
        }
    }

    @Click(android.R.id.button2)
    void onDismissClick() {

        getDialog().dismiss();

        button.setError(buttonError.getSelectedItemPosition());

        try {
            dbFactory.getButtonDao().createOrUpdate(button);
            for (ActionInfo action : actions) {
                action.setButton(button);
                dbFactory.getActionDao().createOrUpdate(action);
            }
        } catch (SQLException e) {
            Log.e(TAG, "failed to save button", e);
        } finally {
            getActivity().sendBroadcast(new Intent(MainActivity.ACTION_DATA_REFRESH), RemoteInputsMgr.PRIVATE_PERMISSION);
        }
    }

    @Click(android.R.id.button1)
    void onCancelClick() {

        getDialog().cancel();
    }

    @Receiver(actions = RemoteInputsMgr.ACTION_DATA_RECEIVE)
    void onCommand(Intent intent) {

        try {
            setButtonValue(Integer.valueOf(intent.getStringExtra(RemoteInputsMgr.EXTRA_ARGS)));
        } catch (Exception e) {}
    }

    @UiThread(propagation = Propagation.REUSE)
    void setButtonValue(int value) {

        buttonValue.setText("[" + String.format("%04d", value) + "]");
        button.setValue(value);
    }

}
