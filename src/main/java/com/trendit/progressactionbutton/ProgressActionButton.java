package com.trendit.progressactionbutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * brief: a progress button view
 * author: yinlijun
 * email: yinlijun2004 at gmail dot com
 * date: 2018/4/11.
 */

@SuppressLint("AppCompatCustomView")
public class ProgressActionButton extends TextView {
    public static final int STATE_INIT = 0;
    public static final int STATE_PROGRESS = 1;
    public static final int STATE_SUCCESS = 2;
    public static final int STATE_FAIL = 3;

    private Context context;
    private float buttonRadius;
    private int failBg;
    private int successBg;
    private int initBg;
    private int disableBg;
    private int progressBg;
    private int progressFg;
    private RectF backgroundBounds;
    private volatile Paint textPaint;

    private int state;
    private int progress;

    public ProgressActionButton(Context context) {
        super(context, null);
    }

    public ProgressActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressActionButton);

        buttonRadius = a.getDimension(R.styleable.ProgressActionButton_buttonRadius, context.getResources().getDimension(R.dimen.default_button_radius));
        initBg = a.getResourceId(R.styleable.ProgressActionButton_initBg, R.drawable.default_init_bg);
        failBg = a.getResourceId(R.styleable.ProgressActionButton_failBg, R.drawable.default_fail_bg);
        successBg = a.getResourceId(R.styleable.ProgressActionButton_successBg, R.drawable.default_success_bg);
        disableBg = a.getResourceId(R.styleable.ProgressActionButton_disableBg, R.drawable.default_disable_bg);
        progressBg = a.getResourceId(R.styleable.ProgressActionButton_progressBg, R.drawable.default_progress_bg);
        progressFg = a.getResourceId(R.styleable.ProgressActionButton_progressFg, R.drawable.default_progress_fg);

        state = STATE_INIT;
        progress = 0;

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(getTextSize());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //解决文字有时候画不出问题
            setLayerType(LAYER_TYPE_SOFTWARE, textPaint);
        }
    }

    public void setProgress(int progress) {
        state = STATE_PROGRESS;
        this.progress = progress;
        this.invalidate();
    }

    public void setFail() {
        state = STATE_FAIL;
    }

    public void setSuccess() {
        state = STATE_SUCCESS;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInEditMode()) {
            drawing(canvas);
        }
    }

    private void drawing(Canvas canvas) {
        drawBackground(canvas);
        drawTextAbove(canvas);
    }

    private void drawTextAbove(Canvas canvas) {
        final float y = canvas.getHeight() / 2 - (textPaint.descent() / 2 + textPaint.ascent() / 2);
        String txt = getText().toString();

        float textWidth = textPaint.measureText(txt);
        textPaint.setColor(getCurrentTextColor());

        canvas.drawText(txt, (getMeasuredWidth() - textWidth) / 2, y, textPaint);
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Bitmap drawRadiusBitmap(Bitmap bg, Bitmap fg, Rect rect, RectF rectF) {
        Bitmap output = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawRoundRect(rectF, buttonRadius, buttonRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bg, rect, rect, paint);
        if(fg != null) {
            Rect fgRect = new Rect(rect.left, rect.top, rect.left + (int) (rect.width() * progress / 100.0f), rect.bottom);
            canvas.drawBitmap(fg, fgRect, fgRect, paint);
        }

        return output;
    }

    private Bitmap getBgBitmap() {
        int drawable = initBg;
        switch (state) {
            case STATE_INIT:
                drawable =  initBg;
                break;
            case STATE_FAIL:
                drawable = failBg;
                break;
            case STATE_PROGRESS:
                drawable = progressBg;
                break;
            case STATE_SUCCESS:
                drawable = successBg;
                break;
        }
        return getBitmap(drawable);
    }

    private Bitmap getFgBitmap() {
        int drawable = progressBg;
        switch (state) {
            case STATE_PROGRESS:
                drawable = progressFg;
                break;
            default:
                return null;
        }
        return getBitmap(drawable);
    }

    private void drawBackground(Canvas canvas) {
        Bitmap fg = getFgBitmap();
        Bitmap bg = getBgBitmap();

        Rect rect = new Rect();
        float radius = getMeasuredHeight() / 2;
        if (buttonRadius == 0 || buttonRadius > radius) {
            buttonRadius = radius;
        }

        rect.left = 0;
        rect.top = 0;
        rect.right = getMeasuredWidth();
        rect.bottom = getMeasuredHeight();
        backgroundBounds = new RectF(rect);

        Bitmap bitmap = drawRadiusBitmap(bg, fg, rect, backgroundBounds);

        canvas.drawBitmap(bitmap, rect ,backgroundBounds, null);
    }
}