package vn.icar.rim.activity;

import lombok.val;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import vn.icar.rim.RemoteInputsMgr;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

@EActivity
public class UsbStateActivity extends Activity {

    @App RemoteInputsMgr app;

    @SystemService UsbManager usbManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if ("USB".equals(prefs.getString("connection_type", "Bluetooth"))) {
            UsbDevice device = (UsbDevice) getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device == null) {
                finish();
                return;
            }

            if (Integer.valueOf(prefs.getString("device_id", "-1")) == -1) {
                prefs.edit().putString("device_id", String.valueOf(device.getDeviceId())).commit();
            }

            if (device.getDeviceId() == Integer.valueOf(prefs.getString("device_id", "-1"))) {
                if (!usbManager.hasPermission(device)) {
//                    usbManager.requestPermission(device, PendingIntent.getActivity(this, 0,
//                            UsbStateActivity.intent(this).flags(Intent.FLAG_ACTIVITY_NEW_TASK).get(),
//                            PendingIntent.FLAG_UPDATE_CURRENT));
                    usbManager.requestPermission(device, PendingIntent.getActivity(this, 0,
                            getIntent().setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            PendingIntent.FLAG_UPDATE_CURRENT));
                } else {
                    app.refreshConnection();
                }
            }
        }

        finish();
    }

}
