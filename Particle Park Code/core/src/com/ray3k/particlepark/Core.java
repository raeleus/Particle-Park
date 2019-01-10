package com.ray3k.particlepark;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import com.ray3k.particlepark.SkeletonDataLoader.SkeletonDataLoaderParameter;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Core extends Game {
    public TwoColorPolygonBatch batch;
    public AssetManager localAssetManager;
    public AssetManager internalAssetManager;
    
    @Override
    public void create() {
        batch = new TwoColorPolygonBatch();
        
        localAssetManager = new AssetManager(new LocalFileHandleResolver());
        localAssetManager.setLoader(SkeletonData.class, new SkeletonDataLoader(localAssetManager.getFileHandleResolver()));
        
        internalAssetManager = new AssetManager(new InternalFileHandleResolver());
        internalAssetManager.setLoader(SkeletonData.class, new SkeletonDataLoader(internalAssetManager.getFileHandleResolver()));
        
        internalAssetManager.load("Particle Park UI/Particle Park UI.json", Skin.class);
        
        for (FileHandle atlasHandle : getInternalFiles("textures")) {
            SkeletonDataLoaderParameter parameter = new SkeletonDataLoaderParameter(atlasHandle.path());
            for (FileHandle fileHandle : getInternalFiles("animations")) {
                internalAssetManager.load(fileHandle.path(), SkeletonData.class, parameter);
            }
            break;
        }
        
        for (FileHandle fileHandle : getInternalFiles("music")) {
            internalAssetManager.load(fileHandle.path(), Music.class);
        }
        
        for (FileHandle fileHandle : getInternalFiles("sound")) {
            internalAssetManager.load(fileHandle.path(), Sound.class);
        }
        
        setScreen(new LoadScreen(this));
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

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        
    }
}
