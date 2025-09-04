package org.schabi.newpipe.util;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class SavedState implements Parcelable {
    private final String prefixFileSaved;
    private final String pathFileSaved;

    public SavedState(@NonNull String prefixFileSaved, @Nullable String pathFileSaved) {
        this.prefixFileSaved = prefixFileSaved;
        this.pathFileSaved = pathFileSaved != null ? pathFileSaved : "";
    }

    protected SavedState(Parcel in) {
        prefixFileSaved = in.readString();
        pathFileSaved = in.readString();
    }

    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
        @Override
        public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
        }

        @Override
        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };

    @NonNull
    public String getPrefixFileSaved() {
        return prefixFileSaved;
    }

    @NonNull
    public String getPathFileSaved() {
        return pathFileSaved;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(prefixFileSaved);
        parcel.writeString(pathFileSaved);
    }
}