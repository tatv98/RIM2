package vn.icar.rim.device.serial;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import vn.icar.rim.device.serial.providers.BluetoothMasterProvider;
import vn.icar.rim.device.serial.providers.BluetoothSlaveProvider;
import vn.icar.rim.device.serial.providers.SerialProvider;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Parcelable;

import vn.icar.rim.device.serial.providers.SerialProvider;
import vn.icar.rim.device.serial.providers.USBProvider;

@EBean
public class SerialProviderFactory {

    @RootContext Context context;

    public SerialProvider getProvider(Parcelable device, boolean master) {

        if (device != null) {
            if (BluetoothDevice.class.equals(device.getClass())) {
                if (master) {
                    return new BluetoothMasterProvider(context, (BluetoothDevice) device);
                } else {
                    return new BluetoothSlaveProvider(context, (BluetoothDevice) device);
                }
            }
            if (UsbDevice.class.equals(device.getClass())) {
                return new USBProvider(context, (UsbDevice) device);
            }
        }

        return null;
    }

}
