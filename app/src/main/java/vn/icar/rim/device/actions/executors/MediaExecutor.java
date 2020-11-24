package vn.icar.rim.device.actions.executors;

import vn.icar.rim.device.actions.CommandType;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MediaExecutor extends ActionExecutor {

    public enum MediaAction {

        REWIND, PREVIOUS, PLAYPAUSE, NEXT, FAST_FORWARD

    }

    public MediaExecutor(Context context) {

        super(context);
    }

    @Override
    public void execute(CommandType command, String action) {

        switch (Enum.valueOf(MediaAction.class, action)) {
            case REWIND:
                switch (command) {
                    case CLICK:
                        sendBroadcast(KeyEvent.KEYCODE_MEDIA_REWIND, true, true);
                        break;
                    case HOLD:
                        sendBroadcast(KeyEvent.KEYCODE_MEDIA_REWIND, true, false);
                        break;
                    case RELEASE:
                        sendBroadcast(KeyEvent.KEYCODE_MEDIA_REWIND, false, true);
                        break;
                }
                break;
            case PREVIOUS:
                if (command != CommandType.RELEASE) {
                    sendBroadcast(KeyEvent.KEYCODE_MEDIA_PREVIOUS, true, true);
                }
                break;
            case PLAYPAUSE:
                if (command != CommandType.RELEASE) {
                    sendBroadcast(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, true, true);
                }
                break;
            case NEXT:
                if (command != CommandType.RELEASE) {
                    sendBroadcast(KeyEvent.KEYCODE_MEDIA_NEXT, true, true);
                }
                break;
            case FAST_FORWARD:
                switch (command) {
                    case CLICK:
                        sendBroadcast(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, true, true);
                        break;
                    case HOLD:
                        sendBroadcast(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, true, false);
                        break;
                    case RELEASE:
                        sendBroadcast(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, false, true);
                        break;
                }
                break;
        }

    }

    private void sendBroadcast(int keyEvent, boolean push, boolean release) {

        Intent intent;

        if (push) {
            intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyEvent));
            getContext().sendBroadcast(intent);
        }
        if (release) {
            intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyEvent));
            getContext().sendBroadcast(intent);
        }
    }

}
