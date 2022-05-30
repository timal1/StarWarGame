package com.starwar.app.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.starwar.app.screen.ScreenManager;
import com.starwar.app.screen.utils.Assets;

public class WorldRender {
    private GameController gc;
    private SpriteBatch batch;
    private BitmapFont font32;
    private BitmapFont font72;
    private Camera camera;
    private StringBuilder sb;

    public WorldRender(GameController gc, SpriteBatch batch) {
        this.gc = gc;
        this.batch = batch;
        this.font32 = Assets.getInstance().getAssetManager().get("fonts/font32.ttf", BitmapFont.class);
        this.font72 = Assets.getInstance().getAssetManager().get("fonts/font72.ttf", BitmapFont.class);
        this.camera = ScreenManager.getInstance().getCamera();
        this.sb = new StringBuilder();
    }

    public void render() {
        ScreenUtils.clear(0, 0, 0.5f, 1);
        batch.begin();
        gc.getBackground().render(batch);
        gc.getAsteroidController().render(batch);
        gc.getBulletController().render(batch);
        gc.getParticleController().render(batch);
        gc.getPowerUpsController().render(batch);
        gc.getInfoController().render(batch, font32);
        gc.getHero().render(batch);
        gc.getHero().renderGUI(batch, font32, font72);

        gc.getBotController().render(batch);

        camera.position.set(gc.getHero().getPosition().x, gc.getHero().getPosition().y, 0.0f);
        camera.update();
        ScreenManager.getInstance().getViewport().apply();
        batch.setProjectionMatrix(camera.combined);
        batch.end();
        gc.getStage().draw();
    }
}
