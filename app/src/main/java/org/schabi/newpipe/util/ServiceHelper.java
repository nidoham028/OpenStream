package org.schabi.newpipe.util;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import com.nidoham.openstream.R;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class ServiceHelper {
    // Only YouTube is supported
    private static final StreamingService DEFAULT_FALLBACK_SERVICE = ServiceList.YouTube;

    private ServiceHelper() { }

    @DrawableRes
    public static int getIcon(final int serviceId) {
        if (serviceId == ServiceList.YouTube.getServiceId()) {
            return R.drawable.ic_smart_display;
        }
        return R.drawable.ic_circle;
    }

    public static String getTranslatedFilterString(final String filter, final Context c) {
        switch (filter) {
            case "all":
                return c.getString(R.string.all);
            case "videos":
            case "sepia_videos":
            case "music_videos":
                return c.getString(R.string.videos_string);
            case "channels":
                return c.getString(R.string.channels);
            case "playlists":
            case "music_playlists":
                return c.getString(R.string.playlists);
            default:
                return filter;
        }
    }

    /**
     * Get a resource string with instructions for importing subscriptions for YouTube only.
     */
    @StringRes
    public static int getImportInstructions(final int serviceId) {
        if (serviceId == ServiceList.YouTube.getServiceId()) {
            return R.string.import_youtube_instructions;
        }
        return -1;
    }

    public static int getSelectedServiceId(final Context context) {
        return Optional.ofNullable(getSelectedService(context))
                .orElse(DEFAULT_FALLBACK_SERVICE)
                .getServiceId();
    }

    @NonNull
    public static StreamingService getSelectedService(final Context context) {
        final String serviceName = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.current_service_key),
                        context.getString(R.string.default_service_value));

        try {
            return NewPipe.getService(serviceName);
        } catch (final ExtractionException e) {
            return DEFAULT_FALLBACK_SERVICE;
        }
    }

    @NonNull
    public static String getNameOfServiceById(final int serviceId) {
        if (serviceId == ServiceList.YouTube.getServiceId()) {
            return ServiceList.YouTube.getServiceInfo().getName();
        }
        return "<unknown>";
    }

    @NonNull
    public static StreamingService getServiceById(final int serviceId) {
        if (serviceId == ServiceList.YouTube.getServiceId()) {
            return ServiceList.YouTube;
        }
        throw new IllegalArgumentException("Only YouTube is supported");
    }

    public static void setSelectedServiceId(final Context context, final int serviceId) {
        String serviceName;
        try {
            serviceName = NewPipe.getService(serviceId).getServiceInfo().getName();
        } catch (final ExtractionException e) {
            serviceName = DEFAULT_FALLBACK_SERVICE.getServiceInfo().getName();
        }

        setSelectedServicePreferences(context, serviceName);
    }

    private static void setSelectedServicePreferences(final Context context,
                                                      final String serviceName) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(context.getString(R.string.current_service_key), serviceName)
                .apply();
    }

    public static long getCacheExpirationMillis(final int serviceId) {
        return TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
    }

    public static void initService(final Context context, final int serviceId) {
        // No extra init needed since only YouTube is supported
    }

    public static void initServices(final Context context) {
        initService(context, ServiceList.YouTube.getServiceId());
    }
}