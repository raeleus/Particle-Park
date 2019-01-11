package com.ray3k.particlepark;

import com.ray3k.particlepark.screens.LoadScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import com.ray3k.particlepark.SkeletonDataLoader.SkeletonDataLoaderParameter;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Core extends Game {
    public TwoColorPolygonBatch batch;
    public AssetManager localAssetManager;
    public AssetManager internalAssetManager;
    public PlayList<Music> playList;
    public SkeletonRenderer skeletonRenderer;
    
    @Override
    public void create() {
        batch = new TwoColorPolygonBatch();
        skeletonRenderer = new SkeletonRenderer();
        skeletonRenderer.setPremultipliedAlpha(true);
        
        addAssets();
        
        setScreen(new LoadScreen(this));
    }
    
    private void addAssets() {
        localAssetManager = new AssetManager(new LocalFileHandleResolver());
        localAssetManager.setLoader(SkeletonData.class, new SkeletonDataLoader(localAssetManager.getFileHandleResolver()));
        
        internalAssetManager = new AssetManager(new InternalFileHandleResolver());
        internalAssetManager.setLoader(SkeletonData.class, new SkeletonDataLoader(internalAssetManager.getFileHandleResolver()));
        
        internalAssetManager.load("Particle Park UI/Particle Park UI.json", Skin.class);
        
        for (FileHandle atlasHandle : getInternalFiles("textures")) {
            if (atlasHandle.extension().toLowerCase(Locale.ROOT).equals("atlas")) {
                SkeletonDataLoaderParameter parameter = new SkeletonDataLoaderParameter(atlasHandle.path());
                for (FileHandle fileHandle : getInternalFiles("animations")) {
                    internalAssetManager.load(fileHandle.path(), SkeletonData.class, parameter);
                }
                break;
            }
        }
        
        for (FileHandle fileHandle : getInternalFiles("music")) {
            internalAssetManager.load(fileHandle.path(), Music.class);
        }
        
        for (FileHandle fileHandle : getInternalFiles("sound")) {
            internalAssetManager.load(fileHandle.path(), Sound.class);
        }
    }
    
    private Array<FileHandle> getInternalFiles(String internalFolder) {
        final Array<FileHandle> assetFiles = new Array<FileHandle>();
        //list files if running from IDE
        assetFiles.addAll(Gdx.files.internal(internalFolder).list());
        
        //list files if running from JAR
        if (assetFiles.size == 0) {
            ZipInputStream zip = null;
            try {
                CodeSource src = getClass().getProtectionDomain().getCodeSource();
                URL jar = src.getLocation();
                zip = new ZipInputStream(jar.openStream());
                while (true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null) {
                        break;
                    }
                    final java.lang.String name = e.getName();
                    if (name.matches(internalFolder + "\\/.+")) {
                        com.badlogic.gdx.files.FileHandle fileHandle = Gdx.files.internal(name);
                        assetFiles.add(fileHandle);
                        //internal files don't report if it's a directory, remove parent directories
                        assetFiles.removeValue(fileHandle.parent(), false);
                    }
                }
            } catch (IOException ex) {
                Gdx.app.error(getClass().getName(), "Error reading assets from JAR", ex);
            } finally {
                try {
                    zip.close();
                } catch (IOException ex) {
                    Gdx.app.error(getClass().getName(), "Error reading assets from JAR", ex);
                }
            }
        }
        
        return assetFiles;
    }
    
    public void playSong() {
        if (playList == null) {
            playList = new PlayList<Music>();
            Array<Music> musics = internalAssetManager.getAll(Music.class, new Array<Music>());
            playList.addAll(musics.toArray());
        }
        
        if (playList.getIndex() == 0) {
            OnCompletionListener listener = new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    playList.next().play();
                }
            };
            for (Music music : playList.getAll()) {
                music.setOnCompletionListener(listener);
            }
            
            playList.shuffle();
            playList.next().play();
        }
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        
    }
}
