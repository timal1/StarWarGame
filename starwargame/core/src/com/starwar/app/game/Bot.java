package com.starwar.app.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.starwar.app.game.helpers.Poolable;
import com.starwar.app.screen.ScreenManager;
import com.starwar.app.screen.utils.Assets;

public class Bot extends Ship implements Poolable {


    private boolean active;
    private Vector2 tmpaVector;
    private int gameLevel;


    public Bot(GameController gc) {
        super(gc, 0, 0);
        this.texture = Assets.getInstance().getAtlas().findRegion("ship");
        this.position = new Vector2();
        this.hitArea = new Circle(position, 28);
        this.active = false;
        this.tmpaVector = new Vector2(0, 0);
        this.gameLevel = 1;
        this.ownerType = OwnerType.BOT;
        this.weaponNum = MathUtils.random(gc.getGameLevel());
        this.currentWeapon = weapons[weaponNum];

    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x - 32, position.y - 32, 32, 32, 64,
                64, 1, 1, angle);

    }

    public void update(float dt) {

        super.update(dt);

        tmpaVector.set(gc.getHero().getPosition()).sub(position).nor();
        angle = tmpaVector.angleDeg();


        if (!isAlive()) {
            deActivate();
            gc.getParticleController().getEffectBuilder().buildMonsterSplash(
                    position.x, position.y);
        }

        if (gc.getHero().getPosition().dst(position) < ScreenManager.SCREEN_WIDTH / 3 + (50 * gameLevel)) {
            accelerate(dt);

            float bx = position.x + MathUtils.cosDeg(angle + 180) * 25;
            float by = position.y + MathUtils.sinDeg(angle + 180) * 25;

            // упраление огнем героя
            for (int i = 0; i < 3; i++) {
                gc.getParticleController().setup(bx + MathUtils.random(-4, 4), by + MathUtils.random(-4, 4),
                        velocity.x * -0.1f + MathUtils.random(-20, 20),
                        velocity.y * -0.1f + MathUtils.random(-20, 20),
                        0.4f,
                        1.2f, 0.2f,
                        1.0f, 0.5f, 0.0f, 1.0f,
                        1.0f, 1.0f, 1.0f, 0.0f);
            }
        }

        if (gc.getHero().getPosition().dst(position) < ScreenManager.SCREEN_WIDTH / 4 + (50 * gameLevel)) {
            tryToFire();
        }

        checkBorders();
    }

    public void activate(float x, float y) {
        position.set(x, y);
        angle = MathUtils.random(0.0f, 360.0f);
        velocity.set(MathUtils.cosDeg(angle) * MathUtils.random(100),
                MathUtils.sinDeg(angle) * MathUtils.random(100));
        active = true;
        hpMax = 50 * gc.getGameLevel();
        hp = hpMax;
        enginePower = 100 * gc.getGameLevel();

    }

    public void deActivate() {
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
