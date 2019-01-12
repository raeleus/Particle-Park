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
package com.ray3k.particlepark.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.particlepark.Core;

/**
 *
 * @author Raymond
 */
public class MenuScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private Core core;

    public MenuScreen(Core core) {
        this.core = core;
        
        core.playSong();
        
        stage = new Stage(new ScreenViewport(), core.batch);
        Gdx.input.setInputProcessor(stage);
        stage.getRoot().setColor(1, 1, 1, 0);
        stage.getRoot().addAction(Actions.fadeIn(.5f));
        
        skin = core.internalAssetManager.get("Particle Park UI/Particle Park UI.json", Skin.class);
        
        createMenu();
    }
    
    private void createMenu() {
        Image image = new Image(skin, "bg");
        image.setFillParent(true);
        stage.addActor(image);
        
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        root.pad(5).padTop(10);
        
        Label label = new Label("Select a Scene", skin, "window");
        root.add(label);
        
        HorizontalGroup horizontalGroup = new HorizontalGroup();
        horizontalGroup.wrap();
        horizontalGroup.center();
        
        root.row();
        ScrollPane scrollPane = new ScrollPane(horizontalGroup, skin, "bg");
        scrollPane.setFadeScrollBars(false);
        root.add(scrollPane).grow().padTop(5);
        stage.setScrollFocus(scrollPane);
        
        horizontalGroup.addActor(createSceneButton("animations/city.json", "thumb-city"));
        horizontalGroup.addActor(createSceneButton("animations/welder.json", "thumb-welder"));
        horizontalGroup.addActor(createSceneButton("animations/camp-fire.json", "thumb-camp-fire"));
        horizontalGroup.addActor(createSceneButton("animations/tub.json", "thumb-tub"));
        horizontalGroup.addActor(createSceneButton("animations/race-car.json", "thumb-race-car"));
        horizontalGroup.addActor(createSceneButton("animations/space-ship.json", "thumb-space-ship"));
        horizontalGroup.addActor(createSceneButton("animations/poop.json", "thumb-poop"));
        horizontalGroup.addActor(createSceneButton("animations/window.json", "thumb-window"));
        horizontalGroup.addActor(createSceneButton("animations/gun.json", "thumb-gun"));
        horizontalGroup.addActor(createSceneButton("animations/train.json", "thumb-train"));
        horizontalGroup.addActor(createSceneButton("animations/brick.json", "thumb-brick"));
        horizontalGroup.addActor(createSceneButton("animations/cloud.json", "thumb-cloud"));
        horizontalGroup.addActor(createSceneButton("animations/fighter.json", "thumb-fighter"));
        
        root.row();
        label = new Label("Copyright 2019 Raymond Buckley", skin);
        root.add(label).left().padTop(5);
        
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        ImageButton imageButton = new ImageButton(skin, "bgm");
        imageButton.setChecked(true);
        table.add(imageButton).expand().bottom().right();
        imageButton.addListener(core.handListener);
        
        imageButton = new ImageButton(skin, "sfx");
        imageButton.setChecked(true);
        table.add(imageButton).bottom();
        imageButton.addListener(core.handListener);
    }
    
    private Button createSceneButton(final String animationPath, String thumbnailName) {
        Button button = new Button(skin);
        button.addListener(core.handListener);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                core.setScreen(new DemoScreen(core, animationPath));
            }
        });
        
        Image image = new Image(skin, thumbnailName);
        button.add(image);
        
        return button;
    }
    
    @Override
    public void show() {
        Array<Music> musics = core.internalAssetManager.getAll(Music.class, new Array<Music>());
        for (final Music music : musics) {
            stage.addAction(new TemporalAction(1.0f) {
                private float startVolume;
                private float targetVolume;
                
                {
                    startVolume = music.getVolume();
                    targetVolume = .5f;
                }
                
                @Override
                protected void update(float percent) {
                    music.setVolume((1 - percent) * (startVolume - targetVolume) + targetVolume);
                }
            });
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
