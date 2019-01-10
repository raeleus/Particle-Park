package com.ray3k.particlepark;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;

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
        
        setScreen(new LoadScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        
    }
}
