package com.appctek.anyroshambo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Base64;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.appctek.anyroshambo.roboguice.EventRoboActivity;
import com.appctek.anyroshambo.services.AdService;
import com.appctek.anyroshambo.util.ViewUtils;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roboguice.inject.InjectResource;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
class FullScreenActivity extends EventRoboActivity {

    private static final Logger logger = LoggerFactory.getLogger(FullScreenActivity.class);

    @Inject
    private AdService adService;

    @InjectResource(R.string.app_name)
    private String appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            final StringBuilder stringBuilder = new StringBuilder();
            printSignatureAndHashes(stringBuilder);
            logger.debug(stringBuilder.toString());
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        adService.init(this);
    }

    private static void printFields(Class<?> clazz, StringBuilder sb, String prefix) {
        for (final Field f : clazz.getFields()) {
            final Object val;
            try {
                val = f.get(null);
            } catch (IllegalAccessException e) {
                logger.error("Can't get field \"" + f.getName() + "\" value", e);
                continue;
            }
            sb.append(prefix).append('.').append(f.getName()).append(": ").append(val).append('\n');
        }
    }

    private void copyToClipboard(String text) {
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(text);
    }

    private void printSignatureAndHashes(StringBuilder sb) {
        try {
            final String appPackageName = getApplicationInfo().packageName;
            final PackageInfo info = getPackageManager().getPackageInfo(appPackageName, PackageManager.GET_SIGNATURES);
            sb.append("Signature KeyHashes: [");
            for (int i=0; i<info.signatures.length; i++) {
                final MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(info.signatures[i].toByteArray());
                final String keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(keyHash);
            }
            sb.append(']');

        } catch (PackageManager.NameNotFoundException e) {
            logger.error("Can't print KeyHash", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Can't print KeyHash", e);
        }
    }

    public void initContainer(ViewGroup rootContainer) {
        if (BuildConfig.DEBUG) {
            final TextView textView = new TextView(this);
            textView.setText("Version: " + AppBuild.VERSION);
            textView.setTextColor(Color.WHITE);
            textView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    final StringBuilder info = new StringBuilder();
                    info.append("App Version: ").append(AppBuild.VERSION).append('\n');
                    info.append("App Ad Enabled: ").append(AppBuild.AD_ENABLED).append('\n');
                    printFields(Build.class, info, "Build");
                    printFields(Build.VERSION.class, info, "SDK");
                    printSignatureAndHashes(info);

                    new AlertDialog.Builder(FullScreenActivity.this).
                            setTitle("Debug info").
                            setMessage(info).
                            setPositiveButton("Send", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();

                                    final Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("message/rfc822");
                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"vyacheslav.mayorov@gmail.com"});
                                    intent.putExtra(Intent.EXTRA_SUBJECT, appName + " Debug info");
                                    intent.putExtra(Intent.EXTRA_TEXT, info.toString());
                                    try {
                                        startActivity(Intent.createChooser(intent, "Send debug info"));
                                    } catch (android.content.ActivityNotFoundException ex) {
                                        logger.error("Can't send debug info", ex);
                                    }

                                }
                            }).
                            setNeutralButton("Copy", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    copyToClipboard(info.toString());
                                    Toast.makeText(FullScreenActivity.this,
                                            "Debug info copied", Toast.LENGTH_SHORT).show();
                                }
                            }).
                            setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).
                            show();

                }
            });
            ViewUtils.addViewToContainer(rootContainer, textView, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        }
        adService.addBanner(rootContainer);
    }

}
