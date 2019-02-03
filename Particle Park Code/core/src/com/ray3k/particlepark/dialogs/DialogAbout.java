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
package com.ray3k.particlepark.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.ray3k.particlepark.Core;

/**
 *
 * @author Raymond
 */
public class DialogAbout extends Dialog {
    private Core core;
    private Skin skin;

    public DialogAbout(Core core, Skin skin) {
        super("About", skin);
        
        this.core = core;
        this.skin = skin;
        
        getTitleLabel().setAlignment(Align.center);
        
        refresh();
    }
    
    private void refresh() {
        Table root = getContentTable();
        root.pad(10);
        root.clear();
        
        Label label = new Label("Particle Park is developed by Raeleus for the LibGDX community.\nVersion " + core.version + "\nCopyright Raymond \"Raeleus\" Buckley 2019", skin);
        label.setAlignment(Align.center);
        root.add(label);
        
        root.row();
        TextButton textButton = new TextButton("github.com/raeleus/Particle-Park", skin, "link");
        root.add(textButton);
        textButton.addListener(core.handListener);
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Gdx.net.openURI("https://github.com/raeleus/Particle-Park");
            }
        });
        
        root.row();
        textButton = new TextButton("Thanks!", skin);
        root.add(textButton);
        textButton.addListener(core.handListener);
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                hide();
            }
        });
    }
}
