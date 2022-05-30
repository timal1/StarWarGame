package com.starwar.app.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.starwar.app.game.helpers.ObjectPool;

public class BotController extends ObjectPool<Bot> {

    private GameController gc;

    public BotController(GameController gc) {
        this.gc = gc;
    }

    @Override
    protected Bot newObject() {
        return new Bot(gc);
    }

    public void render (SpriteBatch batch) {
        for (int i = 0; i < activeList.size(); i++) {
            Bot bot = activeList.get(i);
            bot.render(batch);
        }
    }

    public void setup(float x, float y) {
        getActiveElement().activate(x, y);
    }

    public void update(float dt) {
        for (int i = 0; i < activeList.size(); i++) {
            activeList.get(i).update(dt);
        }
        checkPool();
    }
}
