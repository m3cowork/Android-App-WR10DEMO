package com.m3.wr10.demo;

class Constants {

    // values have to be globally unique
    static final String INTENT_ACTION_DISCONNECT    = "com.m3.wr10.demo" +  ".Disconnect"; /// BuildConfig.APPLICATION_ID +
    static final String NOTIFICATION_CHANNEL        = "com.m3.wr10.demo" + ".Channel";
    static final String INTENT_CLASS_MAIN_ACTIVITY  = "com.m3.wr10.demo" + ".MainActivity";

    // values have to be unique within each app
    static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;


    static final int REQUEST_CODE_PICK_IMPORT_FILE = 2001;
    static final int REQUEST_CODE_PICK_EXPORT_FILE = 2000;

    static final String CMD_REQ_INFO  = "12#$0100010043@!";     ///     ftnPrintInfo
    static final String CMD_REQ_RESET = "12#$0100010143@!";     ///     ftnReset()

    static final String CMD_REQ_SET_HID  = "12#$0100010243@!";  ///     ftnSet_HID()
    static final String CMD_REQ_SET_SPP  = "12#$0100010343@!";  ///     ftnSet_SPP()
    static final String CMD_REQ_SET_NONE = "12#$0100010443@!";  ///     ftnSet_NONE()

    static final String CMD_All_CODE_DISABLE  = "12#$0100011043@!";     ///     0100, 0110   /// disable all code thypes
    static final String CMD_All_CODE_ENABLE   = "12#$0100011143@!";     ///     0100, 0111   /// enable all code types.

    static final String CMD_AIM_OFF  = "12#$0100012043@!";              ///   Aim Off
    static final String CMD_AIM_ON   = "12#$0100012143@!";              ///   Aim On

    static final String CMD_ILL_OFF  = "12#$0100013043@!";              ///     Illumination off
    static final String CMD_ILL_ON   = "12#$0100013143@!";              ///     Illumination on

    static final String CMD_START_SESSION  = "12#$0100014043@!";     ///    Start Session
    static final String CMD_STOP_SESSION   = "12#$0100014143@!";     ///    Stop Session

    static final String CMD_FIND_ME        = "12#$0100015043@!";     ///

    static final String CMD_READ_ASYNC      = "12#$0100016043@!";     ///
    static final String CMD_READ_SYNC       = "12#$0100016143@!";     ///
    static final String CMD_READ_AIM        = "12#$0100016243@!";     ///

    static final String CMD_BEEP_OFF       = "12#$0100017043@!";     ///    Beep Off
    static final String CMD_BEEP_ON        = "12#$0100017143@!";     ///    Beep On





    static final String CMD_001_PRE  = "12#$";
    static final String CMD_001_POST = "43@!";


    static final String LOGTAG = "WR10";

///////////////////////
    static final String ACTION_FOREGROUND   = "com.m3.wr10.svc.FOREGROUND";
    static final String CHANNEL_ID          = "MyServiceChannel";

    static final String FROM_CUSTOM_TO_ACT = "com.m3.from.custom.to.act"  ;
    static final String FROM_CUSTOM_TO_SVC  = "com.m3.from.custom.to.svc"   ;

    static final String FROM_ACT_TO_CUSTOM = "com.m3.from.act.to.custom"  ;
    static final String FROM_ACT_TO_SVC = "com.m3.from.act.to.svc"     ;

    static final String FROM_SVC_TO_CUSTOM  = "com.m3.from.svc.to.custom"   ;
    static final String FROM_SVC_TO_ACT = "com.m3.from.svc.to.act"     ;

    /////////////////////

    static final String M3ENDCHAR  = "M3ENDCHAR";

    static final String M3PRE  = "M3PRE";
    static final String M3POST = "M3POST";
    static final String M3END  = "M3END";

    private Constants() {}
}
