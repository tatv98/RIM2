package vn.icar.rim.device.serial.providers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;
import com.physicaloid.lib.usb.driver.uart.UartConfig;

public class USBProvider extends SerialProvider implements ReadLisener {

    private static final String TAG = USBProvider.class.getSimpleName();

    private UsbDevice device;
    private Physicaloid usbSerial;

    private String serialEventBuff = "";

    public USBProvider(Context context, UsbDevice device) {

        super(context);

        this.device = device;
    }

    @Override
    public boolean open() {

        if (usbSerial != null && usbSerial.close()) {
            usbSerial.clearReadListener();
        }

        serialEventBuff = "";

        usbSerial = new Physicaloid(getContext(), device);

        if (usbSerial.open(new UartConfig(getBaudrate(), UartConfig.DATA_BITS8, UartConfig.STOP_BITS1, UartConfig.PARITY_NONE, true, false))) {
            usbSerial.addReadListener(this);
        }

        return usbSerial.isOpened();
    }

    @Override
    public void close() {

        serialEventBuff = "";

        if (usbSerial != null && usbSerial.close()) {
            usbSerial.clearReadListener();
        }
    }

    @Override
    public synchronized void send(String data) {

        try {
            usbSerial.write((data + "\r\n").getBytes());
        } catch (Exception e) {
            Log.e(TAG, "failed to write serial", e);
        }
    }

    @Override
    public void onRead(int size) {

        synchronized (this) {
            try {
                byte[] buf = new byte[size];
                usbSerial.read(buf, size);
                serialEventBuff += new String(buf);

                Pattern p = Pattern.compile("^.*<([^>]+)>", Pattern.MULTILINE);
                Matcher m = p.matcher(serialEventBuff);
                while (m.find()) {
                    String event = m.group(1);

                    onDataReceive(event);

                    serialEventBuff = m.replaceFirst("");
                    m = p.matcher(serialEventBuff);
                }
            } catch (Exception e) {
                Log.e(TAG, "failed to read serial", e);
                return;
            }
        }
    }

}
