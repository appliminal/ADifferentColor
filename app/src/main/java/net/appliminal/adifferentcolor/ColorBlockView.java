package net.appliminal.adifferentcolor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 *
 */
public class ColorBlockView extends TextView implements OnClickListener {

    //このviewが正解か不正解か
    private boolean isCorrectColor;

    //このviewを保持しているFragment
    //クリック時は正解/不正解に応じて、Fragmentの規定のメソッドを呼ぶ
    private ColorBlocksFragment parentFragment;

    private int color;

    /**
     * コンストラクタ
     * @param c
     * @param b
     * @param f
     * @param color Color.HSVToColor(xxx) で変換後のintの値
     */
    public ColorBlockView(Context c, boolean b, ColorBlocksFragment f, int color) {
        super(c);
        isCorrectColor = b;
        parentFragment = f;
        setBackgroundResource(R.drawable.block_shape);
        setOnClickListener(this);

        this.color = color;
        setBackgroundColor(color);
    }

    @Override
    public void onClick(View v) {
        if(isCorrectColor){
            parentFragment.answeredCorrectly();
        }
        else{
            parentFragment.answeredIncorrectly();
        }
    }

    /**
     *
     * @param color
     */
    public void setBackgroundColor(int color) {
        GradientDrawable drawable = (GradientDrawable) this.getBackground();
        drawable.setColor(color);
    }

    /**
     * ver1.3〜
     * 非表示（白色）にする
     */
    public void hide() {
        GradientDrawable drawable = (GradientDrawable) this.getBackground();
        drawable.setColor(Color.WHITE);
    }

    /**
     * ver1.3〜
     * コンストラクタで指定された、元の色に戻す。
     */
    public void show() {
        GradientDrawable drawable = (GradientDrawable) this.getBackground();
        drawable.setColor(color);
    }

}
