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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.Particle;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.EventData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.PointAttachment;
import com.ray3k.particlepark.Core;
import com.ray3k.particlepark.Tuple;
import java.util.Iterator;
import java.util.Locale;

/**
 *
 * @author Raymond
 */
public class DemoScreen implements Screen {
    private Viewport spineViewport;
    private Stage stage;
    private Skin skin;
    private Core core;
    private Skeleton skeleton;
    private AnimationState animationState;
    private String animationPath;
    private Dialog dialog;
    private ImageButton menuButton;
    private ObjectMap<Sound, Array<Long>> soundMap;
    private Array<EventData> particleEvents;
    private Array<Tuple<String, FileHandle>> particleFiles;
    private ObjectMap<EventData, Tuple<String, FileHandle>> eventParticleMap;
    private PixmapPacker pixmapPacker;
    private TextureAtlas particleAtlas;
    private Array<ParticleEffect> particleEffectsBack;
    private Array<ParticleEffect> particleEffectsFront;
    private ObjectMap<ParticleEffect, Slot> particleSlotFollowMap;
    
    private final Vector2 position;

    public DemoScreen(Core core, String animationPath) {
        this.core = core;
        this.animationPath = animationPath;
        
        position = new Vector2();
        
        particleEvents = new Array<EventData>();
        particleFiles = new Array<Tuple<String, FileHandle>>();
        eventParticleMap = new ObjectMap<EventData, Tuple<String, FileHandle>>();
        particleSlotFollowMap = new ObjectMap<ParticleEffect, Slot>();
        
        pixmapPacker = new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 3, true);
        particleAtlas = new TextureAtlas();
        particleEffectsBack = new Array<ParticleEffect>();
        particleEffectsFront = new Array<ParticleEffect>();
        
        spineViewport = new FitViewport(800, 800, new OrthographicCamera());
        
        stage = new Stage(new ScreenViewport(), core.batch);
        Gdx.input.setInputProcessor(stage);
        
        stage.getRoot().setColor(1, 1, 1, 0);
        stage.getRoot().addAction(Actions.fadeIn(.5f));
        
        skin = core.internalAssetManager.get("Particle Park UI/Particle Park UI.json", Skin.class);
        
        soundMap = new ObjectMap<Sound, Array<Long>>();
        
        loadAnimation();
        getParticleFiles();
        initializeParticles();
        createMenu();
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
                    targetVolume = .2f;
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
        
        core.batch.setProjectionMatrix(spineViewport.getCamera().combined);
        spineViewport.apply();
        core.batch.begin();
        
        core.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Iterator<ParticleEffect> iter = particleEffectsBack.iterator();
        while (iter.hasNext()) {
            ParticleEffect particleEffect = iter.next();
            if (particleEffect.isComplete()) {
                iter.remove();
                particleSlotFollowMap.remove(particleEffect);
            } else {
                Slot slot = particleSlotFollowMap.get(particleEffect);
                if (slot != null) {
                    PointAttachment point = (PointAttachment) slot.getAttachment();
                    point.computeWorldPosition(slot.getBone(), position);
                    particleEffect.setPosition(position.x, position.y);
                }
                particleEffect.draw(core.batch, delta);
            }
        }
        core.batch.flush();
        
        core.batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        animationState.update(delta);
        animationState.apply(skeleton);
        skeleton.updateWorldTransform();
        core.skeletonRenderer.draw(core.batch, skeleton);
        core.batch.flush();
        
        core.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        iter = particleEffectsFront.iterator();
        while (iter.hasNext()) {
            ParticleEffect particleEffect = iter.next();
            if (particleEffect.isComplete()) {
                iter.remove();
                particleSlotFollowMap.remove(particleEffect);
            } else {
                Slot slot = particleSlotFollowMap.get(particleEffect);
                if (slot != null) {
                    PointAttachment point = (PointAttachment) slot.getAttachment();
                    point.computeWorldPosition(slot.getBone(), position);
                    particleEffect.setPosition(position.x, position.y);
                }
                particleEffect.draw(core.batch, delta);
            }
        }
        core.batch.end();
        
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
        Array<Music> musics = core.internalAssetManager.getAll(Music.class, new Array<Music>());
        for (Music music : musics) {
            music.setVolume(.5f);
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        particleAtlas.dispose();
    }
    
    private void loadAnimation() {
        SkeletonData skeletonData = core.internalAssetManager.get(animationPath, SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        
        for (EventData eventData : skeleton.getData().getEvents()) {
            if (eventData.getAudioPath() == null) {
                particleEvents.add(eventData);
            }
        }
        
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void complete(AnimationState.TrackEntry entry) {
                Array<Sound> sounds = core.internalAssetManager.getAll(Sound.class, new Array<Sound>());
                for (Sound sound : sounds) {
                    sound.stop();
                }
                
                for (ParticleEffect particleEffect : particleEffectsBack) {
                    particleEffect.allowCompletion();
                }
                
                for (ParticleEffect particleEffect : particleEffectsFront) {
                    particleEffect.allowCompletion();
                }
                
                if (entry.getAnimation().getName().equals("hide")) {
                    core.setScreen(new MenuScreen(core));
                }
            }
            
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                String audioPath = event.getData().getAudioPath();
                if (audioPath != null) {
                    Sound sound = core.internalAssetManager.get("sound/" + audioPath, Sound.class);
                    long id = -1;
                    if (event.getString() == null || event.getString().equals("")) {
                        if (core.preferences.getBoolean("sfx", true)) {
                            id = sound.play();
                        } else {
                            id = sound.play(0);
                        }
                    } else if (event.getString().equals("loop")) {
                        if (core.preferences.getBoolean("sfx", true)) {
                            id = sound.loop();
                        } else {
                            id = sound.loop(0);
                        }
                    }
                    
                    if (id != -1) {
                        if (!soundMap.containsKey(sound)) {
                            soundMap.put(sound, new Array<Long>());
                        }
                        soundMap.get(sound).add(id);
                    }
                } else {
                    boolean create = true;
                    boolean continuous = false;
                    boolean back = false;
                    Slot slot = null;
                    int index = 0;
                    for (String string : event.getString().split(";")) {
                        switch (index) {
                            case 0:
                                slot = skeleton.findSlot(string);
                                PointAttachment point = (PointAttachment) slot.getAttachment();
                                point.computeWorldPosition(slot.getBone(), position);
                                break;
                            case 1:
                                if (string.equals("start")) {
                                    continuous = true;
                                } else if (string.equals("stop")) {
                                    create = false;
                                    
                                    ParticleEffect particleEffect = particleSlotFollowMap.findKey(slot, false);
                                    if (particleEffect != null) {
                                        particleEffect.allowCompletion();
                                        particleSlotFollowMap.remove(particleEffect);
                                    }
                                }
                                break;
                            case 2:
                                if (string.equals("back")) {
                                    back = true;
                                }
                                break;
                        }
                        index++;
                    }

                    if (create) {
                        ParticleEffect particleEffect = new ParticleEffect();
                        particleEffect.load(eventParticleMap.get(event.getData()).y, particleAtlas);
                        particleEffect.start();
                        
                        particleEffect.setPosition(position.x, position.y);
                        if (back) {
                            particleEffectsBack.add(particleEffect);
                        } else {
                            particleEffectsFront.add(particleEffect);
                        }
                        
                        if (continuous) {
                            particleSlotFollowMap.put(particleEffect, slot);
                        }
                    }
                    
                }
            }
            
        });
        
        animationState.setAnimation(0, "show", false);
        animationState.addAnimation(0, "animation", true, 2);
        animationState.apply(skeleton);
        skeleton.setPosition(400, 400);
        skeleton.updateWorldTransform();
    }
    
    private void getParticleFiles() {
        
        FileHandle sceneFolder = Gdx.files.local(animationPath);
        sceneFolder = Gdx.files.local("Particle Park_data/" + sceneFolder.nameWithoutExtension());
        
        for (FileHandle particleFolder : sceneFolder.list()) {
            for (FileHandle fileHandle : particleFolder.list()) {
                String extension = fileHandle.extension().toLowerCase(Locale.ROOT);
                
                if (extension.equals("p")) {
                    particleFiles.add(new Tuple<String, FileHandle>(fileHandle.nameWithoutExtension(), fileHandle));
                    break;
                }
            }
        }
    }
    
    private void createMenu() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        menuButton = new ImageButton(skin, "menu");
        root.add(menuButton).expand().top().left();
        menuButton.addListener(core.handListener);
        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                menuButton.setTouchable(Touchable.disabled);
                menuButton.addAction(Actions.fadeOut(.25f));
                createWindow(menuButton.localToStageCoordinates(new Vector2(10, 0)));
            }
        });
        
        final ImageButton bgmButton = new ImageButton(skin, "bgm");
        bgmButton.setChecked(core.preferences.getBoolean("bgm", true));
        root.add(bgmButton).expand().bottom().right();
        bgmButton.addListener(core.handListener);
        bgmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                core.preferences.putBoolean("bgm", bgmButton.isChecked());
                core.preferences.flush();
                
                if (bgmButton.isChecked()) {
                    core.playSong();
                } else {
                    core.stopSong();
                }
            }
        });
        
        final ImageButton sfxButton = new ImageButton(skin, "sfx");
        sfxButton.setChecked(core.preferences.getBoolean("sfx", true));
        root.add(sfxButton).bottom();
        sfxButton.addListener(core.handListener);
        sfxButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                core.preferences.putBoolean("sfx", sfxButton.isChecked());
                core.preferences.flush();
                
                Array<Sound> sounds = core.internalAssetManager.getAll(Sound.class, new Array<Sound>());
                if (sfxButton.isChecked()) {
                    for (Sound sound : sounds) {
                        if (soundMap.get(sound) != null) for (long id : soundMap.get(sound)) {
                            sound.setVolume(id, 1);
                        }
                    }
                } else {
                    for (Sound sound : sounds) {
                        if (soundMap.get(sound) != null) for (long id : soundMap.get(sound)) {
                            sound.setVolume(id, 0);
                        }
                    }
                }
            }
        });
    }
    
    private void createWindow(Vector2 position) {
        if (dialog == null) {
            dialog = new Dialog("Settings", skin);
            dialog.getTitleTable().padLeft(10).padRight(10);
            dialog.setModal(false);
            
            dialog.getTitleTable().defaults().space(20);
            Button button = new Button(skin, "close");
            dialog.getTitleTable().add(button).expandX().right();
            button.addListener(core.handListener);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    dialog.setKeepWithinStage(false);
                    dialog.hide(Actions.moveTo(-dialog.getWidth(), dialog.getY(), .5f, Interpolation.slowFast));
                    dialog = null;
                    
                    menuButton.setTouchable(Touchable.enabled);
                    menuButton.addAction(Actions.fadeIn(.25f));
                }
            });
            
            final Table root = new Table();
            final ScrollPane scrollPane = new ScrollPane(root, skin);
            scrollPane.setFlickScroll(false);
            dialog.getContentTable().add(scrollPane).grow();
            root.setWidth(200);
            
            if (particleFiles.size > 0) for (final EventData particleEvent : particleEvents) {
                root.row();
                Label label = new Label(particleEvent.getName(), skin);
                root.add(label).spaceTop(13);
                
                root.row();
                Table table = new Table();
                root.add(table);
                
                final SelectBox<Tuple<String, FileHandle>> selectBox = new SelectBox<Tuple<String, FileHandle>>(skin);
                selectBox.setItems(particleFiles);
                selectBox.setSelected(eventParticleMap.get(particleEvent));
                table.add(selectBox);
                selectBox.addListener(core.handListener);
                selectBox.getList().addListener(core.handListener);
                selectBox.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                        loadParticleEffect(particleEvent, selectBox.getSelected());
                        prepareParticleAtlas();
                    }
                });
                
                ImageButton imageButton = new ImageButton(skin, "download");
                table.add(imageButton);
                imageButton.addListener(core.handListener);
            }
            
            dialog.getContentTable().row();
            TextButton textButton = new TextButton("Quit to Menu", skin);
            dialog.getContentTable().add(textButton).growX();
            textButton.addListener(core.handListener);
            textButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    Gdx.input.setInputProcessor(null);
                    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
                    
                    animationState.getCurrent(0).setTimeScale(0);
                    animationState.setAnimation(1, "hide", false);
                    
                    for (final ParticleEffect particleEffect : particleEffectsBack) {
                        particleEffect.allowCompletion();
                    }

                    for (final ParticleEffect particleEffect : particleEffectsFront) {
                        particleEffect.allowCompletion();
                    }
                    
                    stage.getRoot().addAction(Actions.fadeOut(.5f));
                }
            });
            
            dialog.show(stage, null);
            dialog.setKeepWithinStage(false);
            dialog.setHeight(Math.min(position.y - 10, dialog.getHeight()));
            dialog.setPosition(MathUtils.round(-dialog.getWidth()), MathUtils.round(position.y - dialog.getHeight()));
            stage.setScrollFocus(scrollPane);
            
            dialog.addAction(Actions.sequence(Actions.moveTo(position.x, dialog.getY(), .5f, Interpolation.fastSlow), new Action() {
                @Override
                public boolean act(float delta) {
                    dialog.setKeepWithinStage(true);
                    return true;
                }
            }));
        }
    }
    
    private void initializeParticles() {
        for (EventData particleEvent : particleEvents) {
            if (particleFiles.size > 0) {
                loadParticleEffect(particleEvent, particleFiles.first());
            }
        }
        
        prepareParticleAtlas();
    }
    
    private void loadParticleEffect(final EventData particleEvent, final Tuple<String, FileHandle> selected) {
        for (FileHandle fileHandle : selected.y.parent().list()) {
            if (fileHandle.extension().toLowerCase(Locale.ROOT).equals("png")) {
                if (pixmapPacker.getRect(fileHandle.nameWithoutExtension()) == null) {
                    Pixmap pixmap = new Pixmap(fileHandle);
                    pixmapPacker.pack(fileHandle.nameWithoutExtension(), pixmap);
                }
            }
        }
        
        eventParticleMap.put(particleEvent, selected);
    }
    
    private void prepareParticleAtlas() {
        pixmapPacker.updateTextureAtlas(particleAtlas, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false, false);
        
    }
}
