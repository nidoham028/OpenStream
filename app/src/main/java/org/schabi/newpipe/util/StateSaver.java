package org.schabi.newpipe.util;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.BundleCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/* 
 * Updated StateSaver.java
 * - DEBUG variable is now static
 * - Fully compatible with static methods
 */
public final class StateSaver {
    public static final String KEY_SAVED_STATE = "key_saved_state";
    private static final ConcurrentHashMap<String, Queue<Object>> STATE_OBJECTS_HOLDER =
            new ConcurrentHashMap<>();
    private static final String TAG = "StateSaver";
    private static final String CACHE_DIR_NAME = "state_cache";
    private static String cacheDirPath;
    private static final boolean DEBUG = true; // now static

    private StateSaver() {
        // no instance
    }

    public static void init(final Context context) {
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir != null) {
            cacheDirPath = externalCacheDir.getAbsolutePath();
        }
        if (TextUtils.isEmpty(cacheDirPath)) {
            cacheDirPath = context.getCacheDir().getAbsolutePath();
        }
    }

    @Nullable
    public static SavedState tryToRestore(final Bundle outState, final WriteRead writeRead) {
        if (outState == null || writeRead == null) return null;

        final SavedState savedState = BundleCompat.getParcelable(outState, KEY_SAVED_STATE, SavedState.class);
        if (savedState == null) return null;

        return tryToRestore(savedState, writeRead);
    }

    @Nullable
    private static SavedState tryToRestore(@NonNull final SavedState savedState,
                                           @NonNull final WriteRead writeRead) {
        if (DEBUG) Log.d(TAG, "tryToRestore() called with savedState: " + savedState);

        try {
            Queue<Object> savedObjects = STATE_OBJECTS_HOLDER.remove(savedState.getPrefixFileSaved());
            if (savedObjects != null) {
                writeRead.readFrom(savedObjects);
                return savedState;
            }

            final File file = new File(savedState.getPathFileSaved());
            if (!file.exists()) return null;

            try (FileInputStream fis = new FileInputStream(file);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                savedObjects = (Queue<Object>) ois.readObject();
            }

            if (savedObjects != null) writeRead.readFrom(savedObjects);

            return savedState;
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore state", e);
        }
        return null;
    }

    @Nullable
    public static SavedState tryToSave(final boolean isChangingConfig,
                                       @Nullable final SavedState savedState,
                                       final Bundle outState,
                                       final WriteRead writeRead) {
        @NonNull final String currentPrefix = (savedState == null || TextUtils.isEmpty(savedState.getPrefixFileSaved()))
                ? System.nanoTime() - writeRead.hashCode() + ""
                : savedState.getPrefixFileSaved();

        final SavedState newSavedState = tryToSave(isChangingConfig, currentPrefix,
                writeRead.generateSuffix(), writeRead);

        if (newSavedState != null && outState != null) {
            outState.putParcelable(KEY_SAVED_STATE, newSavedState);
            return newSavedState;
        }
        return null;
    }

    @Nullable
    private static SavedState tryToSave(final boolean isChangingConfig,
                                        final String prefixFileName,
                                        final String suffixFileName,
                                        final WriteRead writeRead) {
        if (DEBUG) Log.d(TAG, "tryToSave() called: " + prefixFileName);

        LinkedList<Object> savedObjects = new LinkedList<>();
        writeRead.writeTo(savedObjects);

        if (isChangingConfig) {
            if (!savedObjects.isEmpty()) {
                STATE_OBJECTS_HOLDER.put(prefixFileName, savedObjects);
                return new SavedState(prefixFileName, "");
            }
            return null;
        }

        try {
            File cacheDir = new File(cacheDirPath, CACHE_DIR_NAME);
            if (!cacheDir.exists() && !cacheDir.mkdir()) {
                if (DEBUG) Log.e(TAG, "Failed to create cache directory");
                return null;
            }

            final File file = new File(cacheDir, prefixFileName + (TextUtils.isEmpty(suffixFileName) ? ".cache" : suffixFileName));
            if (file.exists() && file.length() > 0) return new SavedState(prefixFileName, file.getAbsolutePath());

            // Delete old files with same prefix
            File[] files = cacheDir.listFiles((dir, name) -> name.contains(prefixFileName));
            if (files != null) for (File f : files) f.delete();

            try (FileOutputStream fos = new FileOutputStream(file);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(savedObjects);
            }

            return new SavedState(prefixFileName, file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Failed to save state", e);
        }
        return null;
    }

    public static void onDestroy(final SavedState savedState) {
        if (DEBUG) Log.d(TAG, "onDestroy() called");

        if (savedState != null && !savedState.getPathFileSaved().isEmpty()) {
            STATE_OBJECTS_HOLDER.remove(savedState.getPrefixFileSaved());
            try {
                new File(savedState.getPathFileSaved()).delete();
            } catch (Exception ignored) { }
        }
    }

    public static void clearStateFiles() {
        if (DEBUG) Log.d(TAG, "clearStateFiles() called");

        STATE_OBJECTS_HOLDER.clear();
        File cacheDir = new File(cacheDirPath, CACHE_DIR_NAME);
        if (!cacheDir.exists()) return;

        File[] files = cacheDir.listFiles();
        if (files != null) for (File f : files) f.delete();
    }

    public interface WriteRead {
        String generateSuffix();
        void writeTo(Queue<Object> objectsToSave);
        void readFrom(@NonNull Queue<Object> savedObjects) throws Exception;
    }
}