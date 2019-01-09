package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;

public class Core extends Game {
    public TwoColorPolygonBatch batch;
    public AssetManager localAssetManager;
    public AssetManager internalAssetManager;
    
    @Override
    public void create() {
        batch = new TwoColorPolygonBatch();
        localAssetManager = new AssetManager(new LocalFileHandleResolver());
        internalAssetManager = new AssetManager(new InternalFileHandleResolver());
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
