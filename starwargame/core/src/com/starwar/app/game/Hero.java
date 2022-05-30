package com.starwar.app.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.starwar.app.screen.ScreenManager;
import com.starwar.app.screen.utils.Assets;

public class Hero extends Ship {

    public enum Skill {
        HP_MAX(20), HP(20), WEAPON(100), MAGNETIC(50);

        int cost;

        Skill(int cost) {
            this.cost = cost;
        }
    }

    private int score;
    private int scoreView;
    private int money;
    private StringBuilder sb;
    private Shop shop;
    private Vector2 tmpVector;
    private Circle magneticField;
    private int powerForceField;
    protected Circle hitAreaForceField;

    public Circle getMagneticField() {
        return magneticField;
    }

    public Shop getShop() {
        return shop;
    }

    public int getScore() {
        return score;
    }

    public int getMoney() {
        return money;
    }

    public boolean isMoneyEnough(int amount) {
        return money >= amount;
    }

    public void decreaseMoney(int amount) {
        money -= amount;
    }

    public void setPause(boolean pause) {
        gc.setPause(pause);
    }

    public int getPowerForceField() {
        return powerForceField;
    }

    public Circle getHitAreaForceField() {
        return hitAreaForceField;
    }

    public Hero(GameController gc) {
        super(gc, 700, 100);
        this.texture = Assets.getInstance().getAtlas().findRegion("ship");
        this.starTexture = Assets.getInstance().getAtlas().findRegion("star16");
        this.position = new Vector2(640, 360);
        this.hitArea = new Circle(position, 28);
        this.tmpVector = new Vector2(0, 0);
        this.money = 100;
        this.powerForceField = 0;
        this.hitAreaForceField = new Circle(position, 70);
        this.sb = new StringBuilder();
        this.magneticField = new Circle(position, 100);
        this.shop = new Shop(this);
        this.ownerType = OwnerType.PLAYER;
    }

    public void addScore(int amount) {
        score += amount;
    }

    public void consume(PowerUp powerUp) {
        sb.setLength(0);
        switch (powerUp.getType()) {
            case MEDKIT:
                int oldHp = hp;
                hp += powerUp.getPower();
                if (hp > hpMax) {
                    hp = hpMax;
                }
                sb.append("HP + ").append(hp - oldHp);
                gc.getInfoController().setup(powerUp.getPosition().x, powerUp.getPosition().y, sb, Color.GREEN);
                break;
            case AMMOS:
                int count = currentWeapon.addAmmos(powerUp.getPower());
                sb.append("AMMOS + ").append(count);
                gc.getInfoController().setup(powerUp.getPosition().x, powerUp.getPosition().y, sb, Color.RED);
                break;
            case MONEY:
                money += powerUp.getPower();
                sb.append("MONEY + ").append(powerUp.getPower());
                gc.getInfoController().setup(position.x, position.y, sb, Color.YELLOW);
                break;
            case FORCE_FIELD:
                powerForceField += powerUp.getPower();
                sb.append("FORCE_FIELD + ").append(powerUp.getPower());
                gc.getInfoController().setup(position.x, position.y, sb, Color.BLUE);
                break;
        }

    }

    public void  createForceField(SpriteBatch batch) {
        batch.setColor(Color.BLUE);
        for (int i = 0; i < 120; i++) {
            batch.draw(starTexture, position.x + 70.0f * MathUtils.cosDeg(360.0f / 120.0f * i) - 8, position.y + 70.0f * MathUtils.sinDeg(360.0f / 120.0f * i) - 8);
        }
        batch.setColor(Color.WHITE);
    }

    public boolean upgrade(Skill skill) {
        switch (skill) {
            case HP_MAX:
                hpMax += 10;
                return true;
            case HP:
                if (hp < hpMax) {
                    hp += 10;
                    if (hp > hpMax) {
                        hp = hpMax;
                    }
                    return true;
                }
                return false;
            case WEAPON:
                if (weaponNum < weapons.length - 1) {
                    weaponNum++;
                    currentWeapon = weapons[weaponNum];
                    return true;
                }
                return false;
            case MAGNETIC:
                if (magneticField.radius < 200) {
                    magneticField.radius += 10;
                    return true;
                }
                return false;
        }
        return false;
    }

    public void renderGUI(SpriteBatch batch, BitmapFont fontList, BitmapFont fontLevel) {
        sb.setLength(0);
        sb.append("SCORE: ").append(scoreView).append("\n");
        sb.append("HP: ").append(hp).append("/").append(hpMax).append("\n");
        sb.append("BULLETS: ").append(currentWeapon.getCurBullet()).append("/").append(currentWeapon.getMaxBullet()).append("\n");
        sb.append("MONEY: ").append(money).append("\n");
        sb.append("MAGNETIC: ").append((int) magneticField.radius).append("\n");
        sb.append("TIMER: ").append((int)(gc.getTimer())).append("\n");
        sb.append("FORCE_FIELD: ").append((powerForceField)).append("\n");

        fontList.draw(batch, sb, position.x - ScreenManager.HALF_SCREEN_WIDTH + 20,
                position.y + ScreenManager.HALF_SCREEN_HEIGHT - 20);

        if (gc.getTimer() < 3) {
            sb.setLength(0);
            sb.append("level ").append(gc.getGameLevel());
            fontLevel.draw(batch, sb, position.x - 100, position.y + 100);
        }

        //миникарта
        float mapX =  position.x + 480;
        float mapY =  position.y + 200;
        batch.setColor(Color.GREEN);
        batch.draw(starTexture, mapX - 24, mapY - 24, 48, 48);
        batch.setColor(Color.RED);
        for (int i = 0; i < gc.getAsteroidController().getActiveList().size(); i++) {
            Asteroid a = gc.getAsteroidController().getActiveList().get(i);
            float dst = position.dst(a.getPosition());
            if (dst < 2000.0f) {
                tmpVector.set(a.getPosition()).sub(this.position);
                tmpVector.scl(160.0f / 2000.0f);
                batch.draw(starTexture, mapX + tmpVector.x - 16, mapY + tmpVector.y - 16, 32, 32);
            }
        }

        batch.setColor(Color.BLUE);
        for (int i = 0; i < gc.getBotController().getActiveList().size(); i++) {
            Bot b = gc.getBotController().getActiveList().get(i);
            float dst = position.dst(b.getPosition());
            if (dst < 2000.0f) {
                tmpVector.set(b.getPosition()).sub(this.position);
                tmpVector.scl(160.0f / 2000.0f);
                batch.draw(starTexture, mapX + tmpVector.x - 16, mapY + tmpVector.y - 16, 32, 32);
            }
        }

        batch.setColor(Color.WHITE);
        for (int i = 0; i < 120; i++) {
            batch.draw(starTexture, mapX + 160.0f * MathUtils.cosDeg(360.0f / 120.0f * i) - 8, mapY + 160.0f * MathUtils.sinDeg(360.0f / 120.0f * i) - 8);
        }

        shop.setPosition(position.x - ScreenManager.HALF_SCREEN_WIDTH + 20,
                position.y - ScreenManager.HALF_SCREEN_HEIGHT + 20);

        if (powerForceField > 0) {
            createForceField(batch);
        }


    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x - 32, position.y - 32, 32, 32, 64,
                64, 1, 1, angle);

    }

    public void update(float dt) {
        super.update(dt);
        updateScore(dt);


        float stopKoef = 1.0f - dt;
        if (stopKoef < 0.0f) {
            stopKoef = 0.0f;
        }
        velocity.scl(stopKoef);

        if (Gdx.input.isKeyPressed(Input.Keys.B)) {
            shop.setVisible(true);
            setPause(true);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            tryToFire();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            angle += 180 * dt;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            angle -= 180 * dt;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            accelerate(dt);

            float bx = position.x + MathUtils.cosDeg(angle + 180) * 25;
            float by = position.y + MathUtils.sinDeg(angle + 180) * 25;

            // упраление огнем героя
            for (int i = 0; i < 3; i++) {
                gc.getParticleController().setup(bx + MathUtils.random(-4, 4), by + MathUtils.random(-4, 4),
                        velocity.x * -0.1f + MathUtils.random(-20, 20), velocity.y * -0.1f + MathUtils.random(-20, 20),
                        0.4f,
                        1.2f, 0.2f,
                        1.0f, 0.5f, 0.0f, 1.0f,
                        1.0f, 1.0f, 1.0f, 0.0f);
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {

            // движение задним ходом
            moveBack(dt);

            float bx = position.x + MathUtils.cosDeg(angle - 90) * 25;
            float by = position.y + MathUtils.sinDeg(angle - 90) * 25;

            // упраление огнем героя при заднем ходе
            for (int i = 0; i < 3; i++) {
                gc.getParticleController().setup(bx + MathUtils.random(-4, 4), by + MathUtils.random(-4, 4),
                        velocity.x * 0.1f + MathUtils.random(-20, 20), velocity.y * 0.1f + MathUtils.random(-20, 20),
                        0.2f,
                        1.0f, 0.2f,
                        1.0f, 0.5f, 0.0f, 1.0f,
                        1.0f, 1.0f, 1.0f, 0.0f);
            }
            bx = position.x + MathUtils.cosDeg(angle + 90) * 25;
            by = position.y + MathUtils.sinDeg(angle + 90) * 25;

            for (int i = 0; i < 3; i++) {
                gc.getParticleController().setup(bx + MathUtils.random(-4, 4), by + MathUtils.random(-4, 4),
                        velocity.x * 0.1f + MathUtils.random(-20, 20), velocity.y * 0.1f + MathUtils.random(-20, 20),
                        0.1f,
                        1.0f, 0.2f,
                        1.0f, 0.5f, 0.0f, 1.0f,
                        1.0f, 1.0f, 1.0f, 0.0f);
            }
        }
        hitAreaForceField.setPosition(position);
        magneticField.setPosition(position);
        checkBorders();
    }

    public void damageForceField(int amount) {
        powerForceField -= amount;
        if (powerForceField < 0) {
            powerForceField = 0;
        }
    }

    public void updateScore(float dt) {
        if (scoreView < score) {
            scoreView += 1500 * dt;
            if (scoreView > score) {
                scoreView = score;
            }
        }
    }
}
