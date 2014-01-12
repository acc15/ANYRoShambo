package com.appctek.anyroshambo.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-12-01
 */
public class MediaPlayerUtils {

    private static final Logger logger = LoggerFactory.getLogger(MediaPlayerUtils.class);

    public static void mute(MediaPlayer mediaPlayer) {
        mediaPlayer.setVolume(0, 0);
    }

    public static void unmute(MediaPlayer mediaPlayer) {
        mediaPlayer.setVolume(1, 1);
    }

    public static void play(Context context, MediaPlayer mediaPlayer, int resId) {
        mediaPlayer.reset();

        final AssetFileDescriptor afd = context.getResources().openRawResourceFd(resId);
        if (afd == null) {
            throw new RuntimeException("Raw resource with id " + resId + " is compressed");
        }
        try {
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            throw new RuntimeException("Can't read audio resource with id "  +resId, e);
        } finally {
            try {
                afd.close();
            } catch (IOException e) {
                logger.error("Can't close AssetFileDescriptor", e);
            }
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException("Can't prepare mediaPlayer", e);
        }
        mediaPlayer.start();
    }

}
