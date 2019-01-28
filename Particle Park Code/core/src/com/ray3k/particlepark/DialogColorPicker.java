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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

/**
 *
 * @author Raymond
 */
public class DialogColorPicker extends Dialog {
    private Core core;
    private Skin skin;
    private Color selectedColor;
    private Color previousColor;
    private static enum Mode {
        HUE, SATURATION, BRIGHTNESS, RED, GREEN, BLUE
    }
    private Mode mode;
    private GradientDrawable boxGradient1;
    private GradientDrawable boxGradient2;
    private SliderStyle sliderStyle;
    private Image reticle;
    
    public DialogColorPicker(String title, Skin skin, Core core, Color currentColor) {
        super(title, skin, "dialog");
        this.core = core;
        this.skin = skin;
        this.selectedColor = new Color(currentColor);
        this.previousColor = currentColor;
        mode = Mode.HUE;
        
        getTitleTable().padLeft(10).padRight(10);
        
        getTitleTable().defaults().space(20);
        Button button = new Button(skin, "close");
        getTitleTable().add(button).expandX().right();
        button.addListener(core.handListener);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                fire(new ColorPickerEvent(null));
                hide();
            }
        });
        
        refresh();
    }
    
    private void refresh() {
        Table root = getContentTable();
        root.clear();
        
        Table table = new Table();
        table.setBackground(skin.getDrawable("color-box-bg"));
        root.add(table);
        
        Stack stack = new Stack();
        table.add(stack);
        
        Image image = new Image(skin, "hue-box");
        stack.add(image);
        
        boxGradient1 = new GradientDrawable(skin.getAtlas().findRegion("white"));
        image = new Image(boxGradient1);
        stack.add(image);
        
        boxGradient2 = new GradientDrawable(skin.getAtlas().findRegion("white"));
        image = new Image(boxGradient2);
        stack.add(image);
        
        Table reticleTable = new Table();
        reticleTable.setClip(true);
        stack.add(reticleTable);
        
        reticle = new Image(skin, "reticle");
        reticleTable.addActor(reticle);
        
        stack.addListener(core.handListener);
        stack.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                reticle.setPosition(x, y, Align.center);
                boxColorSelection(x, y);
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                boxColorSelection(x, y);
                updateBoxColor();
            }
        });
        
        Container container = new Container();
        container.setBackground(skin.getDrawable("hue-slider-bg"));
        root.add(container);
        
        image = new Image(skin, "hue-slider");
        
        sliderStyle = new SliderStyle(skin.get("color-slider", SliderStyle.class));
        sliderStyle.background = new GradientDrawable(skin.getAtlas().findRegion("white"));
        ((GradientDrawable) sliderStyle.background).setMinWidth(25);
        ((GradientDrawable) sliderStyle.background).setMinHeight(273);
        ((GradientDrawable) sliderStyle.background).setPaddingTop(8);
        ((GradientDrawable) sliderStyle.background).setPaddingBottom(8);
        final Slider slider = new Slider(0, 1, .01f, true, sliderStyle);
        slider.addListener(core.handListener);
        final ChangeListener sliderChangeListener = new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                switch (mode) {
                    case HUE:
                        TextField textField = findActor("hue-field");
                        textField.setText(Integer.toString(MathUtils.floor(slider.getValue() * 360)));
                        updateColorHSB();
                        break;
                    case SATURATION:
                        textField = findActor("saturation-field");
                        textField.setText(Integer.toString(MathUtils.floor(slider.getValue() * 100)));
                        updateColorHSB();
                        break;
                    case BRIGHTNESS:
                        textField = findActor("brightness-field");
                        textField.setText(Integer.toString(MathUtils.floor(slider.getValue() * 100)));
                        updateColorHSB();
                        break;
                    case RED:
                        textField = findActor("red-field");
                        textField.setText(Integer.toString(MathUtils.floor(slider.getValue() * 255)));
                        updateColorRGB();
                        break;
                    case GREEN:
                        textField = findActor("green-field");
                        textField.setText(Integer.toString(MathUtils.floor(slider.getValue() * 255)));
                        updateColorRGB();
                        break;
                    case BLUE:
                        textField = findActor("blue-field");
                        textField.setText(Integer.toString(MathUtils.floor(slider.getValue() * 255)));
                        updateColorRGB();
                        break;
                }
                updateBoxColor();
            }
        };
        slider.addListener(sliderChangeListener);
        
        stack = new Stack(image, slider);
        container.fill();
        container.size(25, 273);
        container.setActor(stack);
        
        table = new Table();
        root.add(table);
        
        table.defaults().space(5);
        Table subTable = new Table();
        subTable.setBackground(skin.getDrawable("color-box-bg"));
        table.add(subTable);
        
        image = new Image(skin, "color-box");
        image.setName("color-image");
        image.setColor(selectedColor);
        subTable.add(image);
        
        subTable.row();
        image = new Image(skin, "color-box");
        image.setColor(previousColor);
        subTable.add(image);
        
        subTable = new Table();
        table.add(subTable);
        
        subTable.defaults().width(125);
        TextButton textButton = new TextButton("OK", skin);
        subTable.add(textButton);
        textButton.addListener(core.handListener);
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                fire(new ColorPickerEvent(selectedColor));
                hide();
            }
        });
        
        subTable.row();
        textButton = new TextButton("Cancel", skin);
        subTable.add(textButton);
        textButton.addListener(core.handListener);
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                fire(new ColorPickerEvent(null));
                hide();
            }
        });
        
        table.row();
        subTable = new Table();
        table.add(subTable).colspan(2).left();
        
        subTable.defaults().space(0).padBottom(0);
        ButtonGroup buttonGroup = new ButtonGroup<Button>();
        Button button = new Button(skin, "radio");
        button.setName("hue-button");
        buttonGroup.add(button);
        subTable.add(button).padBottom(2);
        button.addListener(core.handListener);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (((Button)actor).isChecked()) {
                    mode = Mode.HUE;
                    
                    slider.removeListener(sliderChangeListener);
                    slider.setValue(selectedColor.toHsv(new float[3])[0] / 360f);
                    slider.addListener(sliderChangeListener);
                    
                    updateBoxColor();
                    updateSliderColor();
                }
            }
        });
        
        Label label = new Label("H", skin);
        subTable.add(label);
        
        TextField textField = new TextField("", skin, "color");
        textField.setName("hue-field");
        textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[0])));
        textField.setDisabled(true);
        subTable.add(textField).width(100).spaceLeft(10);
        textField.addListener(core.iBeamListener);
        
        subTable.row();
        button = new Button(skin, "radio");
        buttonGroup.add(button);
        subTable.add(button).padBottom(2);
        button.addListener(core.handListener);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (((Button)actor).isChecked()) {
                    mode = Mode.SATURATION;
                    
                    slider.removeListener(sliderChangeListener);
                    slider.setValue(selectedColor.toHsv(new float[3])[1]);
                    slider.addListener(sliderChangeListener);
                    
                    updateBoxColor();
                    updateSliderColor();
                }
            }
        });
        
        label = new Label("S", skin);
        subTable.add(label);
        
        textField = new TextField("", skin, "color");
        textField.setName("saturation-field");
        textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[1] * 100)));
        textField.setDisabled(true);
        subTable.add(textField).width(100).spaceLeft(10);
        textField.addListener(core.iBeamListener);
        
        subTable.row();
        button = new Button(skin, "radio");
        buttonGroup.add(button);
        subTable.add(button).padBottom(2);
        button.addListener(core.handListener);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (((Button)actor).isChecked()) {
                    mode = Mode.BRIGHTNESS;
                    
                    slider.removeListener(sliderChangeListener);
                    slider.setValue(selectedColor.toHsv(new float[3])[2]);
                    slider.addListener(sliderChangeListener);
                    
                    updateBoxColor();
                    updateSliderColor();
                }
            }
        });
        
        label = new Label("B", skin);
        subTable.add(label);
        
        textField = new TextField("", skin, "color");
        textField.setName("brightness-field");
        textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[2] * 100)));
        textField.setDisabled(true);
        subTable.add(textField).width(100).spaceLeft(10);
        textField.addListener(core.iBeamListener);
        
        subTable.row();
        button = new Button(skin, "radio");
        buttonGroup.add(button);
        subTable.add(button).padBottom(2);
        button.addListener(core.handListener);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (((Button)actor).isChecked()) {
                    mode = Mode.RED;
                    
                    slider.removeListener(sliderChangeListener);
                    slider.setValue(selectedColor.r);
                    slider.addListener(sliderChangeListener);
                    
                    updateBoxColor();
                    updateSliderColor();
                }
            }
        });
        
        label = new Label("R", skin);
        subTable.add(label);
        
        textField = new TextField("", skin, "color");
        textField.setName("red-field");
        textField.setText(Integer.toString(MathUtils.floor(selectedColor.r * 255)));
        textField.setDisabled(true);
        subTable.add(textField).width(100).spaceLeft(10);
        textField.addListener(core.iBeamListener);
        
        subTable.row();
        button = new Button(skin, "radio");
        buttonGroup.add(button);
        subTable.add(button).padBottom(2);
        button.addListener(core.handListener);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (((Button)actor).isChecked()) {
                    mode = Mode.GREEN;
                    
                    slider.removeListener(sliderChangeListener);
                    slider.setValue(selectedColor.g);
                    slider.addListener(sliderChangeListener);
                    
                    updateBoxColor();
                    updateSliderColor();
                }
            }
        });
        
        label = new Label("G", skin);
        subTable.add(label);
        
        textField = new TextField("", skin, "color");
        textField.setName("green-field");
        textField.setText(Integer.toString(MathUtils.floor(selectedColor.g * 255)));
        textField.setDisabled(true);
        subTable.add(textField).width(100).spaceLeft(10);
        textField.addListener(core.iBeamListener);
        
        subTable.row();
        button = new Button(skin, "radio");
        buttonGroup.add(button);
        subTable.add(button).padBottom(2);
        button.addListener(core.handListener);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (((Button)actor).isChecked()) {
                    mode = Mode.BLUE;
                    
                    slider.removeListener(sliderChangeListener);
                    slider.setValue(selectedColor.b);
                    slider.addListener(sliderChangeListener);
                    
                    updateBoxColor();
                    updateSliderColor();
                }
            }
        });
        
        label = new Label("B", skin);
        subTable.add(label);
        
        textField = new TextField("", skin, "color");
        textField.setName("blue-field");
        textField.setText(Integer.toString(MathUtils.floor(selectedColor.b * 255)));
        textField.setDisabled(true);
        subTable.add(textField).width(100).spaceLeft(10);
        textField.addListener(core.iBeamListener);
        
        subTable.row().padTop(3);
        subTable.add();
        
        label = new Label("#", skin);
        subTable.add(label);
        
        textField = new TextField("", skin, "color");
        textField.setName("hex-field");
        textField.setText(selectedColor.toString().substring(0, 6));
        textField.setDisabled(true);
        subTable.add(textField).width(100).spaceLeft(10);
        textField.addListener(core.iBeamListener);
        
        button = findActor("hue-button");
        button.fire(new ChangeEvent());
    }
    
    private void updateColorRGB() {
        TextField textField = findActor("red-field");
        selectedColor.r = Integer.parseInt(textField.getText()) / 255f;
        
        textField = findActor("green-field");
        selectedColor.g = Integer.parseInt(textField.getText()) / 255f;
        
        textField = findActor("blue-field");
        selectedColor.b = Integer.parseInt(textField.getText()) / 255f;
        
        float[] hsb = selectedColor.toHsv(new float[3]);
        
        textField = findActor("hue-field");
        textField.setText(Integer.toString(MathUtils.floor(hsb[0])));
        
        textField = findActor("saturation-field");
        textField.setText(Integer.toString(MathUtils.floor(hsb[1] * 100)));
        
        textField = findActor("brightness-field");
        textField.setText(Integer.toString(MathUtils.floor(hsb[2] * 100)));
        
        textField = findActor("hex-field");
        textField.setText(selectedColor.toString().substring(0, 6));
        
        Image image = findActor("color-image");
        image.setColor(selectedColor);
    }
    
    private void updateColorHSB() {
        float[] hsb = new float[3];
        TextField textField = findActor("hue-field");
        hsb[0] = Integer.parseInt(textField.getText());
        
        textField = findActor("saturation-field");
        hsb[1] = Integer.parseInt(textField.getText()) / 100f;
        
        textField = findActor("brightness-field");
        hsb[2] = Integer.parseInt(textField.getText()) / 100f;
        
        selectedColor.fromHsv(hsb);
        
        textField = findActor("red-field");
        textField.setText(Integer.toString(MathUtils.floor(selectedColor.r * 255)));
        
        textField = findActor("green-field");
        textField.setText(Integer.toString(MathUtils.floor(selectedColor.g * 255)));
        
        textField = findActor("blue-field");
        textField.setText(Integer.toString(MathUtils.floor(selectedColor.b * 255)));
        
        textField = findActor("hex-field");
        textField.setText(selectedColor.toString().substring(0, 6));
        
        Image image = findActor("color-image");
        image.setColor(selectedColor);
    }
    
    private void updateBoxColor() {
        float[] hsb = selectedColor.toHsv(new float[3]);
        switch (mode) {
            case HUE:
                boxGradient1.getColor1().set(Color.WHITE);
                boxGradient1.getColor2().set(Color.WHITE);
                boxGradient1.getColor3().fromHsv(hsb[0], 1, 1).a = 1;
                boxGradient1.getColor4().fromHsv(hsb[0], 1, 1).a = 1;

                boxGradient2.getColor1().set(Color.BLACK);
                boxGradient2.getColor4().set(Color.BLACK);
                boxGradient2.getColor2().set(Color.CLEAR);
                boxGradient2.getColor3().set(Color.CLEAR);
                
                float s = Integer.parseInt(((TextField) findActor("saturation-field")).getText());
                float b = Integer.parseInt(((TextField) findActor("brightness-field")).getText());
                reticle.setPosition(256f * s / 100, 256f * b / 100, Align.center);
                break;
            case SATURATION:
                boxGradient1.getColor1().set(1,1,1, 1 - hsb[1]);
                boxGradient1.getColor2().set(1,1,1, 1 - hsb[1]);
                boxGradient1.getColor3().set(1,1,1, 1 - hsb[1]);
                boxGradient1.getColor4().set(1,1,1, 1 - hsb[1]);

                boxGradient2.getColor1().set(Color.BLACK);
                boxGradient2.getColor4().set(Color.BLACK);
                boxGradient2.getColor2().set(Color.CLEAR);
                boxGradient2.getColor3().set(Color.CLEAR);
                
                float h = Integer.parseInt(((TextField) findActor("hue-field")).getText());
                b = Integer.parseInt(((TextField) findActor("brightness-field")).getText());
                System.out.println(h + " " + b);
                reticle.setPosition(256f * h / 360, 256f * b / 100, Align.center);
                break;
            case BRIGHTNESS:
                boxGradient1.getColor1().set(Color.WHITE);
                boxGradient1.getColor4().set(Color.WHITE);
                boxGradient1.getColor2().set(Color.CLEAR);
                boxGradient1.getColor3().set(Color.CLEAR);

                boxGradient2.getColor1().set(0, 0, 0, 1 - hsb[2]);
                boxGradient2.getColor2().set(0, 0, 0, 1 - hsb[2]);
                boxGradient2.getColor3().set(0, 0, 0, 1 - hsb[2]);
                boxGradient2.getColor4().set(0, 0, 0, 1 - hsb[2]);
                
                h = Integer.parseInt(((TextField) findActor("hue-field")).getText());
                s = Integer.parseInt(((TextField) findActor("saturation-field")).getText());
                reticle.setPosition(256f * h / 360, 256f * s / 100, Align.center);
                break;
            case RED:
                boxGradient1.getColor1().set(selectedColor.r, 0, 0, 1);
                boxGradient1.getColor2().set(selectedColor.r, 1, 0, 1);
                boxGradient1.getColor3().set(selectedColor.r, 1, 1, 1);
                boxGradient1.getColor4().set(selectedColor.r, 0, 1, 1);

                boxGradient2.getColor1().set(Color.CLEAR);
                boxGradient2.getColor2().set(Color.CLEAR);
                boxGradient2.getColor3().set(Color.CLEAR);
                boxGradient2.getColor4().set(Color.CLEAR);
                
                reticle.setPosition(selectedColor.b * 256f, selectedColor.g * 256f, Align.center);
                break;
            case GREEN:
                boxGradient1.getColor1().set(0, selectedColor.g, 0, 1);
                boxGradient1.getColor2().set(1, selectedColor.g, 0, 1);
                boxGradient1.getColor3().set(1, selectedColor.g, 1, 1);
                boxGradient1.getColor4().set(0, selectedColor.g, 1, 1);

                boxGradient2.getColor1().set(Color.CLEAR);
                boxGradient2.getColor2().set(Color.CLEAR);
                boxGradient2.getColor3().set(Color.CLEAR);
                boxGradient2.getColor4().set(Color.CLEAR);
                
                reticle.setPosition(selectedColor.b * 256f, selectedColor.r * 256f, Align.center);
                break;
            case BLUE:
                boxGradient1.getColor1().set(0, 0, selectedColor.b, 1);
                boxGradient1.getColor2().set(0, 1, selectedColor.b, 1);
                boxGradient1.getColor3().set(1, 1, selectedColor.b, 1);
                boxGradient1.getColor4().set(1, 0, selectedColor.b, 1);

                boxGradient2.getColor1().set(Color.CLEAR);
                boxGradient2.getColor2().set(Color.CLEAR);
                boxGradient2.getColor3().set(Color.CLEAR);
                boxGradient2.getColor4().set(Color.CLEAR);
                
                reticle.setPosition(selectedColor.r * 256f, selectedColor.g * 256f, Align.center);
                break;
        }
    }
    
    private void boxColorSelection(float x, float y) {
        float w = MathUtils.clamp(x / 256f, 0, 1);
        float h = MathUtils.clamp(y / 256f, 0, 1);
        
        switch (mode) {
            case HUE:
                TextField textField = findActor("hue-field");
                selectedColor.fromHsv(Integer.parseInt(textField.getText()), w, h);
                
                textField = findActor("saturation-field");
                textField.setText(Integer.toString(MathUtils.floor(w * 100)));

                textField = findActor("brightness-field");
                textField.setText(Integer.toString(MathUtils.floor(h * 100)));

                textField = findActor("red-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.r * 255)));

                textField = findActor("green-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.g * 255)));

                textField = findActor("blue-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.b * 255)));

                textField = findActor("hex-field");
                textField.setText(selectedColor.toString().substring(0, 6));
                
                break;
            case SATURATION:
                textField = findActor("saturation-field");
                selectedColor.fromHsv(w * 360f, Integer.parseInt(textField.getText()) / 100f, h);

                textField = findActor("hue-field");
                textField.setText(Integer.toString(MathUtils.floor(w * 360f)));

                textField = findActor("brightness-field");
                textField.setText(Integer.toString(MathUtils.floor(h * 100)));

                textField = findActor("red-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.r * 255)));

                textField = findActor("green-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.g * 255)));

                textField = findActor("blue-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.b * 255)));

                textField = findActor("hex-field");
                textField.setText(selectedColor.toString().substring(0, 6));
                break;
            case BRIGHTNESS:
                textField = findActor("brightness-field");
                selectedColor.fromHsv(w * 360, h, Integer.parseInt(textField.getText()) / 100f);
                
                textField = findActor("hue-field");
                textField.setText(Integer.toString(MathUtils.floor(w * 360)));

                textField = findActor("saturation-field");
                textField.setText(Integer.toString(MathUtils.floor(h * 100)));

                textField = findActor("red-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.r * 255)));

                textField = findActor("green-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.g * 255)));

                textField = findActor("blue-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.b * 255)));

                textField = findActor("hex-field");
                textField.setText(selectedColor.toString().substring(0, 6));
                break;
            case RED:
                selectedColor.set(selectedColor.r, h, w, 1);
                
                textField = findActor("hue-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[0])));

                textField = findActor("saturation-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[1] * 100)));

                textField = findActor("brightness-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[2] * 100)));

                textField = findActor("green-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.g * 255)));

                textField = findActor("blue-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.b * 255)));

                textField = findActor("hex-field");
                textField.setText(selectedColor.toString().substring(0, 6));
                break;
            case GREEN:
                selectedColor.set(h, selectedColor.g, w, 1);

                textField = findActor("hue-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[0])));

                textField = findActor("saturation-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[1] * 100)));

                textField = findActor("brightness-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[2] * 100)));

                textField = findActor("red-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.r * 255)));

                textField = findActor("blue-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.b * 255)));

                textField = findActor("hex-field");
                textField.setText(selectedColor.toString().substring(0, 6));
                break;
            case BLUE:
                selectedColor.set(w, h, selectedColor.b, 1);

                textField = findActor("hue-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[0])));

                textField = findActor("saturation-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[1] * 100)));

                textField = findActor("brightness-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.toHsv(new float[3])[2] * 100)));

                textField = findActor("red-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.r * 255)));

                textField = findActor("green-field");
                textField.setText(Integer.toString(MathUtils.floor(selectedColor.g * 255)));

                textField = findActor("hex-field");
                textField.setText(selectedColor.toString().substring(0, 6));
                break;
        }
        
        Image image = findActor("color-image");
        image.setColor(selectedColor);
        
        updateSliderColor();
    }
    
    private void updateSliderColor() {
        switch (mode) {
            case HUE:
                ((GradientDrawable) sliderStyle.background).getColor1().set(Color.CLEAR);
                ((GradientDrawable) sliderStyle.background).getColor4().set(Color.CLEAR);
                ((GradientDrawable) sliderStyle.background).getColor2().set(Color.CLEAR);
                ((GradientDrawable) sliderStyle.background).getColor3().set(Color.CLEAR);
                break;
            case SATURATION:
                float[] hsb = selectedColor.toHsv(new float[3]);
                ((GradientDrawable) sliderStyle.background).getColor1().fromHsv(hsb[0], 0, hsb[2]).a = 1;
                ((GradientDrawable) sliderStyle.background).getColor4().fromHsv(hsb[0], 0, hsb[2]).a = 1;
                ((GradientDrawable) sliderStyle.background).getColor2().fromHsv(hsb[0], 1, hsb[2]).a = 1;
                ((GradientDrawable) sliderStyle.background).getColor3().fromHsv(hsb[0], 1, hsb[2]).a = 1;
                break;
            case BRIGHTNESS:
                hsb = selectedColor.toHsv(new float[3]);
                ((GradientDrawable) sliderStyle.background).getColor1().fromHsv(hsb[0], hsb[1], 0).a = 1;
                ((GradientDrawable) sliderStyle.background).getColor4().fromHsv(hsb[0], hsb[1], 0).a = 1;
                ((GradientDrawable) sliderStyle.background).getColor2().fromHsv(hsb[0], hsb[1], 1).a = 1;
                ((GradientDrawable) sliderStyle.background).getColor3().fromHsv(hsb[0], hsb[1], 1).a = 1;
                break;
            case RED:
                ((GradientDrawable) sliderStyle.background).getColor1().set(0, selectedColor.g, selectedColor.b, 1);
                ((GradientDrawable) sliderStyle.background).getColor4().set(0, selectedColor.g, selectedColor.b, 1);
                ((GradientDrawable) sliderStyle.background).getColor2().set(1, selectedColor.g, selectedColor.b, 1);
                ((GradientDrawable) sliderStyle.background).getColor3().set(1, selectedColor.g, selectedColor.b, 1);
                break;
            case GREEN:
                ((GradientDrawable) sliderStyle.background).getColor1().set(selectedColor.r, 0, selectedColor.b, 1);
                ((GradientDrawable) sliderStyle.background).getColor4().set(selectedColor.r, 0, selectedColor.b, 1);
                ((GradientDrawable) sliderStyle.background).getColor2().set(selectedColor.r, 1, selectedColor.b, 1);
                ((GradientDrawable) sliderStyle.background).getColor3().set(selectedColor.r, 1, selectedColor.b, 1);
                break;
            case BLUE:
                ((GradientDrawable) sliderStyle.background).getColor1().set(selectedColor.r, selectedColor.g, 0, 1);
                ((GradientDrawable) sliderStyle.background).getColor4().set(selectedColor.r, selectedColor.g, 0, 1);
                ((GradientDrawable) sliderStyle.background).getColor2().set(selectedColor.r, selectedColor.g, 1, 1);
                ((GradientDrawable) sliderStyle.background).getColor3().set(selectedColor.r, selectedColor.g, 1, 1);
                break;
        }
    }
    
    public static class ColorPickerEvent extends Event {
        private Color color;

        public ColorPickerEvent(Color color) {
            this.color = color;
        }
    }
    
    public static abstract class ColorPickerListener implements EventListener {
        @Override
        public boolean handle(Event event) {
            if (event instanceof ColorPickerEvent) {
                ColorPickerEvent colorPickerEvent = (ColorPickerEvent) event;
                if (colorPickerEvent.color == null) {
                    cancelled();
                } else {
                    colorSelected(colorPickerEvent.color);
                }
                return true;
            } else {
                return false;
            }
        }
        
        public abstract void colorSelected(Color color);
        public abstract void cancelled();
    }
}
