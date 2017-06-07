package com.brucetoo.expandrecyclerview;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bruce Too
 * On 09/02/2017.
 * At 15:53
 */

public class TestParcel implements Parcelable {

    public int time;
    public String name;

    public TestParcel(){};

    protected TestParcel(Parcel in) {
        time = in.readInt();
        name = in.readString();
    }

    public static final Creator<TestParcel> CREATOR = new Creator<TestParcel>() {
        @Override
        public TestParcel createFromParcel(Parcel in) {
            return new TestParcel(in);
        }

        @Override
        public TestParcel[] newArray(int size) {
            return new TestParcel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(time);
        dest.writeString(name);
    }
}
