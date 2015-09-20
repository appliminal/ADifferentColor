package net.appliminal.adifferentcolor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.View.OnClickListener;

/**
 *
 */
public class ColorBlockView extends View implements OnClickListener {

    //このviewが正解か不正解か
    private boolean isCorrectColor;

    //このviewを保持しているFragment
    //クリック時は正解/不正解に応じて、Fragmentの規定のメソッドを呼ぶ
    private ColorBlocksFragment parentFragment;

    /**
     * コンストラクタ
     * @param c
     * @param b
     * @param f
     */
    public ColorBlockView(Context c, boolean b, ColorBlocksFragment f) {
        super(c);
        isCorrectColor = b;
        parentFragment = f;
        setBackgroundResource(R.drawable.block_shape);
        setOnClickListener(this);
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


}
