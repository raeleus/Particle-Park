/*
 * The MIT License
 *
 * Copyright 2019 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ray3k.particlepark;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

public class GradientDrawable extends BaseDrawable {
    private final Color color1 = new Color(Color.WHITE);
    private final Color color2 = new Color(Color.WHITE);
    private final Color color3 = new Color(Color.WHITE);
    private final Color color4 = new Color(Color.WHITE);
    private final Color temp = new Color();
    private TextureRegion region;
    private float paddingLeft;
    private float paddingRight;
    private float paddingTop;
    private float paddingBottom;
    
    private final float[] verts = new float[20];
    
    public GradientDrawable(TextureRegion region) {
        this.region = region;
        initialize();
    }

    private void initialize() {
        setMinWidth(region.getRegionWidth());
        setMinHeight(region.getRegionHeight());
    }
    
    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        int i = 0;
        verts[i++] = x + paddingLeft;
        verts[i++] = y + paddingBottom;
        temp.set(color1);
        verts[i++] = temp.mul(batch.getColor()).toFloatBits();
        verts[i++] = region.getU();
        verts[i++] = region.getV2();
        
        verts[i++] = x + paddingLeft;
        verts[i++] = y + height - paddingTop;
        temp.set(color2);
        verts[i++] = temp.mul(batch.getColor()).toFloatBits();
        verts[i++] = region.getU();
        verts[i++] = region.getV();
        
        verts[i++] = x + width - paddingRight;
        verts[i++] = y + height - paddingTop;
        temp.set(color3);
        verts[i++] = temp.mul(batch.getColor()).toFloatBits();
        verts[i++] = region.getU2();
        verts[i++] = region.getV();
        
        verts[i++] = x + width - paddingRight;
        verts[i++] = y + paddingBottom;
        temp.set(color4);
        verts[i++] = temp.mul(batch.getColor()).toFloatBits();
        verts[i++] = region.getU2();
        verts[i++] = region.getV2();
        
        batch.draw(region.getTexture(), verts, 0, verts.length);
    }

    public Color getColor1() {
        return color1;
    }

    public Color getColor2() {
        return color2;
    }

    public Color getColor3() {
        return color3;
    }

    public Color getColor4() {
        return color4;
    }

    public TextureRegion getRegion() {
        return region;
    }

    public void setRegion(TextureRegion region) {
        this.region = region;
        initialize();
    }

    @Override
    public float getMinHeight() {
        return super.getMinHeight();
    }

    @Override
    public float getMinWidth() {
        return super.getMinWidth();
    }

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(float paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public float getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(float paddingRight) {
        this.paddingRight = paddingRight;
    }

    public float getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(float paddingTop) {
        this.paddingTop = paddingTop;
    }

    public float getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(float paddingBottom) {
        this.paddingBottom = paddingBottom;
    }
}
