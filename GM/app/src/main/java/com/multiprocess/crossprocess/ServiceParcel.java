package com.multiprocess.crossprocess;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class ServiceParcel implements Parcelable {

    private final IBinder mBinder;

    private ServiceParcel(Parcel source) {
        mBinder = source.readStrongBinder();
    }

    public ServiceParcel(IBinder binder) {
        this.mBinder = binder;
    }

    public IBinder getBinder() {
        return mBinder;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(mBinder);
    }

    public static final Creator<ServiceParcel> CREATOR = new Creator<ServiceParcel>() {

        @Override
        public ServiceParcel createFromParcel(Parcel source) {
            return new ServiceParcel(source);
        }

        @Override
        public ServiceParcel[] newArray(int size) {
            return new ServiceParcel[size];
        }

    };

}
