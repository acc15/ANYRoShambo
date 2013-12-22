package com.appctek.anyroshambo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.appctek.R;
import com.appctek.anyroshambo.services.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    // to make sure it compiles using old android SDK jars
    private static final int FLAG_HARDWARE_ACCELERATED = 0x01000000;
    private static final int SDK_VERSION_HONEYCOMB = 11;

    private ShakeDetector shakeDetector = ServiceRepository.getRepository().getShakeDetector(this);
    private FadedSequentialAnimator sequentialAnimator = ServiceRepository.getRepository().getSequentialAnimator();

    private void setupGame() {
        shakeDetector.start(new ShakeDetector.ShakeListener() {
            public void onShake() {
                logger.info("Shake detected!!");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        shakeDetector.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        shakeDetector.resume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger.debug("Initializing main activity...");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // enable hardware acceleration
        if (Build.VERSION.SDK_INT >= SDK_VERSION_HONEYCOMB) {
            getWindow().setFlags(
                    FLAG_HARDWARE_ACCELERATED,
                    FLAG_HARDWARE_ACCELERATED);
        }

        final View contentView = findViewById(android.R.id.content);
        contentView.setBackgroundColor(Color.BLACK);

        setContentView(R.layout.main);

        final ImageView imageView = (ImageView) findViewById(R.id.splash);
        sequentialAnimator.start(new FadedSequentialAnimator.Action() {
            public View execute() {
                imageView.setImageResource(R.drawable.logoscreen);
                return imageView;
            }
        }, new FadedSequentialAnimator.Action() {
            public View execute() {
                final View gameView = getLayoutInflater().inflate(R.layout.game, null);
                setContentView(gameView);
                setupGame();
                return gameView;
            }
        });

    }


}

