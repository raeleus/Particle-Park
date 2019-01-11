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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.ray3k.particlepark.Core;

/**
 *
 * @author Raymond
 */
public class TitleScreen implements Screen {
    private Viewport spineViewport;
    private Stage stage;
    private Skin skin;
    private Label label;
    private Core core;
    private Skeleton skeleton;
    private AnimationState animationState;
    private long parkSoundID;

    public TitleScreen(Core core) {
        spineViewport = new FitViewport(800, 800, new OrthographicCamera());
        
        this.core = core;
        stage = new Stage(new ScreenViewport(), core.batch);
        Gdx.input.setInputProcessor(stage);
        
        skin = core.internalAssetManager.get("Particle Park UI/Particle Park UI.json", Skin.class);
        
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        root.pad(20.0f);
        
        label = new Label("Click anywhere to begin...", skin);
        label.setColor(1, 1, 1, 0);
        root.add(label).expand().top().right();
        
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                animationState.setAnimation(0, "hide", false);
                Gdx.input.setInputProcessor(null);
                TitleScreen.this.core.internalAssetManager.get("sound/woosh.ogg", Sound.class).play();
                fadeOutParkSound();
            }
        });
        
        loadAnimation();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        core.batch.setProjectionMatrix(spineViewport.getCamera().combined);
        spineViewport.apply();
        core.batch.begin();
        core.batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        animationState.update(delta);
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
        core.skeletonRenderer.draw(core.batch, skeleton);
        core.batch.end();
        
        core.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        stage.getViewport().apply();
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        spineViewport.update(width, height, true);
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
    
    private void loadAnimation() {
        SkeletonData skeletonData = core.internalAssetManager.get("animations/main.json", SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                String name = event.getData().getName();
                if (name.equals("show-text")) {
                    label.addAction(Actions.fadeIn(.5f));
                } else if (name.equals("hide-text")) {
                    label.addAction(Actions.sequence(Actions.fadeOut(.5f), Actions.delay(.5f), new Action() {
                        @Override
                        public boolean act(float delta) {
                            core.setScreen(new MenuScreen(core));
                            return true;
                        }
                    }));
                } else if (name.equals("boing")) {
                    core.internalAssetManager.get("sound/boing.ogg", Sound.class).play();
                } else if (name.equals("park")) {
                    parkSoundID = core.internalAssetManager.get("sound/park.ogg", Sound.class).play(1.0f);
                } else if (name.equals("tap")) {
                    core.internalAssetManager.get("sound/tap.ogg", Sound.class).play();
                } else if (name.equals("woosh")) {
                    core.internalAssetManager.get("sound/woosh.ogg", Sound.class).play();
                }
            }
            
        });
        
        animationState.setAnimation(0, "reveal", false);
        animationState.apply(skeleton);
        skeleton.setPosition(400, 400);
        skeleton.updateWorldTransform();
    }
    
    private void fadeOutParkSound() {
        stage.addAction(Actions.sequence(new TemporalAction(1.0f) {
            @Override
            protected void update(float percent) {
                TitleScreen.this.core.internalAssetManager.get("sound/park.ogg", Sound.class).setVolume(parkSoundID, 1 - percent);
            }
        }, new Action() {
            @Override
            public boolean act(float delta) {
                TitleScreen.this.core.internalAssetManager.get("sound/park.ogg", Sound.class).stop();
                return true;
            }
        }));
    }
}
