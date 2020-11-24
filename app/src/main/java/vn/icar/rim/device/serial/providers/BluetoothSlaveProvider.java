package vn.icar.rim.device.serial.providers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

public class BluetoothSlaveProvider extends SerialProvider {

    public static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private SerialReaderThread serialReader;
    private SerialWriterThread serialWriter;
    private AcceptConnection acceptConnection;

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;

    private String commandBuffer;

    public BluetoothSlaveProvider(Context context, BluetoothDevice device) {

        super(context);

        this.device = device;
    }

    @Override
    public synchronized boolean open() {

        if (acceptConnection != null) {
            acceptConnection.interrupt();
            acceptConnection = null;
        }

        commandBuffer = "";

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null || !ba.isEnabled()) {
            return false;
        }

        if (connect()) {
            return true;
        }

        acceptConnection = new AcceptConnection();
        acceptConnection.start();

        return false;
    }

    @Override
    public synchronized void close() {

        closeSocket();

        if (acceptConnection != null) {
            acceptConnection.cancel();
            acceptConnection = null;
        }
    }

    @Override
    public synchronized void send(String data) {

        if (serialWriter != null) {
            serialWriter.write(data);
        }
    }

    private synchronized boolean connect() {

        if (serialReader != null) {
            serialReader.cancel();
            serialReader = null;
        }
        if (serialWriter != null) {
            serialWriter.cancel();
            serialWriter = null;
        }

        if (!openSocket()) {
            return false;
        }

        serialReader = new SerialReaderThread(in);
        serialReader.start();
        serialWriter = new SerialWriterThread(out);
        serialWriter.start();

        return true;
    }

    private synchronized void disconnect() {

        closeSocket();

        acceptConnection = new AcceptConnection();
        acceptConnection.start();
    }

    private boolean openSocket() {

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();

        ba.cancelDiscovery();

        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(SPP);
        } catch (Exception e) {
            socket = null;
            return false;
        }

        ba.cancelDiscovery();

        try {
            socket.connect();
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (Exception e) {
            try {
                socket.close();
            } catch (Exception e1) {}
            socket = null;
            return false;
        }

        return true;
    }

    private void closeSocket() {

        if (serialReader != null) {
            serialReader.cancel();
            serialReader = null;
        }
        if (serialWriter != null) {
            serialWriter.cancel();
            serialWriter = null;
        }

        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {}
        }

        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {}
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {}
        }

        socket = null;
    }

    class AcceptConnection extends Thread {

        private boolean canceled = false;

        public AcceptConnection() {

            super("AcceptConnection");
        }

        public void cancel() {

            canceled = true;
        }

        @Override
        public void run() {

            while (device != null && socket == null) {
                if (canceled) {
                    return;
                }
                connect();
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {}
            }
        }

    }

    class SerialReaderThread extends Thread {

        private boolean canceled;
        private final InputStream in;
        private int bytes;
        private byte[] buffer = new byte[1024];

        public SerialReaderThread(InputStream in) {

            super("SerialReader");

            this.in = in;
        }

        public void cancel() {

            canceled = true;
        }

        @Override
        public void run() {

            while (!canceled) {
                try {
                    bytes = in.read(buffer);

                    commandBuffer += new String(buffer, 0, bytes);

                    Pattern p = Pattern.compile("^.*<([^>]+)>", Pattern.MULTILINE);
                    Matcher m = p.matcher(commandBuffer);
                    while (m.find()) {
                        String event = m.group(1);

                        onDataReceive(event);

                        commandBuffer = m.replaceFirst("");
                        m = p.matcher(commandBuffer);
                    }
                } catch (IOException e) {
                    break;
                } catch (Exception e) {}
            }
        }

    }

    class SerialWriterThread extends Thread {

        private boolean canceled;
        private final OutputStream out;
        private final BlockingQueue<String> queue;

        public SerialWriterThread(OutputStream output) {

            super("SerialWriter");

            out = output;
            queue = new LinkedBlockingQueue<String>();
        }

        @Override
        public void run() {

            while (!canceled) {
                try {
                    String data = queue.take();
                    out.write((data + "\r\n").getBytes());
                } catch (Exception e) {
                    break;
                }
            }

            if (!canceled) {
                disconnect();
            }
        }

        public void write(String data) {

            queue.add(data);
        }

        public void cancel() {

            canceled = true;
            queue.add("");
        }

    }

}
