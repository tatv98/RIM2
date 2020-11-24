package vn.icar.rim.activity;

import java.util.LinkedList;
import java.util.List;

import lombok.val;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import vn.icar.rim.R;
import vn.icar.rim.RemoteInputsMgr;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.physicaloid.lib.UsbVidList;

@SuppressLint("DefaultLocale")
@EActivity
public class PreferencesActivity extends PreferenceActivity {

    @App
    static RemoteInputsMgr app;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        private SharedPreferences prefs;

        private String type;
        private String mode;
        private String devide;
        private String bandrate;

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            type = prefs.getString("connection_type", "Bluetooth");
            mode = prefs.getString("connection_mode", "Slave");
            devide = prefs.getString("device_id", "-1");
            bandrate = prefs.getString("device_baudrate", "9600");

            initDevicesAdapter(prefs.getString("connection_type", "Bluetooth"), prefs.getString("device_id", "-1"));

            prefs.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

            if ("connection_type".equals(key)) {
                initDevicesAdapter(prefs.getString(key, "Bluetooth"), "-1");
            }
        }

        @Override
        public void onDestroy() {

            prefs.unregisterOnSharedPreferenceChangeListener(this);

            if (!type.equals(prefs.getString("connection_type", "Bluetooth")) ||
                    !mode.equals(prefs.getString("connection_mode", "Slave")) ||
                    !devide.equals(prefs.getString("device_id", "-1")) ||
                    !bandrate.equals(prefs.getString("device_baudrate", "9600"))) {

                app.refreshConnection();
            }

            super.onDestroy();
        }

        private void initDevicesAdapter(String connectionType, String deviceId) {

            List<CharSequence> entries = new LinkedList<CharSequence>();
            List<CharSequence> entryValues = new LinkedList<CharSequence>();

            if ("Bluetooth".equals(connectionType)) {
                bindBluetoothDevices(entries, entryValues);
            } else {
                bindUsbDevices(entries, entryValues);
            }

            ListPreference devices = (ListPreference) findPreference("device_id");
            devices.setEntries(entries.toArray(new CharSequence[0]));
            devices.setEntryValues(entryValues.toArray(new CharSequence[0]));
            devices.setValue(deviceId);

            boolean isBluetooth = "Bluetooth".equals(connectionType);

            findPreference("connection_mode").setEnabled(isBluetooth);
            findPreference("device_baudrate").setEnabled(!isBluetooth);
        }

        private void bindBluetoothDevices(List<CharSequence> entries, List<CharSequence> entryValues) {

            BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
            if (ba == null) {
                return;
            }

            for (BluetoothDevice device : ba.getBondedDevices()) {
                entries.add(device.getName() + ": " + device.getAddress());
                entryValues.add(String.valueOf(device.getAddress()));
            }
        }

        private void bindUsbDevices(List<CharSequence> entries, List<CharSequence> entryValues) {

            UsbManager manager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
            for (UsbDevice device : manager.getDeviceList().values()) {
                for (UsbVidList vid : UsbVidList.values()) {
                    if (vid.getVid() == device.getVendorId()) {
                        entries.add(String.format("%04X", device.getVendorId()).toUpperCase() + ":" + String.format("%04X", device.getProductId()).toUpperCase() + " " + device.getDeviceName());
                        entryValues.add(String.valueOf(device.getDeviceId()));
                        break;
                    }
                }
            }
        }

    }

}
