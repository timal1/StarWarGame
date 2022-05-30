package com.starwar.app.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.starwar.app.game.GameController;
import com.starwar.app.game.WorldRender;
import com.starwar.app.screen.utils.Assets;

public class GameScreen extends AbstractScreen{

    private GameController gc;
    private WorldRender worldRender;

    public GameScreen(SpriteBatch batch) {
        super(batch);
    }

    @Override
    public void show() {
        Assets.getInstance().loadAssets(ScreenManager.ScreenType.GAME);
        this.gc = new GameController(batch);
        this.worldRender = new WorldRender(gc, batch);
    }

    @Override
    public void render(float delta) {
        gc.update(delta);
        worldRender.render();
    }

    @Override
    public void dispose() {
        gc.dispose();
    }
}
