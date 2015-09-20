package net.appliminal.adifferentcolor;

import android.util.Log;

/**
 * Created by yonai on 15/09/06.
 */
public class LogUtil {

    /*
    public static final String getMethodInfo() {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        String className = ste.getClassName();
        className = className.substring(className.lastIndexOf(".") + 1);
        String methodName = ste.getMethodName();
        //int lineNum = ste.getLineNumber();
        //return className + "." + methodName + ":" + lineNum;
        return "(LogUtil出力)" + className + "." + methodName;
    }
    */

    public static void methodCalled(){
        methodCalled("");
    }

    public static void methodCalled(String msg){
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        String className = ste.getClassName();
        className = className.substring(className.lastIndexOf(".") + 1);
        String methodName = ste.getMethodName();
        Log.i("(メソッド呼び出し)" + className + "." + methodName, msg);
    }


}
