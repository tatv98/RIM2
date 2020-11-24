package vn.icar.rim;

import java.util.LinkedList;
import java.util.List;

import lombok.val;

import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.SystemService;

import vn.icar.rim.activity.UsbStateActivity;
import vn.icar.rim.device.entitiy.AppInfo;
import vn.icar.rim.device.entitiy.TaskInfo;
import vn.icar.rim.service.SerialListenerService;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;

@EApplication
public class RemoteInputsMgr extends Application {

    public static final String PRIVATE_PERMISSION = "vn.icar.rim.permission.PRIVATE";

    public static final String ACTION_CONNECT = "vn.icar.rim.action.ACTION_CONNECT";
    public static final String ACTION_SETUP = "vn.icar.rim.action.ACTION_SETUP";

    public static final String ACTION_DATA_RECEIVE = "vn.icar.rim.action.ACTION_DATA_RECEIVE";
    public static final String ACTION_DATA_SEND = "vn.icar.rim.action.ACTION_DATA_SEND";

    public static final String EXTRA_DEVICE = "vn.icar.rim.serial.DEVICE";
    public static final String EXTRA_MODE = "vn.icar.rim.serial.MODE";
    public static final String EXTRA_BAUDRATE = "vn.icar.rim.serial.BAUDRATE";

    public static final String EXTRA_COMMAND = "vn.icar.rim.device.EXTRA_COMMAND";
    public static final String EXTRA_ARGS = "vn.icar.rim.device.EXTRA_ARGS";

    @SystemService UsbManager usbManager;

    private List<AppInfo> packages;
    private List<TaskInfo> tasks;

    @Override
    public void onCreate() {

        super.onCreate();

//        SerialListenerService.intent(this)
//                .extra(EXTRA_DEVICE, getDevice())
//                .extra(EXTRA_MODE, getConnectionMode())
//                .extra(EXTRA_BAUDRATE, getBaudrate())
//                .start();

        startService(new Intent(this,SerialListenerService.class)
        .putExtra(EXTRA_DEVICE, getDevice())
        .putExtra(EXTRA_MODE, getConnectionMode())
        .putExtra(EXTRA_BAUDRATE, getBaudrate()));

    }

    public List<AppInfo> getPackages() {

        if (packages == null) {
            getPackagesForce();
        }

        return packages;
    }

    public List<TaskInfo> getTasks() {

        if (tasks == null) {
            getTasksForce();
        }

        return tasks;
    }

    public List<AppInfo> getPackagesForce() {

        packages = new LinkedList<AppInfo>();
        for (PackageInfo pi : getPackageManager().getInstalledPackages(0)) {
            packages.add(new AppInfo(this, pi));
        }

        return packages;
    }

    public List<TaskInfo> getTasksForce() {

        tasks = new LinkedList<TaskInfo>();
        Cursor c = getContentResolver().query(Uri.parse("content://net.dinglisch.android.tasker/tasks"), null, null, null, null);
        if (c != null) {
            int projectCol = c.getColumnIndex("project_name");
            int nameCol = c.getColumnIndex("name");
            while (c.moveToNext()) {
                tasks.add(new TaskInfo(c.getString(projectCol), c.getString(nameCol)));
            }
            c.close();
        }

        return tasks;
    }

    public void refreshConnection() {

        Intent intent = new Intent(ACTION_CONNECT);

        intent.putExtra(EXTRA_DEVICE, getDevice());
        intent.putExtra(EXTRA_MODE, getConnectionMode());
        intent.putExtra(EXTRA_BAUDRATE, getBaudrate());

        sendBroadcast(intent, RemoteInputsMgr.PRIVATE_PERMISSION);
    }

    private SharedPreferences getSharedPreferences() {

        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private String getConnectionType() {

        return getSharedPreferences().getString("connection_type", "Bluetooth");
    }

    private String getConnectionMode() {

        return getSharedPreferences().getString("connection_mode", "Slave");
    }

    private String getDeviceAddress() {

        return getSharedPreferences().getString("device_id", "-1");
    }

    private Parcelable getDevice() {

        if ("-1".equals(getDeviceAddress())) {
            return null;
        }

        if ("Bluetooth".equals(getConnectionType())) {
            return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(getDeviceAddress());
        } else {
            return getDevice(this, Integer.valueOf(getDeviceAddress()));
        }
    }

    private int getBaudrate() {

        return Integer.valueOf(getSharedPreferences().getString("device_baudrate", "9600"));
    }

    private UsbDevice getDevice(Context context, int deviceId) {

        for (UsbDevice device : usbManager.getDeviceList().values()) {
            if (device.getDeviceId() == deviceId) {
                if (!usbManager.hasPermission(device)) {
//                    UsbStateActivity.intent(context)
//                            .extra(UsbManager.EXTRA_DEVICE, device)
//                            .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            .start();

                    startActivity(new Intent(this, UsbStateActivity.class)
                            .putExtra(UsbManager.EXTRA_DEVICE, device)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                    return null;
                }
                return device;
            }
        }
        return null;
    }

}
