package com.m3.wr10.demo;

import android.bluetooth.BluetoothAdapter;

public class LeScanThread_s2p extends  Thread {

    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothAdapter.LeScanCallback leScanCallback;
    public volatile boolean isRunning = false;

//    public long nTime = 10000;
//    public AlertDialog alertDialog;

    public LeScanThread_s2p(
            BluetoothAdapter adapter,
            BluetoothAdapter.LeScanCallback callback )   /// long time,  AlertDialog alertDialog
    {
        bluetoothAdapter = adapter;
        leScanCallback = callback;

//        this.nTime = time;
//        this.alertDialog = alertDialog;
//        this.strFileName =  strFileName;
    }

    @Override
    public void run() {
        isRunning = true;
        bluetoothAdapter.startLeScan(null, leScanCallback);
    }

    public void stopScan() {
        if (isRunning) {
            LogWriter.d("==before==stopLeScan===" );
            bluetoothAdapter.stopLeScan(leScanCallback);
            isRunning = false;
            LogWriter.d("==after==stopLeScan===" );
        }
    }
}
