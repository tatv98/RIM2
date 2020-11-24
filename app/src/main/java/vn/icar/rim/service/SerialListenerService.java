package vn.icar.rim.service;

import lombok.val;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;
import vn.icar.rim.R;
import vn.icar.rim.RemoteInputsMgr;
import vn.icar.rim.activity.MainActivity;
import vn.icar.rim.device.DBFactory;
import vn.icar.rim.device.actions.ActionExecutorFactory;
import vn.icar.rim.device.actions.CommandType;
import vn.icar.rim.device.actions.executors.ActionExecutor;
import vn.icar.rim.device.entitiy.ActionInfo;
import vn.icar.rim.device.entitiy.ActionInfo.EventType;
import vn.icar.rim.device.entitiy.ButtonInfo;
import vn.icar.rim.device.serial.ISerialDataReciever;
import vn.icar.rim.device.serial.SerialProviderFactory;
import vn.icar.rim.device.serial.providers.SerialProvider;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.os.Parcelable;

import com.j256.ormlite.stmt.QueryBuilder;

@EService
public class SerialListenerService extends Service implements ISerialDataReciever {

    @App RemoteInputsMgr app;

    @Bean SerialProviderFactory providerFactory;
    @Bean ActionExecutorFactory actionFactory;
    @Bean DBFactory dbFactory;

    private boolean setupMode;

    private Parcelable device;
    private String mode;
    private int baudrate;
    private SerialProvider provider;

    @Override
    public void onCreate() {

        super.onCreate();

        closeProvider();
        openProvider();
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        startForeground(1, new Notification.Builder(this)
//                .setAutoCancel(false)
//                .setOngoing(true)
//                .setPriority(-1)
//                .setSmallIcon(R.drawable.ic_launcher)
//                .setContentTitle(getResources().getString(R.string.app_name))
//                .setContentIntent(PendingIntent.getActivity(this, 0,
//                        MainActivity.intent(this).action(Intent.ACTION_MAIN).get(),
//                        PendingIntent.FLAG_UPDATE_CURRENT))
//                .build());
        startForeground(1, new Notification.Builder(this)
                .setAutoCancel(false)
                .setOngoing(true)
                .setPriority(-1)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class).setAction(Intent.ACTION_MAIN),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .build());

        if (intent.hasExtra(RemoteInputsMgr.EXTRA_DEVICE)) {
            onReconnectAction(intent);
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onDestroy() {

        if (provider != null) {
            provider.close();
        }

        if (dbFactory != null) {
            dbFactory.close();
        }

        super.onDestroy();
    }

    @Receiver(actions = { BluetoothAdapter.ACTION_STATE_CHANGED, UsbManager.ACTION_USB_DEVICE_DETACHED })
    void onDeviceEvent(Intent intent) {

        String action = intent.getAction();

        if (BluetoothDevice.class.isInstance(device) && BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                case BluetoothAdapter.STATE_ON:
                    closeProvider();
                    openProvider();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    closeProvider();
                    break;
            }
        } else

        if (UsbDevice.class.isInstance(device) && UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            UsbDevice cdevice = (UsbDevice) device;
            UsbDevice ddevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (ddevice != null && ddevice.getDeviceId() == cdevice.getDeviceId()) {
                if (provider != null) {
                    provider.setCommandReciever(null);
                    provider.close();
                }
            }
        }
    }

    @Receiver(actions = RemoteInputsMgr.ACTION_CONNECT)
    void onReconnectAction(Intent intent) {

        device = intent.getParcelableExtra(RemoteInputsMgr.EXTRA_DEVICE);
        mode = intent.getStringExtra(RemoteInputsMgr.EXTRA_MODE);
        baudrate = intent.getIntExtra(RemoteInputsMgr.EXTRA_BAUDRATE, 9600);

        closeProvider();
        openProvider();
    }

    @Receiver(actions = RemoteInputsMgr.ACTION_SETUP)
    void onSetupMode(Intent intent) {

        setupMode = intent.getBooleanExtra("setup", false);
    }

    @Receiver(actions = RemoteInputsMgr.ACTION_DATA_SEND)
    void onDataSend(Intent intent) {

        provider.send(String.format("<%s:%s>",
                intent.getStringExtra(RemoteInputsMgr.EXTRA_COMMAND),
                intent.getStringExtra(RemoteInputsMgr.EXTRA_ARGS)));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onDataRecieve(String command) {

        String[] parts = command.split(":");

        Intent intent = new Intent(RemoteInputsMgr.ACTION_DATA_RECEIVE);
        intent.putExtra(RemoteInputsMgr.EXTRA_COMMAND, parts[0]);
        intent.putExtra(RemoteInputsMgr.EXTRA_ARGS, parts[1]);

        if (!setupMode) {
            try {
                CommandType cmd = CommandType.valueOf(parts[0].toUpperCase());

                QueryBuilder<ButtonInfo, Long> bBuilder = dbFactory.getButtonDao().queryBuilder();
                bBuilder.where().raw(String.format("%s BETWEEN value - error AND value + error", parts[1]));

                QueryBuilder<ActionInfo, Long> aBuilder = dbFactory.getActionDao().queryBuilder();
                aBuilder.leftJoin(bBuilder);

                switch (cmd) {
                    case CLICK:
                        aBuilder.where().eq("event", EventType.CLICK);
                        break;
                    case HOLD:
                    case RELEASE:
                        aBuilder.where().eq("event", EventType.HOLD);
                        break;
                }

                ActionInfo actionInfo = aBuilder.queryForFirst();
                if (actionInfo != null) {
                    ActionExecutor executor = actionFactory.getExecutor(actionInfo.getActionType());
                    executor.execute(cmd, actionInfo.getAction());
                } else {
                    sendBroadcast(intent);
                }
            } catch (Exception e) {
                sendBroadcast(intent);
            }
        } else {
            sendBroadcast(intent, RemoteInputsMgr.PRIVATE_PERMISSION);
        }
    }

    private void openProvider() {

        try {
            provider = providerFactory.getProvider(device, "Master".equals(mode));
            if (provider != null) {
                provider.setBaudrate(baudrate);
                provider.setCommandReciever(this);
                provider.open();
            }
        } catch (Exception e) {}
    }

    private void closeProvider() {

        if (provider != null) {
            provider.setCommandReciever(null);
            provider.close();
        }
    }

}
