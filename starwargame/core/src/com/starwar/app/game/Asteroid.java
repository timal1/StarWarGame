package com.starwar.app.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.starwar.app.game.helpers.Poolable;
import com.starwar.app.screen.ScreenManager;
import com.starwar.app.screen.utils.Assets;

public class Asteroid implements Poolable {

    private final float BASE_SIZE = 256.0f;
    private final float BASE_RADIUS = BASE_SIZE / 2;

    private GameController gc;
    private TextureRegion texture;
    private boolean active;
    private Vector2 position;
    private Vector2 velocity;
    private int hpMax;
    private int hp;
    private float angle;
    private float rotationSpeed;
    private Circle hitArea;
    private float scale;

    public int getHpMax() {
        return hpMax;
    }

    public Circle getHitArea() {
        return hitArea;
    }

    public float getScale() {
        return scale;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public Asteroid(GameController gc) {
        this.gc = gc;
        this.position = new Vector2();
        this.velocity = new Vector2(0 , 0);
        this.hitArea = new Circle(0, 0, 0);
        this.active = false;
        this.texture = Assets.getInstance().getAtlas().findRegion("asteroid"); // берем картинку из game.pack

    }

    public void render (SpriteBatch batch) {
            batch.draw(texture, position.x - 128, position.y - 128, 128, 128,
                    256, 256, scale, scale, angle); // отрисовка в desktop
    }

    public void activate(float x, float y, float vx, float vy, float scale) {
        position.set(x, y);
        velocity.set(vx, vy);
        this.active = true;
        hpMax = (int) ((gc.getGameLevel() * 3 + 7) * scale);
        this.hp = hpMax;
        angle = MathUtils.random(0.0f, 360.0f);
        rotationSpeed = MathUtils.random(-180.0f, 180.0f);
        hitArea.setPosition(x, y);
        this.scale = scale;
        hitArea.setRadius(BASE_RADIUS * scale);

    }

    public void deActivate() {
        active = false;
    }

    public void update(float dt) {

        position.mulAdd(velocity, dt);
        angle += rotationSpeed * dt;

        if (position.x < 0) {
            position.x = ScreenManager.SPACE_WIDTH;
        }

        if (position.x > ScreenManager.SPACE_WIDTH) {
            position.x = 0;
        }

        if (position.y < 0) {
            position.y = ScreenManager.SPACE_HEIGHT;
        }

        if (position.y > ScreenManager.SPACE_HEIGHT) {
            position.y = 0;
        }
        hitArea.setPosition(position);
    }

    public boolean takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            deActivate();
            if (scale > 0.4f) {
                for (int i = 0; i < 3; i++) {
                    gc.getAsteroidController().setup(position.x, position.y,
                            MathUtils.random(-150, 150), MathUtils.random(-150, 150), scale - 0.3f);
                }
            } else {
                int x = MathUtils.random(1, 3);
                if (x == 3) {

                }
            }
            return true;
        } else {
            return false;
        }

    }
}
