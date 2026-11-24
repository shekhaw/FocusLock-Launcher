/*
 * MIT License
 * Copyright (c) 2025-2026 Donny Yang
 * Project: AndroidLiquidGlassView
 */

package com.dumbphone.launcher.ui.liquidglass.impl;

import android.graphics.Canvas;

public interface Impl {
    void onSizeChanged(int w, int h);
    void onPreDraw();
    void draw(Canvas c);
    default void dispose() {}
}
