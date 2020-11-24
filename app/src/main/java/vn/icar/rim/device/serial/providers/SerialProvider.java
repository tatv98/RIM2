package vn.icar.rim.device.serial.providers;

import lombok.val;

import vn.icar.rim.device.serial.ISerialDataReciever;

import android.content.Context;

public abstract class SerialProvider {

    private final Context context;
    private int baudrate;
    private ISerialDataReciever listener;

    public SerialProvider(Context context) {

        this.context = context;
    }

    public Context getContext() {

        return context;
    }

    public int getBaudrate() {

        return baudrate;
    }

    public void setBaudrate(int baudrate) {

        this.baudrate = baudrate;
    }

    public ISerialDataReciever getCommandReciever() {

        return listener;
    }

    public void setCommandReciever(ISerialDataReciever listener) {

        this.listener = listener;
    }

    protected void onDataReceive(String data) {

        ISerialDataReciever listener = getCommandReciever();
        if (listener != null) {
            listener.onDataRecieve(data);
        }
    }

    public abstract boolean open();

    public abstract void close();

    public abstract void send(String data);

}
