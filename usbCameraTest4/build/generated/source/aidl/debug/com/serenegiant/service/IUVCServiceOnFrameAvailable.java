/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/ashish/android_projects/UVCCamera/usbCameraTest4/src/main/aidl/com/serenegiant/service/IUVCServiceOnFrameAvailable.aidl
 */
package com.serenegiant.service;
/*
 * UVCCamera
 * library and sample to access to UVC web camera on non-rooted Android device
 * 
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 * 
 * File name: IUVCServiceOnFrameAvailable.aidl
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 * All files in the folder are under this Apache License, Version 2.0.
 * Files in the jni/libjpeg, jni/libusb and jin/libuvc folder may have a different license, see the respective files.
*/
public interface IUVCServiceOnFrameAvailable extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.serenegiant.service.IUVCServiceOnFrameAvailable
{
private static final java.lang.String DESCRIPTOR = "com.serenegiant.service.IUVCServiceOnFrameAvailable";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.serenegiant.service.IUVCServiceOnFrameAvailable interface,
 * generating a proxy if needed.
 */
public static com.serenegiant.service.IUVCServiceOnFrameAvailable asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.serenegiant.service.IUVCServiceOnFrameAvailable))) {
return ((com.serenegiant.service.IUVCServiceOnFrameAvailable)iin);
}
return new com.serenegiant.service.IUVCServiceOnFrameAvailable.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
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
case TRANSACTION_onFrameAvailable:
{
data.enforceInterface(DESCRIPTOR);
this.onFrameAvailable();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.serenegiant.service.IUVCServiceOnFrameAvailable
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void onFrameAvailable() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onFrameAvailable, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_onFrameAvailable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onFrameAvailable() throws android.os.RemoteException;
}
