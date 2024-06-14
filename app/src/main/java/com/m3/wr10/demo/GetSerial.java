package com.m3.wr10.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;



import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayDeque;
import java.util.ArrayList;





public class GetSerial extends Activity implements ServiceConnection, SerialListener {
    public static final String CMD_001_PRE  = "12#$";
    public static final String CMD_001_POST = "43@!";


///    private myBcRx receiver;
    private static final int REQUEST_CODE_PICK_IMPORT_FILE = 1001;
    private static final int REQUEST_CODE_PICK_EXPORT_FILE = 1000;

    private enum Connected {False, Pending, True}

    private String deviceAddress;
    private SerialService service;
    private Connected connected = Connected.False;
    private boolean initialStart = true;

    /// private ArrayList<BarCodeData> mBarCodes;

    private TextView mTxtDecodeCount;
    private int mDecodeCount = 0;
    private String originalTitle;
    private Menu menu;
    private Context mContext;

    private final String MSG_SAVE_SUCCESS = "The file has been successfully saved.";
    private final String MSG_SAVE_FAIL = "An error occurred while saving the file.";

    private AlertDialog alertDialog_wait = null;



    static char[] g_cmds = new char[200];
///    byte [] g_txCmd_byte = new byte[200];

    byte[][] g_txMsg = new byte[2][100];
    int gFirstIndex  = 0;
    int gSecondIndex = 0;
    int gnBlockIndex = 0;

    private void ftnPopup_Wait() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this )
///                .setTitle("Title") // AlertDialog의 제목 설정
                .setMessage("*** Please Wait ! ***"); // AlertDialog의 메시지 설정

        alertDialog_wait = builder.create();
        alertDialog_wait.show();
    }


    public GetSerial() {
        // 기본 생성자 내용 추가 (필요한 경우)
        LogWriter.d("============================GetSerial===00===strAdd==" );
    }

    GetSerial( String strAdd ) {
        LogWriter.d("============================GetSerial==01==strAdd==" + strAdd );
        deviceAddress = strAdd;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogWriter.d("====onAttach==01====");

        this.bindService(new Intent( this, SerialService.class), this, Context.BIND_AUTO_CREATE);

        ftnPopup_Wait();

        // 인텐트에서 deviceAddress 값을 가져옴
        deviceAddress = getIntent().getStringExtra("device_address");
        LogWriter.d("====onCreate==02====deviceAddress==" + deviceAddress );
    }

    @Override
    public void onStart() {
        super.onStart();
        LogWriter.d("====onStart==04====");

        if (service != null)
            service.attach(this);
        else
            this.startService(new Intent( this, SerialService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogWriter.d("====onResume==05====");

        if (initialStart && service != null) {
            initialStart = false;
            connect();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        LogWriter.d("====onServiceConnected==06====");

        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);

        connect();
    }

    public void connect() {
        LogWriter.d("====connect==07====");

        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket( this.getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private  void ftnSendToWR10_Info()  { send( Constants.CMD_REQ_INFO  );  }
    private  void ftnSendToWR10_Reset() { send( Constants.CMD_REQ_RESET );  }

    @Override
    public void onSerialConnect() {   /// 연결이 성공하면 이리로 온다.
        status("connected");
        connected = Connected.True;
        LogWriter.d("====onSerialConnect==08====");

        Handler handler1 = new Handler();
        Handler handler2 = new Handler();
        Handler handler3 = new Handler();
        Runnable rbGetInfo = new Runnable() {
            @Override
            public void run() {
                LogWriter.d("====before ftnMenu_Info=====");
                ftnSendToWR10_Info();
            }
        };

        Runnable rbSetReset = new Runnable() {
            @Override
            public void run() {
                LogWriter.d("====before ftnMenu_Reset=====");
                ftnSendToWR10_Reset();
            }
        };

        Runnable rbFinish = new Runnable() {
            @Override
            public void run() {
                /// alertDialog_wait.dismiss();

                if ( alertDialog_wait != null && alertDialog_wait.isShowing()) {
                    alertDialog_wait.dismiss();
                }

                finish();
            }
        };

        long delayMillis = 5000; // 5000밀리초 = 5초
// 콜백 등록
        handler1.postDelayed( rbGetInfo, 1000);   /// 1000
        handler2.postDelayed( rbSetReset, 3000);   /// 2000
        handler2.postDelayed( rbSetReset, 4000);   /// 3000
        handler3.postDelayed( rbFinish, 5000);   /// 4000

//        handler1.postDelayed( rbGetInfo, 1000);
//        handler2.postDelayed( rbSetReset, 3000);
//        handler2.postDelayed( rbSetReset, 4000);
//        handler3.postDelayed( rbFinish, 5000);
//
//        handler1.postDelayed( rbGetInfo, 1000);
//        handler2.postDelayed( rbSetReset, 5000);
//        handler2.postDelayed( rbSetReset, 6000);
//        handler3.postDelayed( rbFinish, 7000);



    }

    @Override
    public void onStop() {
        LogWriter.d("====onStop==01====");
        if( service != null )
            service.detach();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        LogWriter.d("====onDestroy==02====connected=" + connected );
        if (connected != Connected.False) {
            disconnect();
        }

        this.stopService(new Intent( this, SerialService.class));

        LogWriter.d("===UN=registerReceiver=onDestroy==");

        super.onDestroy();
    }

    private void disconnect() {
        LogWriter.d("====disconnect==03====");
        connected = Connected.False;
        service.disconnect();

        LogWriter.d("====onDetach==05====");
        try {
            this.unbindService(this);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        LogWriter.d("====onServiceDisconnected==011111111====");  /// 왜 안 보이지???
        service = null;
    }

    private void send(String str) {
        try {
            String msg;
            byte[] data;

            msg = str;
            data = (str).getBytes();

            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            /// receiveText.append(spn);
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    StringBuilder strRxvd = new StringBuilder();
    String strMsg = null;

    void ftnSend_Display() {     }

    private static boolean hasISOControlCharacters(StringBuilder stringBuilder) {
        for (int i = 0; i < stringBuilder.length(); i++) {
            char ch = stringBuilder.charAt(i);
            if (Character.isISOControl(ch)) {
                return true;
            }
        }
        return false;
    }

    private void receive(ArrayDeque<byte[]> datas) {
        for (byte[] data : datas) {
            String msg = new String(data);
            strRxvd.append(msg);
            LogWriter.d("msg=" + msg +   "==receive===strRxvd==" + strRxvd);

            boolean containsISOControl = hasISOControlCharacters( strRxvd );

            if( containsISOControl ) {
                // strRxvd 초기화
                strRxvd = new StringBuilder();
                strRxvd.setLength(0);
                return;
            }

            if (strRxvd.indexOf(  CMD_001_PRE ) != -1 && strRxvd.indexOf( CMD_001_POST ) != -1) {
                int nS = strRxvd.indexOf( CMD_001_PRE  );
                int nE = strRxvd.indexOf( CMD_001_POST );

                if (nS < nE) {
                    strMsg = strRxvd.substring(nS + 4, nE);
                    ftnSend_Display();
                    LogWriter.d( "==RxMsg==" + strMsg);

                    if( strMsg.startsWith("S/N:")) {
                        // 다른 액티비티에서 결과 설정 및 반환
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("result_key", strMsg );
                        setResult(Activity.RESULT_OK, resultIntent);
                        /// finish();   /// finish  를 해야만 deviceFrament  로 데이타가 넘어가나?
    }   }   }  }   }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        public void handleMessage(final Message msg) {
        }
    };

    private void status(String str) {
        Toast.makeText(service, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        Log.e("M3Mobile", "====onSerialConnectError====" + "connection failed: " + e.getMessage() );
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        ArrayDeque<byte[]> dataArray = new ArrayDeque<>();
        dataArray.add(data);
        receive(dataArray);
    }

    public void onSerialRead(ArrayDeque<byte[]> dataArray) {
        receive(dataArray);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        LogWriter.e( "====onSerialIoError====" + "connection lost: " + e.getMessage() );
        disconnect();
    }
}