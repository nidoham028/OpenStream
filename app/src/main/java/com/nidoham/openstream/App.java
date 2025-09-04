package com.nidoham.openstream;

import android.app.Application;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.jakewharton.processphoenix.ProcessPhoenix;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.util.BridgeStateSaverInitializer;
import org.schabi.newpipe.util.StateSaver;
import org.schabi.newpipe.util.ServiceHelper;
import org.schabi.newpipe.util.image.ImageStrategy;
import org.schabi.newpipe.util.image.PicassoHelper;
import org.schabi.newpipe.util.image.PreferredImageQuality;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class App extends Application {
    private static final String TAG = App.class.toString();
    private static App app;
    private boolean isFirstRun = false;

    @NonNull
    public static App getApp() { return app; }

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
        initACRA();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        if (ProcessPhoenix.isPhoenixProcess(this)) {
            Log.i(TAG, "Phoenix process detected, skipping init");
            return;
        }

        final int lastUsedPrefVersion = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.last_used_preferences_version), -1);
        isFirstRun = lastUsedPrefVersion == -1;

        // Removed NewPipeSettings init

        NewPipe.init(null, null, null); // downloader/localization can be added if needed

        BridgeStateSaverInitializer.init(this);
        StateSaver.init(this);
        initNotificationChannels();

        ServiceHelper.initServices(this);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PicassoHelper.init(this);
        ImageStrategy.setPreferredImageQuality(PreferredImageQuality.fromPreferenceKey(this,
                prefs.getString(getString(R.string.image_quality_key),
                        getString(R.string.image_quality_default))));
        PicassoHelper.setIndicatorsEnabled(BuildConfig.DEBUG
                && prefs.getBoolean(getString(R.string.show_image_indicators_key), false));

        configureRxJavaErrorHandler();

        // Removed PoTokenProviderImpl usage
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PicassoHelper.terminate();
    }

    private void configureRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) {
                Log.e(TAG, "RxJavaPlugins error: " + throwable.getClass().getName());

                Throwable actualThrowable = throwable instanceof UndeliverableException
                        ? Objects.requireNonNull(throwable.getCause())
                        : throwable;

                final List<Throwable> errors = actualThrowable instanceof CompositeException
                        ? ((CompositeException) actualThrowable).getExceptions()
                        : List.of(actualThrowable);

                for (Throwable error : errors) {
                    if (isThrowableIgnored(error)) return;
                    if (isThrowableCritical(error)) {
                        reportException(error);
                        return;
                    }
                }
                Log.e(TAG, "Unhandled RxJava exception", actualThrowable);
            }

            private boolean isThrowableIgnored(Throwable throwable) {
                return false; // ExceptionUtils removed, ignore network exceptions if needed
            }

            private boolean isThrowableCritical(Throwable throwable) {
                return true; // treat all exceptions as critical or customize
            }

            private void reportException(Throwable throwable) {
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), throwable);
            }
        });
    }

    protected void initACRA() {
        if (ACRA.isACRASenderServiceProcess()) return;
        final CoreConfigurationBuilder acraConfig = new CoreConfigurationBuilder()
                .withBuildConfigClass(BuildConfig.class);
        ACRA.init(this, acraConfig);
    }

    private void initNotificationChannels() {
        try {
            final android.app.NotificationManager notificationManager =
                    (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager == null) return;

            createChannel(notificationManager,
                    getString(R.string.notification_channel_id),
                    getString(R.string.notification_channel_name),
                    getString(R.string.notification_channel_description),
                    android.app.NotificationManager.IMPORTANCE_LOW);

            createChannel(notificationManager,
                    getString(R.string.app_update_notification_channel_id),
                    getString(R.string.app_update_notification_channel_name),
                    getString(R.string.app_update_notification_channel_description),
                    android.app.NotificationManager.IMPORTANCE_LOW);

            createChannel(notificationManager,
                    getString(R.string.hash_channel_id),
                    getString(R.string.hash_channel_name),
                    getString(R.string.hash_channel_description),
                    android.app.NotificationManager.IMPORTANCE_HIGH);

            createChannel(notificationManager,
                    getString(R.string.error_report_channel_id),
                    getString(R.string.error_report_channel_name),
                    getString(R.string.error_report_channel_description),
                    android.app.NotificationManager.IMPORTANCE_LOW);

            createChannel(notificationManager,
                    getString(R.string.streams_notification_channel_id),
                    getString(R.string.streams_notification_channel_name),
                    getString(R.string.streams_notification_channel_description),
                    android.app.NotificationManager.IMPORTANCE_DEFAULT);

        } catch (Exception e) {
            Log.e(TAG, "Failed to init notification channels", e);
        }
    }

    private void createChannel(android.app.NotificationManager manager,
                               String id, String name, String description, int importance) {
        android.app.NotificationChannel channel = new android.app.NotificationChannel(id, name, importance);
        channel.setDescription(description);
        manager.createNotificationChannel(channel);
    }

    public boolean isFirstRun() { return isFirstRun; }
}