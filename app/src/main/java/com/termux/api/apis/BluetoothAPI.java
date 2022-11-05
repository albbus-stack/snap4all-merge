package com.termux.api.apis;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.JsonWriter;
import android.util.Log;

import com.termux.MainActivity;
import com.termux.api.TermuxApiReceiver;
import com.termux.api.util.ResultReturner;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BluetoothAPI {

    private static boolean scanning = false;
    private static Set<String> deviceList = new HashSet<String>();
    public static boolean unregistered = true;
    public static BluetoothAdapter mBluetoothAdapter;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private static BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String deviceName = device.getName();
                    // you can get more info from device here
                    // ...

                    if (deviceName != null && !deviceName.equals("null") && !deviceName.trim().equals("")) {
                        deviceList.add(deviceName);
                    }
                }
            }
        }
    };

    public static void bluetoothStartScanning(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        MainActivity.activity.getBaseContext().registerReceiver(mReceiver, filter);
        unregistered = false;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();
    }

    public static void bluetoothStopScanning(){
        if(!unregistered) {
            mBluetoothAdapter.cancelDiscovery();
            MainActivity.activity.getBaseContext().unregisterReceiver(mReceiver);
            unregistered=true;
        }
    }

    public static void onReceiveBluetoothScanInfo(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.ResultJsonWriter() {

            @Override
            public void writeJson(final JsonWriter out) throws Exception {
                scanning = true;
                deviceList.clear();
                bluetoothStartScanning();

                handler.postDelayed(() -> {
                    bluetoothStopScanning();
                    scanning = false;
                    try {
                        out.name("devices").beginArray();
                        for (String device : deviceList) {
                            out.value(device);
                        }
                        out.endArray();
                        out.endObject();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 2000);
            }
        });
    }


    public static void onReceiveBluetoothConnect(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent,new ResultReturner.WithStringInput(){

            @Override
            public void writeResult(PrintWriter out) throws Exception {
                try {
                    JsonWriter writer = new JsonWriter(out);
                    writer.setIndent("  ");

                    // if(inputString.equals("")) {
                    //     writer.beginObject().name("message:").value("invalid input").endObject();
                    // } else {
                    //     Implement Bluetooth connection here to the device with the name inputString
                    //     mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(inputString);
                    //     mBluetoothConnectProgressDialog = ProgressDialog.show(this, "Connecting...", mBluetoothDevice.getName() + " : " + mBluetoothDevice.getAddress(), true, false);
                    //     Thread mBlutoothConnectThread = new Thread(this);
                    //     mBlutoothConnectThread.start();

                    //     try {
                    //         BluetoothSocket mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
                    //         mBluetoothAdapter.cancelDiscovery();
                    //         mBluetoothSocket.connect();
                    //         mHandler.sendEmptyMessage(0);
                    //     } catch (IOException eConnectException) {
                    //         Log.d(TAG, "CouldNotConnectToSocket", eConnectException);

                    //         writer.beginObject().name("message:").value("Bluetooth cannot connect to " + inputString).endObject();
                    //         out.println();

                    //         closeSocket(mBluetoothSocket);
                    //         return;
                    //     }

                        writer.beginObject().name("message:").value("Bluetooth connected to " + inputString).endObject();
                        out.println();
                    //}
                }catch(Exception e){
                    Log.d("Except BluetoothConnect", e.getMessage());

                }
            }
        });
    }

    private Handler mHandler = new Handler() 
    {
        @Override
        public void handleMessage(Message msg) 
        {
            mBluetoothConnectProgressDialog.dismiss();
        }
    };
}
