/*
 * MIT License
 * Copyright (c) 2025-2026 Donny Yang
 * Project: AndroidLiquidGlassView
 */

package com.dumbphone.launcher.ui.liquidglass.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dumbphone.launcher.ui.liquidglass.LiquidGlass;
import com.dumbphone.launcher.ui.liquidglass.Config;
import com.dumbphone.launcher.ui.liquidglass.util.LiquidTracker;
import com.dumbphone.launcher.ui.liquidglass.util.Utils;

public class LiquidGlassView extends ViewGroup {

    private LiquidGlass glass;
    private ViewGroup customSource;
    private final Context context;
    private float cornerRadius = Utils.dp2px(getResources(), 26),
            refractionHeight = Utils.dp2px(getResources(), 20),
            refractionOffset = -Utils.dp2px(getResources(), 70),
            tintAlpha = 0.0f,
            tintColorRed = 1.0f,
            tintColorGreen = 1.0f,
            tintColorBlue = 1.0f,
            blurRadius = 0.01f,
            dispersion = 0.55f,
            downX, downY, startTx, startTy;
    private boolean draggableEnabled = false;
    private boolean elasticEnabled = false;
    private boolean touchEffectEnabled = false;
    private Config config;
    private LiquidTracker liquidTracker;

    private Paint glowPaint;
    private float glowX, glowY;
    private boolean isTouching = false;

    private final Path clipPath = new Path();
    private final RectF clipRect = new RectF();

    public LiquidGlassView(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        this.context = context;
        init();
    }

    public LiquidGlassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        this.context = context;
        init();
    }

    public LiquidGlassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        this.context = context;
        init();
    }

    private void init() {
        setClipToPadding(false);
        setClipChildren(false);
        liquidTracker = new LiquidTracker(this);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        clipRect.set(0, 0, getWidth(), getHeight());
        clipPath.reset();
        clipPath.addRoundRect(
                clipRect,
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
        );

        int save = canvas.save();
        canvas.clipPath(clipPath);

        super.dispatchDraw(canvas);

        canvas.restoreToCount(save);

        if (touchEffectEnabled && isTouching) {
            canvas.save();
            canvas.clipPath(clipPath);

            float radius = Math.max(getWidth(), getHeight()) * 0.8f;
            int[] colors = {Color.argb(60, 255, 255, 255), Color.TRANSPARENT};
            float[] stops = {0f, 1f};

            RadialGradient gradient =
                    new RadialGradient(glowX, glowY, radius, colors, stops, Shader.TileMode.CLAMP);

            glowPaint.setShader(gradient);
            canvas.drawRect(clipRect, glowPaint);
            canvas.restore();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    public void bind(ViewGroup source) {
        this.customSource = source;
        if (glass != null) {
            glass.init(source);
        } else {
            post(this::ensureGlass);
        }
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        if (config != null) {
            config.CORNER_RADIUS_PX = cornerRadius;
        }
        if (glass != null) {
            glass.updateParameters();
        }
        invalidate();
    }

    public void setRefractionHeight(float refractionHeight) {
        this.refractionHeight = refractionHeight;
        updateConfig();
    }

    public void setRefractionOffset(float refractionOffset) {
        this.refractionOffset = -Math.abs(refractionOffset);
        updateConfig();
    }

    public void setTintColorRed(float tintColorRed) {
        this.tintColorRed = tintColorRed;
        updateConfig();
    }

    public void setTintColorGreen(float tintColorGreen) {
        this.tintColorGreen = tintColorGreen;
        updateConfig();
    }

    public void setTintColorBlue(float tintColorBlue) {
        this.tintColorBlue = tintColorBlue;
        updateConfig();
    }

    public void setTintAlpha(float tintAlpha) {
        this.tintAlpha = tintAlpha;
        updateConfig();
    }

    public void setDispersion(float dispersion) {
        this.dispersion = dispersion;
        updateConfig();
    }

    public void setBlurRadius(float blurRadius) {
        this.blurRadius = blurRadius;
        updateConfig();
    }

    public void setDraggableEnabled(boolean draggableEnabled) {
        this.draggableEnabled = draggableEnabled;
    }

    public void setElasticEnabled(boolean elasticEnabled) {
        this.elasticEnabled = elasticEnabled;
    }

    public void setTouchEffectEnabled(boolean touchEffectEnabled) {
        this.touchEffectEnabled = touchEffectEnabled;
    }

    private void updateConfig() {
        if (config != null) {
            config.REFRACTION_HEIGHT = refractionHeight;
            config.REFRACTION_OFFSET = refractionOffset;
            config.TINT_ALPHA = tintAlpha;
            config.TINT_COLOR_RED = tintColorRed;
            config.TINT_COLOR_GREEN = tintColorGreen;
            config.TINT_COLOR_BLUE = tintColorBlue;
            config.DISPERSION = dispersion;
            config.BLUR_RADIUS = blurRadius;
        }
        if (glass != null) {
            glass.updateParameters();
        }
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::ensureGlass);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeGlass();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            if (w > 0 && h > 0) {
                float maxPx = h / 2f;
                if (cornerRadius > maxPx) {
                    cornerRadius = maxPx;
                }
                rebuild();
            }
        }
    }

    private void rebuild() {
        removeGlass();
        post(this::ensureGlass);
    }

    private void ensureGlass() {
        if (glass != null) return;

        int w = getWidth();
        int h = getHeight();
        if (w <= 0) w = Utils.getDeviceWidthPx(context);
        if (h <= 0) h = getResources().getDisplayMetrics().heightPixels;

        config = new Config();
        config.configure(new Config.Overrides()
                .noFilter()
                .contrast(0f)
                .whitePoint(0f)
                .chromaMultiplier(1f)
                .blurRadius(blurRadius)
                .cornerRadius(cornerRadius)
                .refractionHeight(refractionHeight)
                .refractionOffset(refractionOffset)
                .tintAlpha(tintAlpha)
                .tintColorRed(tintColorRed)
                .tintColorGreen(tintColorGreen)
                .tintColorBlue(tintColorBlue)
                .dispersion(dispersion)
                .size(w, h)
        );

        glass = new LiquidGlass(getContext(), config);

        LayoutParams lp = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        addView(glass, 0, lp);

        ViewGroup source = customSource;
        if (source != null) {
            glass.init(source);
        }
    }

    private void removeGlass() {
        if (glass != null) {
            removeView(glass);
            glass = null;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(android.view.MotionEvent e) {
        if (!draggableEnabled && !touchEffectEnabled) return super.onTouchEvent(e);
        if (elasticEnabled) liquidTracker.applyMovement(e);

        switch (e.getActionMasked()) {
            case android.view.MotionEvent.ACTION_DOWN:
                if (touchEffectEnabled) {
                    isTouching = true;
                    liquidTracker.animateScale(1.02f);
                    glowX = e.getX();
                    glowY = e.getY();
                    invalidate();
                }

                if (draggableEnabled) {
                    downX = e.getRawX();
                    downY = e.getRawY();
                    startTx = getTranslationX();
                    startTy = getTranslationY();
                    return true;
                }
                break;
            case android.view.MotionEvent.ACTION_MOVE: {
                if (touchEffectEnabled) {
                    glowX = e.getX();
                    glowY = e.getY();
                    invalidate();
                }

                if (draggableEnabled) {
                    float dx = e.getRawX() - downX;
                    float dy = e.getRawY() - downY;
                    float tx = startTx + dx;
                    float ty = startTy + dy;

                    ViewGroup parent = (ViewGroup) getParent();
                    if (parent != null) {
                        int pw = parent.getWidth(), ph = parent.getHeight();
                        int w = getWidth(), h = getHeight();
                        if (pw > 0 && ph > 0 && w > 0 && h > 0) {
                            float minX = -getLeft();
                            float maxX = pw - getLeft() - w;
                            float minY = -getTop();
                            float maxY = ph - getTop() - h;
                            if (tx < minX) tx = minX;
                            if (tx > maxX) tx = maxX;
                            if (ty < minY) ty = minY;
                            if (ty > maxY) ty = maxY;
                        }
                    }
                    setTranslationX(tx);
                    setTranslationY(ty);
                    return true;
                }
                break;
            }
            case android.view.MotionEvent.ACTION_UP:
            case android.view.MotionEvent.ACTION_CANCEL:
                if (touchEffectEnabled) {
                    isTouching = false;
                    liquidTracker.animateScale(1f);
                    invalidate();
                }
                if (draggableEnabled) return true;
                break;
        }

        boolean superResult = super.onTouchEvent(e);
        return touchEffectEnabled || superResult;
    }
}
