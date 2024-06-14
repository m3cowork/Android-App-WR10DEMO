package com.m3.wr10.demo;

import android.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResultWindowFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResultWindowFragment extends Fragment {

    int nSendingNumber = 0;
    private String deviceAddress;
    int nTempNumber = 11;


///////////

    static String strSerial = null;
    static String strModel  = null;
    static String strVer    = null;
    static String strBat    = null;

    private AlertDialog alertDialog_barcode  = null;

    String[] fileNames_hidsppnone = {
            "m3_hid", "m3_spp", "m3_none"
    };


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    TextView tvIncome;

    RadioGroup radioGroup;
    RadioButton radioButton;

    EditText etCmd;
    EditText etSub;

    EditText etPrefix;
    EditText etPostfix;

    String strPrefix;
    String strPostfix;

///    private Menu menu;




    public ResultWindowFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ResultWindowFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ResultWindowFragment newInstance(String param1, String param2) {
        ResultWindowFragment fragment = new ResultWindowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogWriter.d("==onCreate==" );

        super.onCreate(savedInstanceState);


//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }

        IntentFilter filter = new IntentFilter();
        filter.addAction( Constants.FROM_SVC_TO_CUSTOM);
        getActivity().registerReceiver(receiver_in_Custom, filter );

        ftnInit_codeType();

    }


    Map<String, char[]> gCodeType = new LinkedHashMap<>();

    public void
    ftnInit_codeType() {  /// key string                  /// index, initial value,  cmd bytes( 1byte ~ 3bytes )
        gCodeType.put("code_type_upc_a",                new char[]{ 1, 1,  1           });
        gCodeType.put("code_type_upc_e",                new char[]{ 2, 1,  2           });
        gCodeType.put("code_type_upc_e1",               new char[]{ 3, 0,  0x0c        });
        gCodeType.put("code_type_ean_8",                new char[]{ 4, 1,  4           });
        gCodeType.put("code_type_ean_13",               new char[]{ 5, 1,  3           });
        gCodeType.put("code_type_code_128",             new char[]{ 6, 1,  8           });

        gCodeType.put("code_type_code_39",              new char[]{ 7, 1,  0           });
        gCodeType.put("code_type_code_93",              new char[]{ 8, 1,  9           });
        gCodeType.put("code_type_interleaved_2of5",     new char[]{ 9, 1,  6           });
        gCodeType.put("code_type_discrete_2of5",        new char[]{ 10, 0,  5          });
        gCodeType.put("code_type_chinese_2of5",         new char[]{ 11, 0,  0xf0, 0x98 });

///        gCodeType.put("code_type_codabar",              new char[]{ 12, 0,  7          });
        gCodeType.put("code_type_codabar",              new char[]{ 12, 1,  7          });

        gCodeType.put("code_type_msi",                  new char[]{ 13, 0,  0x0b       });
        gCodeType.put("code_type_code_11",              new char[]{ 14, 0,  0x0a       });
        gCodeType.put("code_type_data_matrix",          new char[]{ 15, 1,  0xf0, 0x24 });
        gCodeType.put("code_type_gs1_databar_14",       new char[]{ 16, 0,  0xf0, 0x52 });
        gCodeType.put("code_type_gs1_databar_limited",  new char[]{ 17, 0,  0xf0, 0x53 });

        gCodeType.put("code_type_gs1_databar_expanded", new char[]{ 18, 1,  0xf0, 0x54 });
        gCodeType.put("code_type_ocr_a",                new char[]{ 19, 0,  0xf1, 0xa8 });
        gCodeType.put("code_type_ocr_b",                new char[]{ 20, 0,  0xf1, 0xa9 });
        gCodeType.put("code_type_micr_e13b",            new char[]{ 21, 0,  0xf1, 0xaa });

        gCodeType.put("code_type_pdf417",               new char[]{ 22, 1,  0x0f       });
        gCodeType.put("code_type_micro_pdf417",         new char[]{ 23, 1,  0xe3       });
        gCodeType.put("code_type_qr_code",              new char[]{ 24, 1,  0xf0, 0x25 });
        gCodeType.put("code_type_micro_qr_code",        new char[]{ 25, 1,  0xf1, 0x3d });
        gCodeType.put("code_type_gs1_128",              new char[]{ 26, 1,  0x0e       });

        gCodeType.put("code_type_dot_code",             new char[]{ 27, 0,  0xf8, 0x07, 0x72 }); ////
        gCodeType.put("code_type_aztec",                new char[]{ 28, 1,  0xf1, 0x3e       });
        gCodeType.put("code_type_han_xin",              new char[]{ 29, 0,  0xf8, 0x04, 0x8f });
        gCodeType.put("code_type_composite_ab",         new char[]{ 30, 0,  0xf0, 0x56       });

        gCodeType.put("code_type_composite_c",          new char[]{ 31, 0,  0xf0, 0x55       });
        gCodeType.put("code_type_japanese_postal",      new char[]{ 32, 1,  0xf0, 0x22       });
        gCodeType.put("code_type_korean_3of5",          new char[]{ 33, 1,  0xf1, 0x45       });
        gCodeType.put("code_type_matrix_2of5",          new char[]{ 34, 0,  0xf1, 0x6a       });

        gCodeType.put("code_type_maxicode",             new char[]{ 35, 0,  0xf0, 0x26       });
        gCodeType.put("code_type_us_planet",            new char[]{ 36, 1,  0x5a             });
        gCodeType.put("code_type_us_postnet",           new char[]{ 37, 1,  0x59             });
        gCodeType.put("code_type_uk_postal",            new char[]{ 38, 1,  0x5b             });
        gCodeType.put("code_type_netherlands_kix",      new char[]{ 39, 1,  0xf0, 0x46       });

        gCodeType.put("code_type_australian_postal",    new char[]{ 40, 1,  0xf0, 0x23       });
        gCodeType.put("code_type_upu_fics_postal",      new char[]{ 41, 0,  0xf1, 0x63       });
    }

    public void handleSettingsChanged(String changedKey, boolean bTemp) {
        LogWriter.d("==handleSettingsChanged==key=" + changedKey + "==bTemp=" + bTemp);

        if (!gCodeType.containsKey(changedKey)) {
            LogWriter.d ( "==No Key==changedKey=" + changedKey);
            return;
        }

        String strPre     = Constants.CMD_001_PRE;   /// "12#$";
        String strCodeCmd = "0200";
        String strMsg1    = "000";
        String strMsg2    = "0";
        String strPost    = Constants.CMD_001_POST;  /// "43@!";

        strMsg1 = String.format("%03x", (int) gCodeType.get( changedKey )[ 0 ]  );
        strMsg2 = ( !bTemp ) ? "0" : "1";

        String strCmd = strPre + strCodeCmd + strMsg1 + strMsg2 + strPost;
        LogWriter.d ("==strCmd=" + strCmd );

        ftnSendToSvc_strCmd( strCmd );

/// ======== 나중에 하기         send(strCmd);
    }


    public void ftnSendToSvc_strCmd(String strCmd ) {
        Intent intent = new Intent( );
        intent.setAction( Constants.FROM_CUSTOM_TO_SVC);
///        intent.putExtra("strCmd", strCmd );
        intent.putExtra("m3Cmd", strCmd );

        getActivity().sendBroadcast( intent );
        /// LocalBroadcastManager.getInstance( getActivity().getApplicationContext() ).sendBroadcast( intent );
        LogWriter.d("==strCmd==" + strCmd );
    }

    public void ftnSendToSvc_nCmd_nSub( int nCmd, int nSub ) {
        Intent intent = new Intent( );
        intent.setAction( Constants.FROM_ACT_TO_SVC);

        intent.putExtra("nCmd", nCmd );
        intent.putExtra("nSub", nSub );

        LocalBroadcastManager.getInstance( getActivity().getApplicationContext() ).sendBroadcast( intent );
        LogWriter.d("==nCmd==" + nCmd  + "==nSub==" + nSub );
    }



    private void ftnCmdSub() {
        try {
            int nCmd = Integer.parseInt( etCmd.getText().toString() );
            int nSub = Integer.parseInt( etSub.getText().toString() );

            ftnSendToSvc_nCmd_nSub( nCmd, nSub );

            LogWriter.d("==nCmd==" + nCmd  + "==nSub==" + nSub );
        } catch (NumberFormatException e) {
            LogWriter.d("==error==");
        }
    }

    private void ftnClear()     { tvIncome.setText( "" ); }
    private void ftnInfo()      { ftnSendToSvc_strCmd( Constants.CMD_REQ_INFO ); }

    private void ftnDisableAll(){ ftnSendToSvc_strCmd( Constants.CMD_All_CODE_DISABLE ); }
    private void ftnEnableAll() { ftnSendToSvc_strCmd( Constants.CMD_All_CODE_ENABLE ); }
    private void ftnHid()       { ftnSendToSvc_strCmd( Constants.CMD_REQ_SET_HID ); }
    private void ftnSpp()       { ftnSendToSvc_strCmd( Constants.CMD_REQ_SET_SPP ); }
    private void ftnNone()      { ftnSendToSvc_strCmd( Constants.CMD_REQ_SET_NONE ); }

    private void ftnAimOff()    { ftnSendToSvc_strCmd( Constants.CMD_AIM_OFF ); }
    private void ftnAimOn()     { ftnSendToSvc_strCmd( Constants.CMD_AIM_ON ); }
    private void ftnLightOff()  { ftnSendToSvc_strCmd( Constants.CMD_ILL_OFF ); }
    private void ftnLightOn()   { ftnSendToSvc_strCmd( Constants.CMD_ILL_ON ); }


    private void ftnTrigOn()    {
        ftnAimOff();
        Handler handler1 = new Handler();
        Runnable rbTrigOn = new Runnable() {
            @Override
            public void run() {
                LogWriter.d("====CMD_START_SESSION==");
                /// ftnSendToWR10_Info();
                ftnSendToSvc_strCmd( Constants.CMD_START_SESSION );
            }
        };

        handler1.postDelayed( rbTrigOn, 400);   /// 1000

        /// ftnSendToSvc_strCmd( Constants.CMD_START_SESSION );
    }


    private void ftnTrigOff()   {
        ftnAimOff();
        Handler handler2 = new Handler();
        Runnable rbTrigOff = new Runnable() {
            @Override
            public void run() {
                LogWriter.d("====CMD_START_SESSION==");
                /// ftnSendToWR10_Info();
                ftnSendToSvc_strCmd( Constants.CMD_STOP_SESSION );
            }
        };
        handler2.postDelayed( rbTrigOff, 400);   /// 1000

///        ftnSendToSvc_strCmd( Constants.CMD_STOP_SESSION );
    }

    private void ftnFindMe()    { ftnSendToSvc_strCmd( Constants.CMD_FIND_ME ); }


    private void ftnBeep_Off()    { ftnSendToSvc_strCmd( Constants.CMD_BEEP_OFF );  LogWriter.d("==CMD_BEEP_OFF==");  }
    private void ftnBeep_On()     { ftnSendToSvc_strCmd( Constants.CMD_BEEP_ON  );  LogWriter.d("==CMD_BEEP_ON==");  }

    private void ftnRead_Async()    { ftnSendToSvc_strCmd( Constants.CMD_READ_ASYNC );  LogWriter.d("==CMD_READ_ASYNC==");  }
    private void ftnRead_Sync()     { ftnSendToSvc_strCmd( Constants.CMD_READ_SYNC );   LogWriter.d("==CMD_READ_SYNC==");   }
    private void ftnRead_Aim()      { ftnSendToSvc_strCmd( Constants.CMD_READ_AIM );    LogWriter.d("==CMD_READ_Aim==");    }


    private void ftnC3() { } // ftnSendToSvc_strCmd( Constants.CMD_ILL_OFF ); }
    private void ftnC4() { } // ftnSendToSvc_strCmd( Constants.CMD_ILL_ON ); }


    private void ftnServiceOn() {
        LogWriter.d("==ftnService On==");

        Intent intent = new Intent( );
        intent.setAction( Constants.FROM_CUSTOM_TO_ACT);
        intent.putExtra("strAdd", deviceAddress );
        getActivity().sendBroadcast( intent );
    }

    private void ftnServiceOff() {
        LogWriter.d("==ftnService Off==");

        Intent intent2 = new Intent( Constants.FROM_CUSTOM_TO_ACT );
        intent2.putExtra("strServiceOff", "Off" );
        getActivity().sendBroadcast(intent2);
    }

    private void ftnSetEndChar( int nChar ) {
        Intent intent = new Intent( Constants.FROM_CUSTOM_TO_SVC );
        intent.putExtra( Constants.M3ENDCHAR,  Constants.M3ENDCHAR + nChar + Constants.M3END );
        getActivity().sendBroadcast( intent );

        LogWriter.d(  "==Constants.M3ENDCHAR + strCh + Constants.M3END==" + Constants.M3ENDCHAR + nChar + Constants.M3END );
    }

    private void ftnSetFix() {
        strPrefix = etPrefix.getText().toString();
        strPostfix = etPostfix.getText().toString();

        etPrefix.clearFocus();
        etPostfix.clearFocus();

        Intent intent = new Intent( Constants.FROM_CUSTOM_TO_SVC);

        String strPrePostEnd = "";

        strPrePostEnd = Constants.M3PRE;
        strPrePostEnd += strPrefix;
        strPrePostEnd += Constants.M3POST;
        strPrePostEnd += strPostfix;
        strPrePostEnd += Constants.M3END;

        intent.putExtra( "strPrePostEnd",  strPrePostEnd  );
        getActivity().sendBroadcast( intent );

        LogWriter.d(  "==strPrePostEnd==" + strPrePostEnd );
    }

    private final RadioGroup.OnCheckedChangeListener  OnRadioBtn = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            radioButton =  radioGroup.findViewById( checkedId );
            String strSum = null;

            if( checkedId == R.id.rb_ec_enter) ftnSetEndChar(0 );
            if( checkedId == R.id.rb_ec_space) ftnSetEndChar(1 );
///            if( checkedId == R.id.ec_tab ) ftnSetEndChar(2 );
            if( checkedId == R.id.rb_ec_none) ftnSetEndChar(3 );

//            switch ( checkedId ) {
//                case R.id.ec_enter     : ftnSetEndChar(0 );   break;
//                case R.id.ec_space     : ftnSetEndChar(1 );   break;
//                case R.id.ec_tab      : ftnSetEndChar(2 );   break;
//                case R.id.ec_none      : ftnSetEndChar(3 );   break;
//            }

        }
    };

    private final View.OnClickListener OnButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
///            if (view.getId() == R.id.btnSetCodeValue)   {   ftnCmdSub();     } else
//            if (view.getId() == R.id.btn_read_enable)   {   } else
//            if (view.getId() == R.id.btn_read_disable)  {   } else
//            if (view.getId() == R.id.btn_read_status)   {   } else

            if (view.getId() == R.id.btn_top_read)  {
                ftnTrigOn();  } else

            if (view.getId() == R.id.btn_top_cancel) {
                /// ftnAimOff();    ftnLightOff();
                ftnTrigOff(); } else

            if( view.getId() == R.id.rb_beep_off )   { ftnBeep_Off();  } else
            if( view.getId() == R.id.rb_beep_on )    { ftnBeep_On();   } else

            if( view.getId() == R.id.btn_read_async )   { ftnRead_Async();  } else
            if( view.getId() == R.id.btn_read_sync )    { ftnRead_Sync();  } else
            if( view.getId() == R.id.btn_read_aim )     { ftnRead_Aim();  } else


            if (view.getId() == R.id.btnClear)          {   ftnClear();      } else
            if (view.getId() == R.id.btnInfo)           {   ftnInfo();       } else
            if (view.getId() == R.id.btnServiceOn)      {   ftnServiceOn();  } else
            if (view.getId() == R.id.btnServiceOff)     {   ftnServiceOff(); } else

            if (view.getId() == R.id.btnDisableAll)     {   ftnDisableAll(); } else
            if (view.getId() == R.id.btnEnableAll )     {   ftnEnableAll();  } else

            if (view.getId() == R.id.btnSetFix )        {   ftnSetFix();    } else

            if (view.getId() == R.id.btnHid  )     { ftnHid();  } else
            if (view.getId() == R.id.btnSpp  )     { ftnSpp();  } else
            if (view.getId() == R.id.btnNone )     { ftnNone();  } else

            if (view.getId() == R.id.btnAimOff)     { ftnAimOff();  } else
            if (view.getId() == R.id.btnAimOn)      { ftnAimOn();  } else
            if (view.getId() == R.id.btnLightOff)   { ftnLightOff();  } else
            if (view.getId() == R.id.btnLightOn)    { ftnLightOn();  } else

            if (view.getId() == R.id.btnTrigOn)     { ftnTrigOn();  } else
            if (view.getId() == R.id.btnTrigOff)    { ftnTrigOff();  } else
            if (view.getId() == R.id.btnFindMe )    { ftnFindMe();  } else
            if (view.getId() == R.id.btnC4 )        { ftnC4();  } else

            if (view.getId() == R.id.btnCode  )    { ftnMenu_CodeType(); }
        }
    };

    public  void ftnMenu_CodeType() {
        LogWriter.d("==ftnMenu_CodeType==bbb" );

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, new PreferenceFragment(this)); // this (ResultWindowFragment context) 전달
        transaction.addToBackStack(null);
        transaction.commit();
    }

//    private void showKeyboard(View view) {
//        if (view.requestFocus()) {
//            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService( Activity.INPUT_METHOD_SERVICE );
//            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scanner_intent, container, false);

        setHasOptionsMenu(true);

        tvIncome = view.findViewById( R.id.tvIncome );
///        etCmd = view.findViewById( R.id.etCmd );
///        etSub = view.findViewById( R.id.etSub );

        etPrefix  = view.findViewById( R.id.etPrefix );
        etPostfix = view.findViewById( R.id.etPostfix );

        radioGroup = view.findViewById(R.id.radio_end_mode);
        radioGroup.setOnCheckedChangeListener( OnRadioBtn );



        (view.findViewById(R.id.btn_top_read    )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btn_top_cancel  )).setOnClickListener(OnButtonClick);

        (view.findViewById(R.id.rb_beep_off     )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.rb_beep_on      )).setOnClickListener(OnButtonClick);

        (view.findViewById(R.id.btn_read_async  )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btn_read_sync   )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btn_read_aim    )).setOnClickListener(OnButtonClick);



        (view.findViewById(R.id.btnClear        )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnInfo         )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnServiceOn    )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnServiceOff   )).setOnClickListener(OnButtonClick);

        (view.findViewById(R.id.btnDisableAll   )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnEnableAll    )).setOnClickListener(OnButtonClick);

        (view.findViewById(R.id.btnSetFix       )).setOnClickListener(OnButtonClick);

        (view.findViewById(R.id.btnHid      )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnSpp      )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnNone     )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnCode     )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnNone     )).setOnClickListener(OnButtonClick);

        (view.findViewById(R.id.btnAimOff   )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnAimOn    )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnLightOff )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnLightOn  )).setOnClickListener(OnButtonClick);

        (view.findViewById(R.id.btnTrigOn   )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnTrigOff  )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnFindMe   )).setOnClickListener(OnButtonClick);
        (view.findViewById(R.id.btnC4       )).setOnClickListener(OnButtonClick);



//        String someText = "Hello, BLE!";
//        tvIncome.setText(someText);


        nTempNumber = 11;

        deviceAddress = getArguments().getString("device");
        LogWriter.d("==ResultWindowFragment==deviceAddress==" + deviceAddress + "==");



        // Inflate the layout for this fragment
///     return inflater.inflate(R.layout.fragment, container, false);
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        LogWriter.d("==onStart==" );
    }

    @Override
    public void onStop() {
        super.onStop();

        LogWriter.d("==onStop==" );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(receiver_in_Custom);
        LogWriter.d("==onDestroy==" );
    }

    private void ftnPrint( String strArg ) {
        String tvStrscnRead = tvIncome.getText().toString();

        int lineCount = tvStrscnRead.split("\n", -1).length - 1;
        if( 5 < lineCount ) tvStrscnRead = "";

        String newText = tvStrscnRead + strArg + "\n" ;
        tvIncome.setText( newText );

        LogWriter.d( "==strCmd==" + strArg );
    }

    private final BroadcastReceiver receiver_in_Custom = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if ( Constants.FROM_SVC_TO_CUSTOM.equals(action)) {
                String strScnRead = null;

                strScnRead = intent.getStringExtra("strScnRead");
                if( strScnRead != null ) {
                    ftnPrint( strScnRead );
                }
            }
        }
    };
}