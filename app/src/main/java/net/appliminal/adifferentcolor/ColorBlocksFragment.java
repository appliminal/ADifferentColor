package net.appliminal.adifferentcolor;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Random;

/**
 *
 * このフラグメントの大きさは親要素に合わせる
 *
 */
public class ColorBlocksFragment extends Fragment {

    private int numberOfItems; //縦（=横）のブロック数
    private int gameLevel;
    private final int INITIAL_NUMBER_OF_ITEMS = 2;
    private final int MAX_NUMBER_OF_ITEMS = 10;
    private final int INITIAL_GAME_LEVEL = 1;

    //private int currentColor; //現在表示している色
    private float[] currentColorHSV = null; //メインの色（HSV形式）
    //private int COLOR_NOT_SET;

    private boolean isGamePaused;

    private RelativeLayout rootView;

    //ver1.3〜 正解ブロックを保持
    public ColorBlockView correctColorBlockView;

    /**
     * コンストラクタ
     */
    public ColorBlocksFragment() {
        numberOfItems = this.INITIAL_NUMBER_OF_ITEMS;
        gameLevel = this.INITIAL_GAME_LEVEL;
        isGamePaused = true;
        //this.currentColor = COLOR_NOT_SET;
        currentColorHSV = null;
    }

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View要素。ここで返したものがcontainer以下に追加される
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //rootViewの初期化はコンストラクタではなくここで
        rootView = new RelativeLayout(this.getActivity()); //もしくはレイアウトファイルを作っておいてinflateさせるなど

        currentColorHSV = selectNextColorHSV();

        resetColorBlocks();

        return rootView;
    }

    /**
     * カラーブロックを配置し、各ブロックにOnClickListenerをセット
     * ブロックの色は、currentColorHSVのものとするが、
     * 1つのブロックだけ、微妙に色の違うものとする。
     *
     */
    private void resetColorBlocks() {
        LogUtil.methodCalled(this.toString());

        if(rootView != null) {
            rootView.removeAllViews();
            //this.rootView.invalidate(); //再描画は特に不要なようす
        }

        int blockIndex = 0;
        int correctBlockIndex;
        LinearLayout vLayout, hLayout;
        ColorBlockView cbView;
        boolean isColorCorrect;
        LinearLayout.LayoutParams params;
        float[] colorHSV;

        //マージン等はブロックの大きさに応じて微調整するため
        int blockMargin; //@TODO intでいいのか
        float blockRadius;
        GradientDrawable gDrawable;

        //numberOfItems^2個のブロックのうち、色違いにするブロックの
        //インデックスをランダムに生成（0〜numberOfItems^2-1まで）
        Random random = new Random();
        correctBlockIndex = random.nextInt(numberOfItems * numberOfItems);
        //correctBlockIndex = 2; //for testing

        //縦方向にLinerLayoutを配置
        vLayout = new LinearLayout(this.getActivity());
        vLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        vLayout.setOrientation(LinearLayout.VERTICAL);
        rootView.addView(vLayout);

        for(int i = 0; i < numberOfItems; i++) {

            //縦方向にLinerLayoutを配置
            hLayout = new LinearLayout(this.getActivity());
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            hLayout.setOrientation(LinearLayout.HORIZONTAL);
            vLayout.addView(hLayout, params);

            for (int j = 0; j < numberOfItems; j++) {
                if(blockIndex == correctBlockIndex){
                    isColorCorrect = true;
                    //色をメインのものから変える
                    colorHSV = adjustColorHSV(currentColorHSV);
                }
                else{
                    isColorCorrect = false;
                    colorHSV = currentColorHSV;
                }

                //このブロックの正解/不正解を設定し、インスタンス化
                //色の設定もコンストラクタの中で。
                cbView = new ColorBlockView(this.getActivity(), isColorCorrect, this, Color.HSVToColor(colorHSV));

                //パラメータやマージンはColorBlockViewの属性としてはもたず、ここで設定
                params = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                params.weight = 1;

                //@TODO 要調整
                if(numberOfItems < 3){
                    blockMargin = 8;
                    blockRadius = 15;
                }
                else if(numberOfItems < 5){
                    blockMargin = 4;
                    blockRadius = 15 - numberOfItems;
                }
                else if(numberOfItems < 8){
                    blockMargin = 3;
                    blockRadius = 15 - numberOfItems;
                }
                else{
                    blockMargin = 2;
                    blockRadius = 12 - numberOfItems;
                }

                params.setMargins(blockMargin, blockMargin, blockMargin, blockMargin);
                gDrawable = (GradientDrawable) cbView.getBackground();
                gDrawable.setCornerRadius(blockRadius);
                hLayout.addView(cbView, params);
                blockIndex++;

                //ここでcbView.setBackgroundColorをすると
                //事前にcbView.setBackgroundResourceしたリソース(shapeのビュー)を上書きしてしまうようす
                //cbView.setBackgroundColor(Color.parseColor("#99ff99"));
                //→ そんなことないような。。

                //ver1.3〜 正解ブロックをインスタンス変数にセット
                if(isColorCorrect){
                    correctColorBlockView = cbView;
                }

            }
        }
    }


    /**
     * //@TODO 要調整
     *
     * @return
     */
    private float[] selectNextColorHSV(){
        /*
        float[] hsv = new float[3]; で確保してます。
        hsv 配列のインデクス 0, 1, 2 に対応して、色相、彩度、明度が格納されています。

        色相に関しては 0.0 - 360.0 の値が格納されています。
        彩度、明度に関しては 0.0 - 1.0 の値が格納されています。
        */

        //ランダムに色を生成
        //int range = 0xFF*0xFF*0xFF; //256*256*256=16777216
        int r, g, b;
        float[] hsv = new float[3]; //インデクス 0, 1, 2 に対応して、色相、彩度、明度。
        int range =0xFF; //255
        Random random = new Random();
        r = random.nextInt(range + 1); //0〜rangeの乱数を生成
        g = random.nextInt(range + 1);
        b = random.nextInt(range + 1);
        //int color = r.nextInt(range);
        Log.i("range: " + range + ", r=" + r + ", g=" + g + ", b=" + b, "");

        android.graphics.Color.RGBToHSV(r, g, b, hsv);
        //Log.i("HSV_H", "Hue=" + hsv[0]);
        //Log.i("HSV_H", "Saturation=" + hsv[1]);
        //Log.i("HSV_H", "Value=" + hsv[2]);

        //彩度は0.70前後
        float r1 = (65 + random.nextInt(10)) / 100f;
        //hsv[1] = 0.75f; //float型で
        hsv[1] = r1;
        //明度は0.65前後
        float r2 = (60 + random.nextInt(10)) / 100f;
        hsv[2] = r2;

        return hsv;
    }

    /**
     * レベルに応じて、色を少し変更したものを返す
     *
     * @param hsv 変更前の色
     */
    private float[] adjustColorHSV(float[] hsv){

        float[] adjustedHSV = hsv.clone();

        //レベルが高いほど、色の変化率が小さくなるように。
        //明度を上げて彩度を下げるとパステルカラーになるらしい。
        //ランダムに、濃くする場合と薄くする場合を分ける。

        if((new Random()).nextInt(2) == 0){
            //少し薄くする
            adjustedHSV[1] -= 0.2 / gameLevel;
            adjustedHSV[2] += 0.2 * 3 / gameLevel;
        }
        else {
            //少し濃くする
            adjustedHSV[1] += 0.3 / gameLevel;
            adjustedHSV[2] -= 0.2 * 2 / gameLevel;
        }

        return adjustedHSV;
    }

    /**
     *
     */
    protected void answeredCorrectly(){
        LogUtil.methodCalled(this.toString());

        if(isGamePaused) {
            return;
        }

        if(((MainActivity) this.getActivity()).isMenuVisible() == false){
            //ver1.3〜
            //正解音を鳴らす
            int soundIndex = ((MainActivity) this.getActivity()).SOUND_CORRECT_1;
            ((MainActivity) this.getActivity()).sound(soundIndex);
        }

        ((MainActivity)this.getActivity()).getScore(gameLevel);

        levelUp();
        this.resetColorBlocks();
    }

    /**
     *
     */
    protected void answeredIncorrectly(){
        LogUtil.methodCalled(this.toString());

        if(isGamePaused){
            return;
        }

        if(((MainActivity) this.getActivity()).isMenuVisible() == false) {
            //ver1.3〜
            //不正解音を鳴らす
            int soundIndex = ((MainActivity) this.getActivity()).SOUND_WRONG_1;
            ((MainActivity) this.getActivity()).sound(soundIndex);
        }

        ((MainActivity)this.getActivity()).minusScore(gameLevel);
    }

    protected void gameStart(){
        LogUtil.methodCalled(this.toString());
        isGamePaused = false;
    }

    protected void gameStop(){
        LogUtil.methodCalled(this.toString());
        isGamePaused = true;
    }


    /**
     *
     */
    private void levelUp(){

        switch(numberOfItems){
            case MAX_NUMBER_OF_ITEMS:
                break;
            case 3: //3
                if(3 < gameLevel){ //3
                    numberOfItems++;
                }
                break;
            case 4: //4
                if(5 < gameLevel){ //5
                    numberOfItems++;
                }
                break;
            case 6:
                if(15 < gameLevel){
                    numberOfItems++;
                }
                break;
            case 8: //8
                if(30 < gameLevel){ //20
                    numberOfItems++;
                }
                break;
            default:
                numberOfItems++;
                break;
        }

        //@FIXME ColorBlocksFragmentの大きさはMainActivityで制御すべき
        //少し大きくしてみる。。
        if(numberOfItems == MAX_NUMBER_OF_ITEMS) {
            int size = ((MainActivity) this.getActivity()).getColorBlocksFragmentSize();
            size *= 1.1;
            RelativeLayout v = (RelativeLayout) this.getActivity().findViewById(R.id.container_color_blocks);
            ViewGroup.LayoutParams p = v.getLayoutParams();
            p.width = size;
            p.height = size;
            v.setLayoutParams(p);
        }
        //

        gameLevel++;

        currentColorHSV = selectNextColorHSV();

        LogUtil.methodCalled("Next Level: " + gameLevel + ", Items: " + numberOfItems);

        //currentColor = color;
        //Log.i("<C> red " + Color.red(color) + " green " + Color.green(color) + " blue " + Color.blue(color), "");
        //Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);

    }



}