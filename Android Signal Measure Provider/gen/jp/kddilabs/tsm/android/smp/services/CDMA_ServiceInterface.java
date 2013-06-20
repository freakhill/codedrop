/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Documents and Settings\\jgall\\Desktop\\workspace\\Android Signal Measure Provider\\src\\jp\\kddilabs\\tsm\\android\\smp\\services\\CDMA_ServiceInterface.aidl
 */
package jp.kddilabs.tsm.android.smp.services;
public interface CDMA_ServiceInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements jp.kddilabs.tsm.android.smp.services.CDMA_ServiceInterface
{
private static final java.lang.String DESCRIPTOR = "jp.kddilabs.tsm.android.smp.services.CDMA_ServiceInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an jp.kddilabs.tsm.android.smp.services.CDMA_ServiceInterface interface,
 * generating a proxy if needed.
 */
public static jp.kddilabs.tsm.android.smp.services.CDMA_ServiceInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof jp.kddilabs.tsm.android.smp.services.CDMA_ServiceInterface))) {
return ((jp.kddilabs.tsm.android.smp.services.CDMA_ServiceInterface)iin);
}
return new jp.kddilabs.tsm.android.smp.services.CDMA_ServiceInterface.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getLastValue:
{
data.enforceInterface(DESCRIPTOR);
jp.kddilabs.tsm.android.smp.measures.CDMA_Measure _result = this.getLastValue();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements jp.kddilabs.tsm.android.smp.services.CDMA_ServiceInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public jp.kddilabs.tsm.android.smp.measures.CDMA_Measure getLastValue() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
jp.kddilabs.tsm.android.smp.measures.CDMA_Measure _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getLastValue, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = jp.kddilabs.tsm.android.smp.measures.CDMA_Measure.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getLastValue = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public jp.kddilabs.tsm.android.smp.measures.CDMA_Measure getLastValue() throws android.os.RemoteException;
}
