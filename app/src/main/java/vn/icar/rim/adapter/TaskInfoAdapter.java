package vn.icar.rim.adapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;
import vn.icar.rim.R;
import vn.icar.rim.RemoteInputsMgr;
import vn.icar.rim.device.entitiy.TaskInfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@EBean
public class TaskInfoAdapter extends ArrayAdapter<TaskInfo> {

    @App RemoteInputsMgr app;

    private Drawable sDrawable;
    private Drawable bDrawable;

    public TaskInfoAdapter(Context context) {

        super(context, -1);
    }

    @AfterInject
    void initAdapter() {

        addAll(app.getTasks());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView;

        if (convertView == null) {
            textView = createTextView();
        } else {
            textView = (TextView) convertView;
        }

        TaskInfo info = getItem(position);

        textView.setCompoundDrawablesWithIntrinsicBounds(getSDrawable(), null, null, null);
        textView.setText(info.getName());

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

        TaskInfo info = getItem(position);

        textView.setCompoundDrawablesWithIntrinsicBounds(getBDrawable(), null, null, null);
        textView.setText(info.getName());

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

    public Drawable getSDrawable() {

        if (sDrawable == null) {
            Drawable drawable = getContext().getResources().getDrawable(R.drawable.ic_tasker);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            sDrawable = new BitmapDrawable(getContext().getResources(), Bitmap.createScaledBitmap(bitmap, 24, 24, true));
        }

        return sDrawable;
    }

    public Drawable getBDrawable() {

        if (bDrawable == null) {
            Drawable drawable = getContext().getResources().getDrawable(R.drawable.ic_tasker);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            bDrawable = new BitmapDrawable(getContext().getResources(), Bitmap.createScaledBitmap(bitmap, 56, 56, true));
        }

        return bDrawable;
    }

}