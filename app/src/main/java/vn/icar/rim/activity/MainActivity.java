package vn.icar.rim.activity;

import java.sql.SQLException;

import lombok.val;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.UiThread.Propagation;
import org.androidannotations.annotations.ViewById;
import vn.icar.rim.R;
import vn.icar.rim.RemoteInputsMgr;
import vn.icar.rim.adapter.ButtonInfoAdapter;
import vn.icar.rim.device.DBFactory;
import vn.icar.rim.fragment.ButtonInfoDialogFragment;

import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener.DismissCallbacks;

@EActivity(R.layout.main)
public class MainActivity extends ListActivity implements DismissCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String ACTION_DATA_REFRESH = "vn.icar.rim.action.ACTION_DATA_REFRESH";

    @App RemoteInputsMgr app;

    @Bean DBFactory dbFactory;

    @Bean ButtonInfoAdapter buttonInfoAdapter;

    @ViewById(android.R.id.icon) ImageView loadingIcon;

    @AfterViews
    void onInit() {
        app.getPackagesForce();
        app.getTasksForce();

        setListAdapter(buttonInfoAdapter);

        getListView().setOnTouchListener(new SwipeDismissListViewTouchListener(getListView(), this));
    }

    @Override
    protected void onResume() {

        super.onResume();

        Intent intent = new Intent(RemoteInputsMgr.ACTION_SETUP);
        intent.putExtra("setup", true);
        sendBroadcast(intent, RemoteInputsMgr.PRIVATE_PERMISSION);
    }

    @Override
    protected void onPause() {

        Intent intent = new Intent(RemoteInputsMgr.ACTION_SETUP);
        intent.putExtra("setup", false);
        sendBroadcast(intent, RemoteInputsMgr.PRIVATE_PERMISSION);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem button;

        button = menu.add(R.string.menu_button_settings);
        button.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        button.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {

                FragmentManager fm = getFragmentManager();
                if (fm.findFragmentByTag("dialog") == null) {
                    new ButtonInfoDialogFragment().show(fm, "dialog");
                }

                return false;
            }
        });

        button = menu.add(R.string.app_settings);
        button.setIcon(R.drawable.ic_sysbar_quicksettings);
        button.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        button.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {

//                PreferencesActivity.intent(MainActivity.this).start();

                startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onListItemClick(ListView listView, View v, int pos, long id) {

        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentByTag("dialog") == null) {
//            ButtonInfoDialogFragment.builder().arg("buttonId", id).build().show(fm, "dialog");
            ButtonInfoDialogFragment bidf = new ButtonInfoDialogFragment();
            bidf.show(fm, "dialog");
        }
    }

    @Override
    public void onDismiss(ListView listView, int position) {

        try {
            dbFactory.getButtonDao().deleteById(buttonInfoAdapter.getItemId(position));
        } catch (SQLException e) {
            Log.e(TAG, "failed to delete button", e);
        } finally {
            buttonInfoAdapter.refresh();
        }
    }

    @Receiver(actions = RemoteInputsMgr.ACTION_DATA_RECEIVE)
    void onCommand(Intent intent) {

        showMessage(intent.getStringExtra(RemoteInputsMgr.EXTRA_COMMAND) + ":" + intent.getStringExtra(RemoteInputsMgr.EXTRA_ARGS));
    }

    @Receiver(actions = MainActivity.ACTION_DATA_REFRESH)
    void onRefresh() {

        buttonInfoAdapter.refresh();
    }

    @UiThread(propagation = Propagation.REUSE)
    void showMessage(String message) {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
