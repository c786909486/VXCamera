package cn.ckz.camera.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import cn.ckz.camera.utils.DisplayUtils;

/**
 * Created by hz-java on 2018/7/16.
 */

public class ImageViewFix extends ImageView {
    private Context context;
    private Paint textPaint;
    private Paint namePaint;
    public ImageViewFix(Context context) {
        this(context,null);
    }

    public ImageViewFix(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ImageViewFix(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initSize();
    }


    private void initPaint(){
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE );
        textPaint.setTextSize(DisplayUtils.sp2px(context,13f));

        namePaint = new Paint();
        namePaint.setAntiAlias(true);
        namePaint.setColor(Color.WHITE );
        namePaint.setTextSize(DisplayUtils.sp2px(context,16f));
    }

    public void setTime(String time){
        this.time = time;
       invalidate();
    }
    private String time;
    private int width;
    private int height;
    private String name;

    public void setName(String name) {
        this.name = name;
        invalidate();
    }

    private void initSize(){
        width = getWidth();
        height = getHeight();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initPaint();
        Rect rect = new Rect();
       if (!TextUtils.isEmpty(time)){
           textPaint.getTextBounds(time, 0, time.length(), rect);
           int textWidth = rect.width();//文本的宽度
           int textHeight = rect.height();//文本的高度
           canvas.drawText(time,getWidth()-DisplayUtils.dp2px(context,15f)- textWidth,getHeight()-DisplayUtils.dp2px(context,15f)
                   ,textPaint);
       }
//        canvas.drawText(name,DisplayUtils.dp2px(context,15f),getHeight()-DisplayUtils.dp2px(context,15f),namePaint);
    }
}
