package jp.kddilabs.tsm.android.smp.measures;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class Location_Measure extends AsmpMeasure implements Parcelable {

	// blaaablaaablaaa
	public static final Parcelable.Creator<Location_Measure> CREATOR = new Parcelable.Creator<Location_Measure>() {

		@Override
		public Location_Measure createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new Location_Measure(source);
		}

		@Override
		public Location_Measure[] newArray(int size) {
			// TODO Auto-generated method stub
			return new Location_Measure[size];
		}
	};

	public Location location;

	public Location_Measure() {

	}

	public Location_Measure(Parcel source) {
		super.readFromParcel(source);
		location = (Location) source.readParcelable(null);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeParcelable(location, 0);
	}

}
