package vn.icar.rim.device.serial.providers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.val;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

public class BluetoothMasterProvider extends SerialProvider {

    public static final UUID PROFILE_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private AcceptConnection acceptConnection;
    private SerialReaderThread serialReader;
    private SerialWriterThread serialWriter;

    private BluetoothAdapter ba;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;

    private String commandBuffer;

    public BluetoothMasterProvider(Context context, BluetoothDevice device) {

        super(context);

        ba = BluetoothAdapter.getDefaultAdapter();

        this.device = device;
    }

    @Override
    public synchronized boolean open() {

        close();

        commandBuffer = "";

        if (ba == null || !ba.isEnabled()) {
            return false;
        }

        acceptConnection = new AcceptConnection();
        acceptConnection.start();

        return true;
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

    private synchronized void disconnect() {

        open();
    }

    private boolean manageSocket(BluetoothSocket socket) {

        if (serialReader != null) {
            serialReader.cancel();
            serialReader = null;
        }

        if (serialWriter != null) {
            serialWriter.cancel();
            serialWriter = null;
        }

        ba.cancelDiscovery();

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (Exception e) {
            try {
                socket.close();
            } catch (Exception e1) {}
            socket = null;
            return false;
        }

        serialReader = new SerialReaderThread(in);
        serialReader.start();

        serialWriter = new SerialWriterThread(out);
        serialWriter.start();

        return true;
    }

    private void closeSocket() {

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

        if (serialReader != null) {
            serialReader.cancel();
            serialReader = null;
        }

        if (serialWriter != null) {
            serialWriter.cancel();
            serialWriter = null;
        }
    }

    class AcceptConnection extends Thread {

        private BluetoothServerSocket serverSocket;

        public AcceptConnection() {

            super("AcceptConnection");

            try {
                serverSocket = ba.listenUsingRfcommWithServiceRecord(getContext().getPackageName(), PROFILE_SPP);
            } catch (IOException e) {}
        }

        public void run() {

            while (serverSocket != null) {
                try {
                    BluetoothSocket socket = serverSocket.accept();
                    if (!isValid(socket)) {
                        disconnect();
                    }
                    if (isValid(socket) && manageSocket(socket)) {
                        return;
                    }
                } catch (IOException e) {
                    break;
                }
            }

            cancel();
        }

        private boolean isValid(BluetoothSocket socket) {

            return socket != null && socket.getRemoteDevice().getAddress().equals(device.getAddress());
        }

        public void cancel() {

            try {
                if (serverSocket != null) {
                    serverSocket.close();
                    serverSocket = null;
                }
            } catch (IOException e) {}
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

            disconnect();
        }

        public void cancel() {

            canceled = true;
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
