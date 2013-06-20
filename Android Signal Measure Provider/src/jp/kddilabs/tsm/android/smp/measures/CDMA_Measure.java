package jp.kddilabs.tsm.android.smp.measures;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.NeighboringCellInfo;

public final class CDMA_Measure extends AsmpMeasure implements Parcelable {

	// blaaablaaablaaa
	public static final Parcelable.Creator<CDMA_Measure> CREATOR = new Parcelable.Creator<CDMA_Measure>() {

		@Override
		public CDMA_Measure createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new CDMA_Measure(source);
		}

		@Override
		public CDMA_Measure[] newArray(int size) {
			// TODO Auto-generated method stub
			return new CDMA_Measure[size];
		}
	};

	// DAMN THE CONTENT!!
	public int cid;
	public int lac;
	public int rnc;
	public int rssi = 85; // 'unknown' value for gsm after asu2dbm conversion
	public int cdma_ecio;
	public int phone_type;
	public List<NeighboringCellInfo> neighbours;

	public CDMA_Measure() {

	}

	// blaaablaaablaaa
	public CDMA_Measure(Parcel source) {
		readFromParcel(source);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	// how to read and write the data
	@Override
	synchronized public void writeToParcel(Parcel writer, int arg1) {
		super.writeToParcel(writer, arg1);
		writer.writeInt(cid);
		writer.writeInt(lac);
		writer.writeInt(rnc);
		writer.writeInt(rssi);
		writer.writeInt(cdma_ecio);
		writer.writeInt(phone_type);
		writer.writeTypedList(neighbours);
	}

	protected void readFromParcel(Parcel reader) {
		super.readFromParcel(reader);
		cid = reader.readInt();
		lac = reader.readInt();
		rnc = reader.readInt();
		rssi = reader.readInt();
		cdma_ecio = reader.readInt();
		phone_type = reader.readInt();
		reader.readTypedList(neighbours, NeighboringCellInfo.CREATOR);
	}
}
