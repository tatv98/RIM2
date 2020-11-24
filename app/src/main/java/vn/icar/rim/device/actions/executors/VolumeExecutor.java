package vn.icar.rim.device.actions.executors;

import java.util.Timer;
import java.util.TimerTask;

import vn.icar.rim.device.actions.CommandType;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;

public class VolumeExecutor extends ActionExecutor {

    private static Timer timer = new Timer();

    private int flags = 0;

    public enum VolumeAction {

        UP, DOWN, MUTE

    }

    public VolumeExecutor(Context context) {

        super(context);

        SharedPreferences prefs = context.getSharedPreferences("vn.icar.rim_preferences", Context.MODE_MULTI_PROCESS);

        if (prefs.getBoolean("volume_change_ui", false)) {
            flags |= AudioManager.FLAG_SHOW_UI;
        }
        if (prefs.getBoolean("volume_change_sound", false)) {
            flags |= AudioManager.FLAG_PLAY_SOUND;
        }
    }

    @Override
    public void execute(CommandType command, String action) {

        SharedPreferences prefs = getContext().getSharedPreferences("vn.icar.rim_preferences", Context.MODE_MULTI_PROCESS);
        final AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        int volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        switch (Enum.valueOf(VolumeAction.class, action)) {
            case UP:
                switch (command) {
                    case CLICK:
                        adjustVolume(am, AudioManager.ADJUST_RAISE);
                        break;
                    case HOLD:
                        timer.schedule(new TimerTask() {

                            @Override
                            public void run() {

                                adjustVolume(am, AudioManager.ADJUST_RAISE);
                            }
                        }, 0, 100);
                        break;
                    case RELEASE:
                        timer.cancel();
                        timer = new Timer();
                        break;
                }
                break;
            case DOWN:
                switch (command) {
                    case CLICK:
                        adjustVolume(am, AudioManager.ADJUST_LOWER);
                        break;
                    case HOLD:
                        timer.schedule(new TimerTask() {

                            @Override
                            public void run() {

                                adjustVolume(am, AudioManager.ADJUST_LOWER);
                            }
                        }, 0, 100);
                        break;
                    case RELEASE:
                        timer.cancel();
                        timer = new Timer();
                        break;
                }
                break;
            case MUTE:
                if (command != CommandType.RELEASE) {
                    if (volume > 0) {
                        Editor editor = prefs.edit();
                        editor.putInt("volume", volume);
                        editor.commit();
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, flags);
                    } else {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, prefs.getInt("volume", 5), flags);
                    }
                }
                break;
        }
    }

    private void adjustVolume(AudioManager am, int direction) {

        int volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume + direction, flags);
    }

}
