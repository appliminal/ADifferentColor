package net.appliminal.adifferentcolor;

/**
 *
 */

import android.os.CountDownTimer;
import android.util.Log;

/**
 *
 */
public class Timer{

    private final MainActivity mainActivity;
    private MyTimer myTimer;
    private boolean isStarted;

    private long millisUntilFinished;
    private final long COUNT_DOWN_INTERVAL = 50;
    //countDownInterval;

    /*
    private int currentTimeSecond; //残り時間（秒）
    private int currentTimeMillis; //残り時間の小数点以下２桁。60進（0〜59）で。
    */

    /**
     *
     * @param secondsInFuture 秒数（ミリ秒ではなく）
     * @param mActivity 呼び出し元のActivity
     *                  timerOnFinish()、timerOnTick(timerOnFinish())を実装している必要あり
     */
    public Timer(int secondsInFuture, MainActivity mActivity) {
        mainActivity = mActivity;
        millisUntilFinished = secondsInFuture * 1000;
        myTimer = new MyTimer(millisUntilFinished, COUNT_DOWN_INTERVAL);
        //setCurrentTime(millisInFuture);
        isStarted = false;
    }

    /*
    private void setCurrentTime(long millisUntilFinished){
        currentTimeSecond = (int)millisUntilFinished / 1000;
        currentTimeMillis = (int)((millisUntilFinished % 1000) / 10 * 0.6); //残り秒の小数点以下２桁。60進で。
    }
    */

    public void start() {
        isStarted = true;
        myTimer.start();
    }

    public boolean isStarted(){
        return isStarted;
    }

    public int[] getTimeLeft(){
        int currentTimeSecond = (int)millisUntilFinished / 1000;
        int currentTimeMillis = (int)((millisUntilFinished % 1000) / 10 * 0.6); //残り秒の小数点以下２桁。60進で。
        int[] timeLeft = new int[2];
        timeLeft[0] = currentTimeSecond;
        timeLeft[1] = currentTimeMillis;
        return timeLeft;
    }

    public void addTime(int second, int maxTimeLeft){
        //LogUtil.methodCalled("additional time: " + second);

        //myTimerを一度破棄してから、新しい残り時間をセットしたMyTimerを再生成する。
        //millisUntilFinishedの値は正確ではなく、前回onTick()が呼ばれたときに保存しておいた値なので、
        //最大でCOUNT_DOWN_INTERVALだけの誤差があるはず。
        millisUntilFinished += second * 1000;
        if(maxTimeLeft * 1000 < millisUntilFinished){
            //残り時間が上限値を超えてしまう場合は、上限値とする
            millisUntilFinished = maxTimeLeft * 1000;
            myTimer.cancel();
            myTimer = new MyTimer(millisUntilFinished, COUNT_DOWN_INTERVAL);
            myTimer.start();
        }
        else if(millisUntilFinished <= 0){
            //残り時間がマイナスになる場合、ゼロにしonFinishを呼ぶ
            //@TODO ゼロだった場合は何もせずとも次の瞬間onFinishが呼ばれる？
            //      よく分からないので、念のため明示的に呼ぶことにする。
            myTimer.onFinish();
            //onFinish()内で終了処理済み。タイマーもキャンセルして終了。
            myTimer.cancel();
        }
        else {
            myTimer.cancel();
            myTimer = new MyTimer(millisUntilFinished, COUNT_DOWN_INTERVAL);
            myTimer.start();
        }

        ////スタート済みであれば同様にスタートさせる
        //if(isStarted){
        //    myTimer.start();
        //}
    }

    /**
     * Timerクラスで使用するインナークラス
     * カウントダウンする基本的な機能だけをもつ
     * （CountDownTimerはabstractなので、実装しないと使えない）
     */
    class MyTimer extends CountDownTimer{

        MyTimer(long millisInFuture, long countDownInterval){
            super(millisInFuture, countDownInterval);
            //mainActivity = mActivity;
            //isTimerStarted = false;
            //currentTimeSecond = (int) millisInFuture / 1000; //@FIXME 冗長。。

            /* 落ちる
            //スタートしてないが、currentTimeSecond等にセットするため一度呼んでおく
            onTick(millisInFuture);
            */
        }
//        /**
//         * CountDownTimer#start() をオーバーライドしたいがfinalなのでできない。。
//         */
//        public void startTimer() {
//            isTimerStarted = true;
//            super.start();
//        }
//
//        public boolean isTimerStarted(){
//            return isTimerStarted;
//        }
//
//        /**
//         *
//         * @return
//         */
//        public int[] getTimeLeft(){
//            int[] timeLeft = new int[2];
//            timeLeft[0] = currentTimeSecond;
//            timeLeft[1] = currentTimeMillis;
//            return timeLeft;
//        }

        @Override
        public void onFinish() {
            // タイマー終了時処理
            millisUntilFinished = 0;
            mainActivity.timerOnFinish();
        }

        @Override
        public void onTick(long millisUntilFinished_) {
            // countDownIntervalで指定した間隔で呼ばれる定期処理
            millisUntilFinished = millisUntilFinished_;
            //setCurrentTime(millisUntilFinished);
            mainActivity.timerOnTick(millisUntilFinished);
        }

    }


}
