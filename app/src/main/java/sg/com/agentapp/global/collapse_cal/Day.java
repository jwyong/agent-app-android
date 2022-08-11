package sg.com.agentapp.global.collapse_cal;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by shrikanthravi on 06/03/18.
 */

public class Day implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public sg.com.agentapp.global.collapse_cal.Day createFromParcel(Parcel in) {
            return new sg.com.agentapp.global.collapse_cal.Day(in);
        }

        public sg.com.agentapp.global.collapse_cal.Day[] newArray(int size) {
            return new sg.com.agentapp.global.collapse_cal.Day[size];
        }
    };
    private int mYear;
    private int mMonth;
    private int mDay;

    public Day(int year, int month, int day) {
        this.mYear = year;
        this.mMonth = month;
        this.mDay = day;
    }

    public Day(Parcel in) {
        int[] data = new int[3];
        in.readIntArray(data);
        this.mYear = data[0];
        this.mMonth = data[1];
        this.mYear = data[2];
    }

    public int getMonth() {
        return mMonth;
    }

    public int getYear() {
        return mYear;
    }

    public int getDay() {
        return mDay;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(new int[]{this.mYear,
                this.mMonth,
                this.mDay});
    }


}
