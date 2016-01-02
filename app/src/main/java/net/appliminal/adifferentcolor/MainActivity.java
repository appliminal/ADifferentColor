package net.appliminal.adifferentcolor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Random;


//@FIXME 途中でスマホの向き変えたら落ちる

/**
 * ＜基本＞
 * サイズは基本的にdpで指定。（グラフィックの場合pxも可）
 * フォントサイズも基本はdpで。（spの場合端末のフォントサイズ設定で大小が変わる）
 */

/**
 *
 * @author Appliminal
 * @version 1.2.0
 *
 */
public class MainActivity extends AppCompatActivity {

    private Bundle instanceState;

    private final String PREFERENCE_FILE_NAME = "A_Different_Color";
    private final String PREFERENCE_KEY_BEST_SCORE = "best_score";
    private final int BEST_SCORE_NOT_SET = -1;
    private int bestScore;

    private ColorBlocksFragment colorBlocksFragment = null;
    //private int cbfWidth, cbfHeight; //@TODO intでいいのか？

    private int currentScore = 0;

    private Timer mainTimer;
    private final int INITIAL_TIME_LFET = 10; //残り時間の初期値
    private final int ADDITIONAL_TIME = 5; //正解時に追加する秒数
    private final int MAX_TIME_LEFT = 20; //残り時間の上限
    private final int INCORRECT_PENALTY_TIME = -3; //間違えた時のペナルティ（残り時間から引かれる）

    private InterstitialAd interstitialAd;
    private final int INTERSTITIAL_ADVERTISEMENT_DISPLAY_RATE = 7; //リトライ時、全画面広告を何回に一度表示させるか //TODO 値は要調整

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instanceState = savedInstanceState;

        //ロケール設定。setContentViewの前に行う必要がある。
        setLocale();

        setContentView(R.layout.activity_main);

        //画面中央にブロックを初期状態で配置
        //@TODO このIF文の意味を理解すること
        if (savedInstanceState == null) {
            initializeColorBlocksFragmentView();
        }

        //ベストスコアの読み込み
        bestScore = readBestScore();
        updateBestScoreView();

        //@FIXME どこまでif文の中に入れればいいのか。。

        updateCurrentScoreView();

        //ゲームスタート（ユーザ操作の有効化、タイマー開始）
        gameStart();

        //広告を有効化
        loadBannerAdvertisement();
        initializeInterstitialAdvertisement();

    }

    @Override
    protected void onStart() {
        LogUtil.methodCalled("onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        LogUtil.methodCalled("onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        LogUtil.methodCalled("onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogUtil.methodCalled("onPause");
        super.onPause();

        ////Activityの終了 => MainActivityなのでアプリケーションの終了
        //this.finish();
    }

    @Override
    protected void onStop() {
        LogUtil.methodCalled("onStop");
        super.onStop();

        ////Activityの終了 => MainActivityなのでアプリケーションの終了
        //this.finish();
    }

    @Override
    protected void onDestroy() {
        LogUtil.methodCalled("onDestroy");
        super.onDestroy();
    }

    /**
     * ホームボタンが押された時や、他のアプリが起動した時に呼ばれる
     * 戻るボタンが押された場合には呼ばれない
     */
    @Override
    public void onUserLeaveHint() {
        //Activityの終了 => MainActivityなのでアプリケーションの終了
        this.finish();
    }

    /**
     * ロケール設定
     * TODO Preferenceとかに持って、読み込むようにしたい。
     */
    private void setLocale(){
        return;
        /* サンプル
        //Locale locale = new Locale(Locale.JAPANESE);
        //Locale.setDefault(locale);
        Locale.setDefault(Locale.JAPANESE);
        Configuration config = new Configuration();
        config.locale = Locale.JAPANESE;
        //getResources().updateConfiguration(config, getResources().getDisplayMetrics()); //設定変更を反映

        //getBaseContextは必要？？
        getBaseContext().getResources().updateConfiguration(config, getResources().getDisplayMetrics()); //設定変更を反映

        //getResources().updateConfiguration(config, null);
        //setContentView(R.layout.activity_main);
        */
    }

    /**
     * 画面中央のブロックが並ぶ部分のサイズを決定する
     *
     * @return 表示幅
     */
    //TODO privateにすべき
    public int getColorBlocksFragmentSize() {
        //画面の向きを取得する場合
        //getResources().getConfiguration().orientation

        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        //Log.i("width: " + dm.widthPixels + " / height: " + dm.heightPixels, ""); //480*800とか。
        int size = Math.min(dm.widthPixels, dm.heightPixels);
        return (int) Math.floor(size * 0.85);
        //●DisplayMetricsでは以下の値が取得できます。
        //・density       :ディスプレイの論理的な密度
        //・densityDpi    :インチ当たりのドット数で表した画面の密度
        //・heightPixels  :ピクセルのディスプレイの絶対的な高さ
        //・scaledDensity : ディスプレイに表示されるフォントのスケーリングファクター
        //・widthPixels   : ピクセルのディスプレイの絶対的な幅
        //・xdpi          : X次元の画面の1インチあたりの正確な物理的なピクセル数
        //・ydpi          : Y次元の画面の1インチあたりの正確な物理的なピクセル数
    }

    /**
     *
     */
    private void gameStart() {
        //ユーザ操作の有効化
        colorBlocksFragment.gameStart();

        mainTimer = new Timer(INITIAL_TIME_LFET, this);
        initializeTimerView();
        return;
        //ここでは mainTimer.start() せず、最初にgetScoreした時にスタートさせる
    }

    public void timerOnFinish() {
        LogUtil.methodCalled("bestScore: " + bestScore + ", currentScore: " + currentScore);
        updateTimerView();

        //ベストスコアをbestScoreにセットして、saveBestScoreで保存。
        if (bestScore < currentScore) {
            bestScore = currentScore;
            updateBestScoreView();
            saveBestScore();
        }

        //ブロックを押せないようにする
        colorBlocksFragment.gameStop();

        //メニューを表示
        showMenu();
    }

    public void timerOnTick(long millisUntilFinished) {
        //millisUntilFinished は引数でもらうが、基本使わない
        //（getTimerLeft()で秒/millisを取得して、それらを使うため）

        //LogUtil.methodCalled("until: " + millisUntilFinished);
        updateTimerView();
    }

    /**
     * mainTimerのスタート前に使う前提。
     * updateTimerView都の違いは以下：
     * ・秒数+1しない
     * ・ミリ秒の表示をする
     *
     */
    private void initializeTimerView() {
        int[] timeLeft = mainTimer.getTimeLeft();
        int second = timeLeft[0];
        int millis = timeLeft[1]; //="0"のはず

        TextView tSecond = (TextView) findViewById(R.id.current_timer_second);
        TextView tMillis = (TextView) findViewById(R.id.current_timer_millis);

        tSecond.setText(String.format("%1$ 2d", second));
        tMillis.setText(":" + String.format("%1$02d", millis));
        tSecond.setTextColor(Color.BLACK);
        tMillis.setTextColor(Color.BLACK);
    }

    private void updateTimerView() {
        int[] timeLeft = mainTimer.getTimeLeft();

        ////@TODO 秒数がゼロでもミリ秒が残っている間はOKなのが直感的に変な感じ。。どっちがいいか。。
        ////int second = timeLeft[0];

        //表示するときだけ、秒数に+1する。（秒数1が終わった瞬間終わり）
        int second = (timeLeft[0] + timeLeft[1] == 0) ? 0 : timeLeft[0] + 1;
        int millis = timeLeft[1];

        TextView tSecond = (TextView) findViewById(R.id.current_timer_second);
        TextView tMillis = (TextView) findViewById(R.id.current_timer_millis);

        //tSecond.setText(String.format("%1$ 2d", second));
        tSecond.setText(String.format("%1$ 2d", second));
        //tSecond.setText(second);

        if (second <= 5) {
            tSecond.setTextColor(Color.RED);
            tMillis.setText(":" + String.format("%1$02d", millis));
            tMillis.setTextColor(Color.RED);
        } else {
            tSecond.setTextColor(Color.BLACK);
            tMillis.setText("");
        }

    }

    /**
     * 前提： this.bestScoreに値がセット済み
     */
    private void updateBestScoreView() {
        TextView tBestScore = (TextView) findViewById(R.id.best_score);
        String t = (bestScore == BEST_SCORE_NOT_SET) ? "---" : String.valueOf(bestScore);
        Resources res = getResources();
        String t1 = res.getString(R.string.main_bestscore_text_left);
        String t2 = res.getString(R.string.main_bestscore_text_right);
        String text = t1 + t + t2;
        tBestScore.setText(text);
    }

    /**
     * @param gameLevel
     */
    public void getScore(int gameLevel) {
        //TODO 引数は他に必要？
        LogUtil.methodCalled("gameLevel=" + gameLevel);

        int second = mainTimer.getTimeLeft()[0];

        currentScore += gameLevel * (second / 4 + 1);
        updateCurrentScoreView();

        mainTimer.addTime(ADDITIONAL_TIME, MAX_TIME_LEFT);
        if (!mainTimer.isStarted()) {
            mainTimer.start();
        }
    }

    private void updateCurrentScoreView(){
        TextView tv = (TextView) findViewById(R.id.current_score);
        Resources res = getResources();
        String t = res.getString(R.string.main_currentscore_text_left);

        //4桁で右詰め
        tv.setText(t + String.format("%1$4d", currentScore));
    }

    /**
     * @param gameLevel
     */
    public void minusScore(int gameLevel) {
        mainTimer.addTime(INCORRECT_PENALTY_TIME, MAX_TIME_LEFT);
        if (!mainTimer.isStarted()) {
            mainTimer.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private int readBestScore() {
        SharedPreferences pref = getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
        int s = pref.getInt(PREFERENCE_KEY_BEST_SCORE, BEST_SCORE_NOT_SET); //第2引数は設定値がないとき
        LogUtil.methodCalled("key: " + PREFERENCE_KEY_BEST_SCORE + ", value: " + s);
        return s;
    }

    private void saveBestScore() {
        //getPreferences()だと、内部で呼ばれるgetSharedPreferences()の
        //nameの補い方がもし変わった場合に、アプリのバージョンアップ後に
        //旧バージョンで設定したものを取得できなくなる恐れがある。
        //（nameごとに設定ファイルが作られる）
        SharedPreferences pref = getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.putInt(PREFERENCE_KEY_BEST_SCORE, bestScore);
        editor.commit();
        LogUtil.methodCalled("key: " + PREFERENCE_KEY_BEST_SCORE + ", value: " + bestScore);

        //TODO 実際にxmlファイルを確認して、想定通りの動きをしていることを確認すべき

        ////for testing
        ////値を削除する場合
        //editor.clear();
        //editor.commit();
        //pref = getPreferences(MODE_PRIVATE);
        //editor = pref.edit();
        //editor.clear();
        //editor.commit();
    }

    public void gameRetryButtonClicked(View button) {
        LogUtil.methodCalled(this.toString());

        //ランダムに、全画面広告を表示
        if(needToShowInterstitialAd()) {
            showInterstitialAdvertisement();
            //TODO 要確認
            //全画面広告を表示すると、Activityが削除されるので、後続処理は不要。
            //。。。と思うが、設定した広告ユニットの上限に達した場合はどうなるんだろ。showされずに返ってくるだけ？
        }

        hideMenu();

        ////落ちる
        //アプリ開始と同じ動きをさせる
        //onCreate(instanceState);

        initializeColorBlocksFragmentView();
        currentScore = 0;
        updateCurrentScoreView();
        gameStart();
    }

    public void gameFinishButtonClicked(View button) {
        LogUtil.methodCalled(this.toString());
        //アプリ終了
        finish(); //これが正しい終了のさせ方？
    }

    private void showMenu() {

        //TODO ゆっくり表示させたい。。
        //try {
        //    Thread.sleep(500); //3000ミリ秒Sleepする
        //} catch (InterruptedException e) {
        //    //do nothing
        //}


        Button b1 = (Button) findViewById(R.id.button_game_retry);
        b1.setAllCaps(false);
        Button b2 = (Button) findViewById(R.id.button_game_finish);
        b2.setAllCaps(false);

        RelativeLayout v = (RelativeLayout) findViewById(R.id.container_menu);
        v.setVisibility(View.VISIBLE);
    }

    private void hideMenu() {
        RelativeLayout v = (RelativeLayout) findViewById(R.id.container_menu);
        v.setVisibility(View.INVISIBLE);
    }

    private void initializeColorBlocksFragmentView() {
        //表示サイズを設定
        int size = getColorBlocksFragmentSize();

        LogUtil.methodCalled("container size: " + size);
        RelativeLayout v = (RelativeLayout) findViewById(R.id.container_color_blocks);
        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.width = size;
        params.height = size;
        v.setLayoutParams(params);

        //フラグメントの配置
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(colorBlocksFragment != null){
            //すでに存在する場合、古いものを削除
            transaction.remove(colorBlocksFragment);
        }
        colorBlocksFragment = new ColorBlocksFragment();
        transaction.add(R.id.container_color_blocks, colorBlocksFragment);
        transaction.commit();
    }

    /**
     * アプリケーションを再起動
     * この方法が正しいのか不明
     */
    public void rebootApplication() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    /**
     * バナー広告を有効化する
     * 前提として以下が必要
     * ・SDK Manager から Google Play Serviceをインストール
     * ・コンパイルさせるよう、build.gradle に依存関係を記述
     * ・manifestファイルにパーミッション等を記述
     * ・adViewをレイアウトファイルに配置
     */
    private void loadBannerAdvertisement(){
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    /**
     * インタースティシャル広告（全面広告）の初期化
     */
    private void initializeInterstitialAdvertisement(){
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        AdRequest adRequest = new AdRequest.Builder().build();

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                //TODO もっとうまい方法あるはず。単純に広告表示前の画面に戻したいだけ。。
                rebootApplication(); //アプリ自体を再起動
            }
        });

        interstitialAd.loadAd(adRequest);
    }

    /**
     * インタースティシャル広告（全面広告）の表示
     * 表示させたら、ユーザは広告をタップ、✕ボタンをタップ、物理ボタン（戻るとか）操作が可能
     */
    private void showInterstitialAdvertisement(){
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    /**
     * 単純に乱数から、全画面広告を表示させるかを決めて返す
     * これとは別に、interstitialAd.showできる上限？？を広告ID作成時に設定済みなので注意。
     *
     * @return
     */
    private boolean needToShowInterstitialAd(){
        Random random = new Random();
        int n = random.nextInt(INTERSTITIAL_ADVERTISEMENT_DISPLAY_RATE);
        LogUtil.methodCalled("乱数: " + n);
        return (n == 0) ? true : false;
    }

}
