package com.m3.wr10.demo;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;

import android.bluetooth.BluetoothProfile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import java.util.Random;

import java.lang.reflect.Method;

import androidx.activity.result.ActivityResult;

import androidx.activity.result.ActivityResultCallback;


/**
 * show list of BLE devices
 */
public class DevicesFragment extends ListFragment {

    private ActivityResultLauncher<Intent> resultLauncher;

    private static final int REQUEST_CODE_GET_SERIAL = 3000;


///    private static final String TAG = "SearchDevices";
    private static final String DDVICENAME_DEFAULT = "WR10";

    private Thread th_Scan_OnOff_s2p = null;
    private Thread th_Get_SerialNum = null;

    private LeScanThread_s2p leScanThread_s2p = null;

    private AlertDialog alertDialog_location = null;

    private AlertDialog alertDialog_barcode  = null;
    private enum ScanState { NONE, LE_SCAN, DISCOVERY, DISCOVERY_FINISHED }
    public  ScanState scanState = ScanState.NONE;
///    private static final long LE_SCAN_PERIOD = 15000; /// 10second // similar to bluetoothAdapter.startDiscovery
    private static final long LE_SCAN_PERIOD = 7000; /// 10second // similar to bluetoothAdapter.startDiscovery

    private static final long LE_SCAN_ON_PERIOD  = 3000; /// 10second // similar to bluetoothAdapter.startDiscovery
    private static final long LE_SCAN_OFF_PERIOD = 1000; /// 10second // similar to bluetoothAdapter.startDiscovery

    private final Handler leScanStopHandler = new Handler();

    private Handler handler_ResultWindow = new Handler();

    private final BluetoothAdapter.LeScanCallback   cb_Scan_for_normal;
    private final Runnable                          cb_ScanStop_for_normal;

    private final BroadcastReceiver discoveryBroadcastReceiver;
    private final IntentFilter discoveryIntentFilter;

    private Menu menu;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private Context context;

///    private final Handler timeoutHandler;
///    private final Runnable timeoutRunnable;

    private final ArrayList<BluetoothUtil.Device> listItems = new ArrayList<>();
    private ArrayAdapter   <BluetoothUtil.Device> listAdapter;

    ActivityResultLauncher<String[]>    requestBluetoothPermissionLauncherForStartScan;
    ActivityResultLauncher<String>       requestLocationPermissionLauncherForStartScan;
    int selectedItemPosition = -1;

    String gstrScanToPairDeviceName = DDVICENAME_DEFAULT;

    boolean bDeviceFound = false;

    boolean bPopupBtnClose = false;

    public DevicesFragment() {

///        timeoutHandler = new Handler(Looper.getMainLooper());
///        timeoutRunnable = () -> Log.d(TAG, "Timeout reached, no device found with name starting with 'ab'");

        cb_Scan_for_normal = (device, rssi, scanRecord) -> {
            /*
            if (device != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    LogWriter.d("*****DevicesFragment=leScanCallback_for_normal==name==" + device.getName() + "=="  + "=getAddress=" + device.getAddress() + "==" );
                    updateScanListIfRequired(device);

                });
            }
            */
        };

        discoveryBroadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC && getActivity() != null) {
                        // getActivity().runOnUiThread(() -> updateScanListIfRequired(device));
                    }
                }

                if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( intent.getAction() ) ) {
                    scanState = ScanState.DISCOVERY_FINISHED; // don't cancel again
                    menu_stopScan_for_normal(); /// stopScan();
                }
            }
        };

        discoveryIntentFilter = new IntentFilter();
        discoveryIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoveryIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


///     leScanStopCallback            = this::stopScan ;           /// stopScan; // w/o explicit Runnable, a new lambda would be created on each postDelayed, which would not be found again by removeCallbacks
        cb_ScanStop_for_normal = this::menu_stopScan_for_normal; /// stopScan; // w/o explicit Runnable, a new lambda would be created on each postDelayed, which would not be found again by removeCallbacks

        requestBluetoothPermissionLauncherForStartScan = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
///                 granted -> BluetoothUtil.onPermissionsResult(this, granted, this::startScan_for_normal) );   ///    startScan) );
                    granted -> BluetoothUtil.onPermissionsResult(this, granted, null  ) );   ///    startScan) );

        requestLocationPermissionLauncherForStartScan = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {                                        /// startScan,
///                        new Handler(Looper.getMainLooper()).postDelayed(this::startScan_for_normal,1); // run after onResume to avoid wrong empty-text
                        new Handler(Looper.getMainLooper()).postDelayed( null,1); // run after onResume to avoid wrong empty-text
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getText(R.string.location_permission_title));
                        builder.setMessage(getText(R.string.location_permission_denied));
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                    }
                });
    }



    private final BluetoothAdapter.LeScanCallback cb_Scan_for_WR
            = (device, rssi, scanRecord) -> {

        LogWriter.d("=s2p=**cb_Scan_for_WR***Name=" + device.getName() + "=scanState=" + scanState
                + "=gstrScanToPairDeviceName=" + gstrScanToPairDeviceName );

        if( device != null
                && device.getName() != null
                && device.getName().contains( gstrScanToPairDeviceName )
                && scanState == ScanState.LE_SCAN ) {
            bDeviceFound = true;

            scanState = ScanState.NONE;
            LogWriter.d("******Found device with name starting with 'ab':****** " + device.getAddress());

            if( leScanThread_s2p != null ) {
                leScanThread_s2p.stopScan();
                leScanThread_s2p.interrupt();
            }

///            handler_ResultWindow.postDelayed( this::showResultWindow, 3000);
            String deviceAddress = device.getAddress();

            connectToDevice(getContext(), device);
///            showResultWindow(deviceAddress);

///  아래는 지우지 말것, 디바이스의 실제 시리얼 번호를 알아내는 것
//            LogWriter.d("******befoe __ resultLauncher================================");

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ftnPermission();

        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listAdapter = new ArrayAdapter<BluetoothUtil.Device>(getActivity(), 0, listItems) {
            @NonNull
            @Override
            public View getView(int position, View view, @NonNull ViewGroup parent) {
                BluetoothUtil.Device device = listItems.get(position);

                if (view == null)
                    view = getActivity().getLayoutInflater().inflate(R.layout.device_list_item, parent, false);

                TextView text1 = view.findViewById(R.id.text1);
                TextView text2 = view.findViewById(R.id.text2);

                String deviceName = device.getName();
                if (deviceName == null || deviceName.isEmpty())
                    deviceName = "<unnamed>";

                text1.setText(deviceName);
                text2.setText(device.getDevice().getAddress());

                // Divider 추가
                View divider = new View(getContext());
                divider.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1)); // divider 높이 설정

///                divider.setBackgroundColor(getResources().getColor(R.color.your_divider_color)); // divider 색상 리소스 설정
                divider.setBackgroundColor( getResources().getColor(R.color.colorPrimary) ); // divider 색상 리소스 설정

                // 아이템의 마지막 위치에는 divider를 추가하지 않음
/*                if (position != getCount() - 1) {
                    ((ViewGroup) view).addView(divider);
                }*/
                ((ViewGroup) view).addView(divider);

                return view;
            }
        };

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>()
                {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                String message = data.getStringExtra("result_key");

                                String lastSeven = message.substring(message.length() - 7);

                                gstrScanToPairDeviceName = "WR10 " + lastSeven;

                                LogWriter.d("======result_key==" + message + "=7=" + lastSeven +
                                        "==gstrScanToPairDeviceName==" + gstrScanToPairDeviceName );

                                if( message.contains("S/N:")) {
                                    ftnScan_WR(message);
                    }   }   }   }
                });
    }

    public void ftnScan_WR( String strName) {
        scanState = ScanState.LE_SCAN;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {

/// deprecated 된 것을 사용하지 말라.
///            bluetoothAdapter.startLeScan(null,  cb_Scan_for_WR);

            leScanThread_s2p = new LeScanThread_s2p( bluetoothAdapter, cb_Scan_for_WR );
            leScanThread_s2p.start();

            try {
                Thread.sleep( (long) (LE_SCAN_ON_PERIOD/3.0) );
                Thread.sleep( (long) (LE_SCAN_ON_PERIOD/3.0) );
                Thread.sleep( (long) (LE_SCAN_ON_PERIOD/3.0) );
            } catch (InterruptedException e) { e.printStackTrace();}

            LogWriter.d("==before==leScanThread_s2p.stopScan()==" );

            if( leScanThread_s2p != null ) {
                leScanThread_s2p.stopScan();
                leScanThread_s2p.interrupt();
            }


        } else {
            LogWriter.e( "Bluetooth is not enabled");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(null);

        View header = getActivity().getLayoutInflater().inflate(R.layout.device_list_header, null, false);
        getListView().addHeaderView(header, null, false);
        setEmptyText("initializing...");
        ((TextView) getListView().getEmptyView()).setTextSize(18);

        setListAdapter(listAdapter);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_devices, menu);
        this.menu = menu;

        if (bluetoothAdapter == null) {
///            menu.findItem(R.id.ble_settings).setEnabled(false);
            menu.findItem(R.id.ble_scan).setEnabled(false);
            menu.findItem(R.id.ble_s2p).setEnabled(false);

        } else if (!bluetoothAdapter.isEnabled()) {
            menu.findItem(R.id.ble_scan).setEnabled(false);
            menu.findItem(R.id.ble_s2p).setEnabled(false);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(discoveryBroadcastReceiver, discoveryIntentFilter);

        if (bluetoothAdapter == null) {
            setEmptyText("<bluetooth LE not supported>");

        } else if (!bluetoothAdapter.isEnabled()) {
            setEmptyText("<bluetooth is disabled>");
            if (menu != null) {
                listItems.clear();
                listAdapter.notifyDataSetChanged();
                menu.findItem(R.id.ble_s2p).setEnabled(false);
                menu.findItem(R.id.ble_scan).setEnabled(false);
            }
        } else {
            setEmptyText("<use SCAN to refresh devices>");
            if (menu != null) {
                menu.findItem(R.id.ble_s2p).setEnabled(true);
                menu.findItem(R.id.ble_scan).setEnabled(true);
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        menu_stopScan_for_normal(); /// stopScan();
        getActivity().unregisterReceiver(discoveryBroadcastReceiver);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        menu = null;
    }





    private final BluetoothAdapter.LeScanCallback cb_Scan_for_BarcodeName
            = (device, rssi, scanRecord) -> {

        LogWriter.d("=s2p=**cb_Scan_for_BarcodeName***Name=" + device.getName() + "=scanState=" + scanState
                + "=gstrScanToPairDeviceName=" + gstrScanToPairDeviceName ); /// + "==selectedFileName=" + selectedFileName); /// + "=alias=" + device.getAlias()

        if( device != null
                && device.getName() != null
                && device.getName().startsWith( gstrScanToPairDeviceName )
///                && device.getName().startsWith("M310")
                && scanState == ScanState.LE_SCAN ) {
            bDeviceFound = true;

            scanState = ScanState.NONE;
            LogWriter.d("******Found device with name starting with 'ab':****** " + device.getAddress());
            stopLeScan_s2p();


            if ( alertDialog_barcode != null && alertDialog_barcode.isShowing()) {
                alertDialog_barcode.dismiss();
            }


///            ftnPopup_Wait();


///            handler_ResultWindow.postDelayed( this::showResultWindow, 3000);
            String deviceAddress = device.getAddress();

///            connectToDevice(getContext(), device);
///            showResultWindow(deviceAddress);

///  아래는 지우지 말것, 디바이스의 실제 시리얼 번호를 알아내는 것
//            LogWriter.d("******befoe __ resultLauncher================================");

//
            // A activity에서 GetSerial을 호출하는 부분
            Intent intent = new Intent(getActivity(), GetSerial.class);
            intent.putExtra("device_address", deviceAddress); // deviceAddress는 Bluetooth 장치의 주소입니다.

            resultLauncher.launch(intent);
        }
    };



    /////////////////////////////////////////////////////////////////////////
public void connectToDevice(Context context, BluetoothDevice device) {
    this.context = context;
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    LogWriter.d("==11111==connect_To_Device===" );

    ///===이곳이 다르다. ===
    registerBondStateReceiver(); // Register the bond state receiver

    LogWriter.d("==2222==connect_To_Device===" );

    // Connect to the GATT server on the device
    bluetoothGatt = device.connectGatt( context, false, cbGatt);

    LogWriter.d("==3333==connect_To_Device===" );
}

    private final BluetoothGattCallback cbGatt = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // When connected, discover services
                LogWriter.d("==4444==STATE_CONNECTED == before gatt.discoverServices===" );
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Handle disconnection
                // Unregister the bond state receiver when disconnected

                LogWriter.d("==555==STATE_DISCONNECTED ===" );
                unregisterBondStateReceiver();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Bond with the device
                boolean result = gatt.getDevice().createBond();

                LogWriter.d("==666==after createBond===result=" + result + "==" );
                if (!result) {
                    // Handle bonding failure
                    // Unregister the bond state receiver if bonding fails
                    unregisterBondStateReceiver();
                }
            }
        }
    };

    // BroadcastReceiver to handle bond state changes
    private final BroadcastReceiver bondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                LogWriter.d("==777==after createBond ), bondState=" + bondState );
                LogWriter.d("==888==device" + device + "==bluetoothGatt.getDevice()==" + bluetoothGatt.getDevice() );

                if (device.equals(bluetoothGatt.getDevice())) {
                    LogWriter.d("==99==9999===================bondState==" + bondState );

                    if (bondState == BluetoothDevice.BOND_BONDED) {

                        LogWriter.d("==101010==after BOND_BONDED ===" );
                        LogWriter.d("******Bonding successfu******* " );
                        // Bonding successful
                        // Unregister the bond state receiver after successful bonding

//                        if( leScanThread_s2p != null ) leScanThread_s2p.stopScan();
//                        if( th_Scan_OnOff_s2p != null ) th_Scan_OnOff_s2p.interrupt(); // 스레드를 중지

                        unregisterBondStateReceiver();
                        showResultWindow( device.getAddress() );   ///  본드가 된 다음에 호출하게 끔 되어 있군.

                    } else if (bondState == BluetoothDevice.BOND_NONE) {
                        LogWriter.d("==999==after BOND_NONE ===" );
                        // Bonding failed or removed
                        // Unregister the bond state receiver if bonding fails
                        unregisterBondStateReceiver();
                    }
                }
            }
        }
    };



    // BroadcastReceiver to handle bond state changes
    private final BroadcastReceiver bondStateReceiver_OLD = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                LogWriter.d("==777==after createBond ), bondState=" + bondState );
                LogWriter.d("==888==device" + device + "==bluetoothGatt.getDevice()==" + bluetoothGatt.getDevice() );

                if (device.equals(bluetoothGatt.getDevice())) {
                    LogWriter.d("==99==9999===================bondState==" + bondState );

                    if (bondState == BluetoothDevice.BOND_BONDED) {

                        LogWriter.d("==101010==after BOND_BONDED ===" );
                        LogWriter.d("******Bonding successfu******* " );
                        // Bonding successful
                        // Unregister the bond state receiver after successful bonding

                        if( leScanThread_s2p != null ) leScanThread_s2p.stopScan();
                        if( th_Scan_OnOff_s2p != null ) th_Scan_OnOff_s2p.interrupt(); // 스레드를 중지

                        unregisterBondStateReceiver();
                        showResultWindow( device.getAddress() );   ///  본드가 된 다음에 호출하게 끔 되어 있군.

                    } else if (bondState == BluetoothDevice.BOND_NONE) {
                        LogWriter.d("==999==after BOND_NONE ===" );
                        // Bonding failed or removed
                        // Unregister the bond state receiver if bonding fails
                        unregisterBondStateReceiver();
                    }
                }
            }
        }
    };



    // Method to register the bond state receiver
    private void registerBondStateReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver( bondStateReceiver, filter);
    }

    // Method to unregister the bond state receiver
    private void unregisterBondStateReceiver() {
        context.unregisterReceiver(bondStateReceiver);
    }


    private void ftnInit_for_S2P_thread() {
        bDeviceFound = false;
        bPopupBtnClose = false;
        scanState = ScanState.NONE;
    }

    private boolean ftnCheck_ExitCondition() {
        boolean bReturnValue = false;

        if( bDeviceFound ) bReturnValue = true;
        if( bPopupBtnClose ) bReturnValue = true;

        return bReturnValue;
    }

    private void ftnJob_after_S2P_thread() {
        leScanThread_s2p.stopScan();
        leScanThread_s2p.interrupt();

///        alertDialog_barcode.dismiss();
        if ( alertDialog_barcode != null && alertDialog_barcode.isShowing()) {
            alertDialog_barcode.dismiss();
        }
    }


    /// if (leScanThread_s2p != null && leScanThread_s2p.isRunning) return;

    private void ftnScan_for_BarcodeName(String selectedFileName )    {
        gstrScanToPairDeviceName = selectedFileName; ///   name by barcode image
        LogWriter.d("==enter==startLeScan_s2p==" );
        int nFrom = 0;         int nTo = 3;

        th_Scan_OnOff_s2p = new Thread(() -> {
            for (int i = nFrom; (i < nTo) && (bDeviceFound == false ) ; i++) {
                if( ftnCheck_ExitCondition() ) break;   /// 발견이 되었으면 나간다.

                leScanThread_s2p = new LeScanThread_s2p( bluetoothAdapter, cb_Scan_for_BarcodeName);
                leScanThread_s2p.start();
                LogWriter.d( "==after==leScanThread_s2p.start()==" );

                try {
                    Thread.sleep( (long) (LE_SCAN_ON_PERIOD/3.0) ); if( ftnCheck_ExitCondition() ) break;
                    Thread.sleep( (long) (LE_SCAN_ON_PERIOD/3.0) ); if( ftnCheck_ExitCondition() ) break;
                    Thread.sleep( (long) (LE_SCAN_ON_PERIOD/3.0) ); if( ftnCheck_ExitCondition() ) break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if( ftnCheck_ExitCondition() ) break;

                LogWriter.d("==before==leScanThread_s2p.stopScan()==" );
                leScanThread_s2p.stopScan();
                leScanThread_s2p.interrupt();
                LogWriter.d("==after==leScanThread_s2p.stopScan()==" );

                if( i ==(nTo - 1) ) break;;

                try {
                    Thread.sleep( (long) (LE_SCAN_OFF_PERIOD/3.0) ); if( ftnCheck_ExitCondition() ) break;
                    Thread.sleep( (long) (LE_SCAN_OFF_PERIOD/3.0) ); if( ftnCheck_ExitCondition() ) break;
                    Thread.sleep( (long) (LE_SCAN_OFF_PERIOD/3.0) ); if( ftnCheck_ExitCondition() ) break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ftnJob_after_S2P_thread();
        });

        th_Scan_OnOff_s2p.start();
    }

    private void stopLeScan_s2p() {
        if(  ( th_Scan_OnOff_s2p != null )  ||  (bDeviceFound == true)  ) {
            scanState = ScanState.NONE;

            if( th_Scan_OnOff_s2p != null ) {
                th_Scan_OnOff_s2p.interrupt(); // 스레드를 중지
                th_Scan_OnOff_s2p = null; // 스레드 참조를 null로 설정
            }

            /// if( alertDialog_barcode != null ) {
            if( alertDialog_barcode != null && alertDialog_barcode.isShowing() ) {
                scanState = ScanState.NONE;
                alertDialog_barcode.dismiss();
            }

        }
    }

    public void ftnPopup_Barcode( String strTitle,  String barcodePicName ) {

        int resourceId = getResources().getIdentifier( "@drawable/" + barcodePicName,
                "drawable", getContext().getPackageName() ); ///  getPackageName() );

        LogWriter.d("***ftnPopupBarcode==scanState:" + scanState + "==barcodePicName:"
                + barcodePicName + "==resourceId:" + resourceId );

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
///                .setTitle("Title") // AlertDialog의 제목 설정
                .setMessage( strTitle ) // AlertDialog의 메시지 설정
                .setPositiveButton("Close", (dialog, which) -> { // 사용자가 버튼을 누르면 AlertDialog가 닫힘
                    bPopupBtnClose = true;
                    stopLeScan_s2p();
                    scanState = ScanState.NONE;
                    dialog.dismiss();
                });

        bPopupBtnClose = false;

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.popup_fig, null);

        ImageView imageView = dialogLayout.findViewById(R.id.image_view);
        imageView.setImageResource(resourceId);

        builder.setView(dialogLayout);


        alertDialog_barcode = builder.create();
        alertDialog_barcode.setCancelable(false);
        alertDialog_barcode.setCanceledOnTouchOutside(false);
        alertDialog_barcode.show();
    }


    public void ftnDeleteBond() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            if (bt.getName().contains("WR")) {
                try {
                    Method m = bt.getClass().getMethod("removeBond", (Class[]) null);
                    m.invoke(bt, (Object[]) null);
                } catch (Exception e) {
                    LogWriter.e( e.getMessage() );
                }
            }
        }
    }

    private void menu_Popup_Barcode_and_Scan( int nSel ) {
        String[] fileNames_hidsppnone = {
                "m3_hid", "m3_spp", "m3_none"
        };  /// 10, 11, 12

        if( nSel == 10) ftnPopup_Barcode( "Only HiD",   fileNames_hidsppnone[ 0 ] );
        if( nSel == 11) ftnPopup_Barcode( "Only SPP",   fileNames_hidsppnone[ 1 ] );
        if( nSel == 12) ftnPopup_Barcode( "None ",      fileNames_hidsppnone[ 2 ] );
        if( 9 < nSel) return;

        if( !bluetoothAdapter.isEnabled() ) return;

        String[] fileNames = {     //// 확장자 gif 를 붙이면 안 되더라
                "m3_0123", "m3_1234", "m3_2345", "m3_3456", "m3_4567",
                "m3_5678", "m3_6789", "m3_7890", "m3_8901", "m3_9012"
        };    /// 0, 1, 2, ..., 9


        String strBarcodeName = null;

        ftnInit_for_S2P_thread();
//      if (scanState != ScanState.NONE) return;

        ftnDeleteBond();   /// WR 만 지운다.

        Random random = new Random();
        int index = random.nextInt( fileNames.length );
        strBarcodeName = fileNames[index];
        ftnPopup_Barcode( "Scan to Pair", strBarcodeName );

        bDeviceFound = false;
        LogWriter.d("==searchForDeviceWithNameAb==scanState=" + scanState );

        if( scanState == ScanState.NONE ) {
            scanState = ScanState.LE_SCAN;
            LogWriter.d("==after-scanState = ScanState.LE_SCAN==scanState=" + scanState );
            ftnScan_for_BarcodeName( strBarcodeName  );
        }
    }

    private void menu_Popup_Barcode_and_Scan_OLD( int nSel ) {

        if (!bluetoothAdapter.isEnabled()) return;

        String[] fileNames = {     //// 확장자 gif 를 붙이면 안 되더라
                "m3_0123", "m3_1234", "m3_2345", "m3_3456", "m3_4567",
                "m3_5678", "m3_6789", "m3_7890", "m3_8901", "m3_9012"
        };    /// 0, 1, 2, ..., 9

        String[] fileNames_hidsppnone = {
                "m3_hid", "m3_spp", "m3_none"
        };  /// 10, 11, 12


        String strBarcodeName = null;

        if ( nSel < 10) {

            ftnInit_for_S2P_thread();
//      if (scanState != ScanState.NONE) return;

            ftnDeleteBond();   /// WR 만 지운다.

            Random random = new Random();
            int index = random.nextInt( fileNames.length );
            strBarcodeName = fileNames[index];
            ftnPopup_Barcode( "Scan to Pair", strBarcodeName );

            bDeviceFound = false;
            LogWriter.d("==searchForDeviceWithNameAb==scanState=" + scanState );

            if( scanState == ScanState.NONE )        {
                scanState = ScanState.LE_SCAN;
                LogWriter.d("==after-scanState = ScanState.LE_SCAN==scanState=" + scanState );
                ftnScan_for_BarcodeName( strBarcodeName  );
            }
        }
        else {
            if( nSel == 10) ftnPopup_Barcode( "Only HiD", fileNames_hidsppnone[ 0 ] );
            if( nSel == 11) ftnPopup_Barcode( "Only SPP", fileNames_hidsppnone[ 1 ] );
            if( nSel == 12) ftnPopup_Barcode( "None ", fileNames_hidsppnone[ 2 ] );
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.ble_s2p) { menu_Popup_Barcode_and_Scan(9 );   return true; } else
        if (id == R.id.ble_scan  ) { menu_startScan_for_normal();     return true; } else
///        if (id == R.id.stop      ) { menu_stopScan_for_normal();      return true; } else // stopScan();

        if (id == R.id.menu_hid  ) { menu_Popup_Barcode_and_Scan(10 );   return true; } else
        if (id == R.id.menu_spp  ) { menu_Popup_Barcode_and_Scan(11 );   return true; } else
        if (id == R.id.menu_none ) { menu_Popup_Barcode_and_Scan(12 );   return true; }



        /*else if (id == R.id.ble_settings) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
            return true;+
        } */
        else {
            return super.onOptionsItemSelected(item);
        }
    }


    public void ftnPermission() {
        ScanState nextScanState = ScanState.LE_SCAN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)     /// 31
        {
            if (!BluetoothUtil.hasPermissions(this, requestBluetoothPermissionLauncherForStartScan))
                return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)      /// 23
        {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                scanState = ScanState.NONE;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.location_permission_title);
                builder.setMessage(R.string.location_permission_grant);
                builder.setPositiveButton(android.R.string.ok,
                        (dialog, which) -> requestLocationPermissionLauncherForStartScan.launch(Manifest.permission.ACCESS_FINE_LOCATION));
                builder.show();
                return;
            }
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            boolean locationEnabled = false;
            try {
                locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ignored) {
            }

            try {
                locationEnabled |= locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ignored) {
            }

            if (!locationEnabled)
                scanState = ScanState.DISCOVERY;

            // Starting with Android 6.0 a bluetooth scan requires ACCESS_COARSE_LOCATION permission, but that's not all!
            // LESCAN also needs enabled 'location services', whereas DISCOVERY works without.
            // Most users think of GPS as 'location service', but it includes more, as we see here.
            // Instead of asking the user to enable something they consider unrelated,
            // we fall back to the older API that scans for bluetooth classic _and_ LE
            // sometimes the older API returns less results or slower
        }

///        scanState = nextScanState;
        scanState = ScanState.NONE;
    }


    private void menu_startScan_for_normal() /// stopScan()
    {
        LogWriter.d("***Scan__normal==scanState=" + scanState + "==");

        if (scanState != ScanState.NONE) return;

        listItems.clear();
        listAdapter.notifyDataSetChanged();
        setEmptyText("<scanning...>");

        menu.findItem(R.id.ble_s2p).setVisible(false);
        menu.findItem(R.id.ble_scan).setVisible(false);
        menu.findItem(R.id.ble_stop).setVisible(true);

        getBondedDevices_for_normal();

        scanState = ScanState.LE_SCAN;

        if (scanState == ScanState.LE_SCAN) {
            leScanStopHandler.postDelayed(cb_ScanStop_for_normal, LE_SCAN_PERIOD);  /// 7second

            new Thread(() -> bluetoothAdapter.startLeScan(null, cb_Scan_for_normal), "startLeScan")
                    .start(); // start async to prevent blocking UI, because startLeScan sometimes take some seconds
        } else {
            bluetoothAdapter.startDiscovery();
        }
    }

    private void getBondedDevices_for_normal() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            for (BluetoothDevice aDevice : pairedDevices) {

                LogWriter.d("==normal==tName=" + aDevice.getName() +
                        "=gstrScanToPairDeviceName=" + gstrScanToPairDeviceName  );

///                if (aDevice.getName() != null && aDevice.getName().contains("WR10"))
///                if (aDevice.getName() != null && aDevice.getName().contains("M310"))
///                if (aDevice.getName() != null && aDevice.getName().contains("m3_6789"))
                 if ( aDevice.getName() != null &&
                         ( aDevice.getName().contains( DDVICENAME_DEFAULT ) ||
                           aDevice.getName().contains( gstrScanToPairDeviceName ) )
                    )
                 {
                    // Only add devices with the name containing "WR10"
                    getActivity().runOnUiThread(() -> updateScanListIfRequired(aDevice));
                 }
            }
        }
    }
    @SuppressLint("MissingPermission")
    private void updateScanListIfRequired(BluetoothDevice device) {
        LogWriter.d("*****updateScanListIfRequired******scanState=" + scanState );

///        if (scanState == ScanState.NONE)       return;

        BluetoothUtil.Device bluetoothDevice = new BluetoothUtil.Device(device);
        String deviceName = bluetoothDevice.getName();
        if( deviceName != null && ( deviceName.contains("WR10") || deviceName.contains( gstrScanToPairDeviceName ) )  )
        {
            int insertionPoint = Collections.binarySearch(listItems, bluetoothDevice);
            if (insertionPoint < 0) {
                listItems.add(-insertionPoint - 1, bluetoothDevice);
                listAdapter.notifyDataSetChanged();
            }
        }
    }





    @SuppressLint("MissingPermission")
    private void menu_stopScan_for_normal() {
        if (scanState == ScanState.NONE)           return;

        setEmptyText("<no bluetooth devices found>");

        if (menu != null) {
            menu.findItem(R.id.ble_s2p).setVisible(true);
            menu.findItem(R.id.ble_scan).setVisible(true);

            menu.findItem(R.id.ble_stop).setVisible(false);
        }

        switch (scanState) {
            case LE_SCAN:
///             leScanStopHandler.removeCallbacks(leScanStopCallback);
                leScanStopHandler.removeCallbacks(cb_ScanStop_for_normal);

///             bluetoothAdapter.stopLeScan(leScanCallback);
                bluetoothAdapter.stopLeScan(cb_Scan_for_normal);

                break;

            case DISCOVERY:
                bluetoothAdapter.cancelDiscovery();
                break;
            default:
                // already canceled
        }
        scanState = ScanState.NONE;
    }


    private void showResultWindow( String strAdd ) {

///        if (getActivity() != null && !getActivity().isFinishing())
        if ( getActivity() != null )
        {

            Bundle args = new Bundle();
            args.putString("device", strAdd);   /// device.getDevice().getAddress());

            LogWriter.d("***in the showResultRunnable==strAdd=" + strAdd + "==");

            Fragment fragment = new ResultWindowFragment();
            fragment.setArguments(args);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            fragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment, fragment, "result_window")
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        menu_stopScan_for_normal(); /// stopScan();
        BluetoothUtil.Device device = listItems.get(position-1);
        String strAdd = device.getDevice().getAddress();
        showResultWindow( strAdd );
    }
}