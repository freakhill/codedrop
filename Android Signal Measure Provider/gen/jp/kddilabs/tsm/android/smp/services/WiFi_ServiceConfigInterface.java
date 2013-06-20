/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Documents and Settings\\jgall\\Desktop\\workspace\\Android Signal Measure Provider\\src\\jp\\kddilabs\\tsm\\android\\smp\\services\\WiFi_ServiceConfigInterface.aidl
 */
package jp.kddilabs.tsm.android.smp.services;
public interface WiFi_ServiceConfigInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements jp.kddilabs.tsm.android.smp.services.WiFi_ServiceConfigInterface
{
private static final java.lang.String DESCRIPTOR = "jp.kddilabs.tsm.android.smp.services.WiFi_ServiceConfigInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an jp.kddilabs.tsm.android.smp.services.WiFi_ServiceConfigInterface interface,
 * generating a proxy if needed.
 */
public static jp.kddilabs.tsm.android.smp.services.WiFi_ServiceConfigInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof jp.kddilabs.tsm.android.smp.services.WiFi_ServiceConfigInterface))) {
return ((jp.kddilabs.tsm.android.smp.services.WiFi_ServiceConfigInterface)iin);
}
return new jp.kddilabs.tsm.android.smp.services.WiFi_ServiceConfigInterface.Stub.Proxy(obj);
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
case TRANSACTION_setRefreshPeriod:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setRefreshPeriod(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_startRecording:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.startRecording(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_stopRecording:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.stopRecording(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_isRecording:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.isRecording(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements jp.kddilabs.tsm.android.smp.services.WiFi_ServiceConfigInterface
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
public void setRefreshPeriod(int period) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(period);
mRemote.transact(Stub.TRANSACTION_setRefreshPeriod, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void startRecording(java.lang.String format) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(format);
mRemote.transact(Stub.TRANSACTION_startRecording, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void stopRecording(java.lang.String format) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(format);
mRemote.transact(Stub.TRANSACTION_stopRecording, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public boolean isRecording(java.lang.String format) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(format);
mRemote.transact(Stub.TRANSACTION_isRecording, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_setRefreshPeriod = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_startRecording = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_stopRecording = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_isRecording = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public void setRefreshPeriod(int period) throws android.os.RemoteException;
public void startRecording(java.lang.String format) throws android.os.RemoteException;
public void stopRecording(java.lang.String format) throws android.os.RemoteException;
public boolean isRecording(java.lang.String format) throws android.os.RemoteException;
}
