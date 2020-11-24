package vn.icar.rim.adapter;

import java.sql.SQLException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import vn.icar.rim.device.DBFactory;
import vn.icar.rim.device.entitiy.ButtonInfo;
import vn.icar.rim.view.ButtonInfoView;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

@EBean
public class ButtonInfoAdapter extends ArrayAdapter<ButtonInfo> {

    private static final String TAG = ButtonInfoAdapter.class.getSimpleName();

    @RootContext Context context;

    @Bean DBFactory dbFactory;

    public ButtonInfoAdapter(Context context) {

        super(context, 0);
    }

    @AfterInject
    void initAdapter() {

        try {
            addAll(dbFactory.getButtonDao().queryForAll());
        } catch (SQLException e) {
            Log.e(TAG, "failed to get buttons", e);
        }
    }

    @Override
    public long getItemId(int position) {

        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
//            view = ButtonInfoView.build(context);
            view = new ButtonInfoView(context);
        }

        ((ButtonInfoView) view).setButton(getItem(position));

        return view;
    }

    public void refresh() {

        clear();

        try {
            addAll(dbFactory.getButtonDao().queryForAll());
        } catch (SQLException e) {
            Log.e(TAG, "failed to get buttons", e);
        }

        notifyDataSetChanged();
    }

}
