package vn.icar.rim.adapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;
import vn.icar.rim.RemoteInputsMgr;
import vn.icar.rim.device.entitiy.AppInfo;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@EBean
public class AppInfoAdapter extends ArrayAdapter<AppInfo> {

    @App RemoteInputsMgr app;

    public AppInfoAdapter(Context context) {

        super(context, 0);
    }

    @AfterInject
    void initAdapter() {

        addAll(app.getPackages());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView;

        if (convertView == null) {
            textView = createTextView();
        } else {
            textView = (TextView) convertView;
        }

        AppInfo info = getItem(position);

        textView.setCompoundDrawablesWithIntrinsicBounds(info.getSDrawable(), null, null, null);
        textView.setText(info.getTitle());

        return textView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView textView;

        if (convertView == null) {
            textView = createTextView();
        } else {
            textView = (TextView) convertView;
        }

        AppInfo info = getItem(position);

        textView.setCompoundDrawablesWithIntrinsicBounds(info.getBDrawable(), null, null, null);
        textView.setText(info.getTitle());

        return textView;
    }

    private TextView createTextView() {

        TextView textView = new TextView(getContext());
        textView.setSingleLine();
        textView.setEllipsize(null);
        textView.setPadding(10, 0, 0, 0);
        textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        textView.setCompoundDrawablePadding(10);
        return textView;
    }

}