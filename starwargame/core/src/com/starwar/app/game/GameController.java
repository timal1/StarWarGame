
package com.starwar.app.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.starwar.app.screen.ScreenManager;
import com.starwar.app.screen.utils.Assets;

public class GameController {
    private Background background;
    private Hero hero;
    private AsteroidController asteroidController;
    private BulletController bulletController;
    private ParticleController particleController;
    private InfoController infoController;
    private Vector2 tempVec;
    private Vector2 tempVectorPowerUps;
    private PowerUpsController powerUpsController;
    private Stage stage;
    private int gameLevel;
    private boolean pause;
    private float timer;
    private Music music;
    private Sound soundGong;
    private BotController botController;

    public float getTimer() {
        return timer;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public int getGameLevel() {
        return gameLevel;
    }

    public Stage getStage() {
        return stage;
    }

    public ParticleController getParticleController() {
        return particleController;
    }

    public Background getBackground() {
        return background;
    }

    public Hero getHero() {
        return hero;
    }

    public AsteroidController getAsteroidController() {
        return asteroidController;
    }

    public BulletController getBulletController() {
        return bulletController;
    }

    public PowerUpsController getPowerUpsController() {
        return powerUpsController;
    }

    public InfoController getInfoController() {
        return infoController;
    }

    public BotController getBotController() {
        return botController;
    }

    public GameController(SpriteBatch batch) {
        this.background = new Background(this);
        this.hero = new Hero(this);
        this.botController = new BotController(this);
        this.asteroidController = new AsteroidController(this);
        this.bulletController = new BulletController(this);
        this.particleController = new ParticleController();
        this.powerUpsController = new PowerUpsController(this);
        this.botController = new BotController(this);

        this.infoController = new InfoController();
        this.tempVec = new Vector2();
        this.tempVectorPowerUps = new Vector2();
        this.stage = new Stage(ScreenManager.getInstance().getViewport(), batch);
        this.stage.addActor(hero.getShop());
        this.gameLevel = 1;

        this.music = Assets.getInstance().getAssetManager().get("audio/mortal.mp3");
        this.music.setLooping(true);
        this.music.play();

        this.soundGong = Assets.getInstance().getAssetManager().get("audio/gong.mp3");

        Gdx.input.setInputProcessor(stage);

        addAsteroid();
    }

    public void addAsteroid() {
        for (int i = 0; i < gameLevel * 2; i++) {
            asteroidController.setup(MathUtils.random(-129, ScreenManager.SCREEN_WIDTH + 129),
                    MathUtils.random(-129, ScreenManager.SCREEN_HEIGHT + 129),
                    MathUtils.random(100), MathUtils.random(100), 1.0f);
        }
    }

    public void update(float dt) {
        if (pause) {
            return;
        }
        timer += dt;
        background.update(dt);
        asteroidController.update(dt);
        bulletController.update(dt);
        particleController.update(dt);
        powerUpsController.update(dt);
        infoController.update(dt);
        botController.update(dt);

        hero.update(dt);

        stage.act(dt);
        checkCollisions();

        if (!hero.isAlive()) {
            ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.GAME_OVER, hero);
        }

        if (asteroidController.getActiveList().size() == 0) {
            timer = 0;
            gameLevel++;

            // в начале каждого уровня удаляем всех ботов
            for (int i = 0; i < botController.getActiveList().size(); i++) {
                Bot bot = botController.getActiveList().get(i);
                bot.deActivate();
            }

            addAsteroid();
        }

        if (timer == 0) {
            soundGong.play();
        }
    }

    public void checkCollisions() {

        // столкновение powerups-ов с героем
        collisionPowerUpsHero();

        // столкновения астероидов и героя
        collisionHeroAsteroid();

        // столкновение пуль и астероидов
        bulletHitAsteroid();

        //столкновение астероида и бота
        collisionBotAsteroid();

        // столкновения бота и героя
        collisionBotHero();

        // столкновение пуль c кораблем
        bulletsHitShip();

    }

    public void collisionPowerUpsHero() {
        for (int i = 0; i < powerUpsController.getActiveList().size(); i++) {
            PowerUp p = powerUpsController.getActiveList().get(i);
            if (hero.getMagneticField().contains(p.getPosition())) {
                tempVec.set(hero.getPosition()).sub(p.getPosition()).nor();
                p.getPosition().mulAdd(tempVec, 2 * getGameLevel());
                if (hero.getHitArea().contains(p.getPosition())) {
                    hero.consume(p);
                    particleController.getEffectBuilder().takePowerUpsEffect(p);
                    p.deActivate();
                }
            }
        }
    }

    public void collisionHeroAsteroid() {
        for (int i = 0; i < asteroidController.getActiveList().size(); i++) {
            Asteroid a = asteroidController.getActiveList().get(i);
            if (hero.getPowerForceField() > 0 & hero.hitAreaForceField.overlaps(a.getHitArea())) {
                float dst = a.getPosition().dst(hero.getPosition());
                float halfOverLen = (a.getHitArea().radius + hero.getHitAreaForceField().radius - dst)/ 2.0f;
                tempVec.set(hero.getPosition()).sub(a.getPosition()).nor();
                hero.getPosition().mulAdd(tempVec, halfOverLen);
                a.getPosition().mulAdd(tempVec, -halfOverLen);

                // отлет друг от лруга зависит от радиуса
                float sumScl = hero.getHitArea().radius * 2 + a.getHitArea().radius;
                hero.getVelocity().mulAdd(tempVec, 200.0f * a.getHitArea().radius / sumScl);
                a.getVelocity().mulAdd(tempVec, -200.0f * hero.getHitArea().radius / sumScl);

                // урон от столкновения
                if (a.takeDamage(2)) {
                    hero.addScore(a.getHpMax() * 50);
                }
                hero.damageForceField(2 * gameLevel);
            }

            if (hero.getPowerForceField() == 0 & hero.getHitArea().overlaps(a.getHitArea())) {
                float dst = a.getPosition().dst(hero.getPosition());
                float halfOverLen = (a.getHitArea().radius + hero.getHitArea().radius - dst)/ 2.0f;
                tempVec.set(hero.getPosition()).sub(a.getPosition()).nor();
                hero.getPosition().mulAdd(tempVec, halfOverLen);
                a.getPosition().mulAdd(tempVec, -halfOverLen);

                // отлет друг от лруга зависит от радиуса
                float sumScl = hero.getHitArea().radius * 2 + a.getHitArea().radius;
                hero.getVelocity().mulAdd(tempVec, 200.0f * a.getHitArea().radius / sumScl);
                a.getVelocity().mulAdd(tempVec, -200.0f * hero.getHitArea().radius / sumScl);

                // урон от столкновения
                if (a.takeDamage(2)) {
                    hero.addScore(a.getHpMax() * 50);
                }
                hero.takeDamage(2 * gameLevel);
            }
        }
    }

    public void bulletHitAsteroid() {
        for (int i = 0; i < bulletController.getActiveList().size(); i++) {
            Bullet b = bulletController.getActiveList().get(i);
            for (int j = 0; j < asteroidController.getActiveList().size(); j++) {
                Asteroid a = asteroidController.getActiveList().get(j);
                if (a.getHitArea().contains(b.getPosition())) {
                    particleController.getEffectBuilder().effectBulletCollideWithAsteroid(b);
                    b.deActivate();
                    if (a.takeDamage(b.getOwner().getCurrentWeapon().getDamage()) & b.getOwner().getOwnerType() == OwnerType.PLAYER) {
                        hero.addScore(a.getHpMax() * 100);
                        for (int k = 0; k < 3; k++) {
                            powerUpsController.setup(a.getPosition().x, a.getPosition().y,
                                    a.getScale() * 0.25f);
                        }
                        // появление бота с 10% вероятностью в зависимости от размера астероида
                        if (MathUtils.random(0, 100) < 10 * a.getScale()) {
                            botController.setup(a.getPosition().x, a.getPosition().y);
                        }
                    }

                    if (a.takeDamage(b.getOwner().getCurrentWeapon().getDamage()) & b.getOwner().getOwnerType() == OwnerType.BOT) {

                        for (int k = 0; k < 3; k++) {
                            powerUpsController.setup(a.getPosition().x, a.getPosition().y,
                                    a.getScale() * 0.25f);
                        }
                    }
                    break;
                }
            }
        }
    }

    public void collisionBotAsteroid() {
        for (int i = 0; i < asteroidController.getActiveList().size(); i++) {
            Asteroid a = asteroidController.getActiveList().get(i);
            for (int j = 0; j < botController.getActiveList().size(); j++) {
                Bot bot = botController.getActiveList().get(j);
                if (bot.getHitArea().overlaps(a.getHitArea())) {
                    float dst = a.getPosition().dst(bot.getPosition());
                    float halfOverLen = (a.getHitArea().radius + bot.getHitArea().radius - dst) / 2.0f;
                    tempVec.set(bot.getPosition()).sub(a.getPosition()).nor();
                    bot.getPosition().mulAdd(tempVec, halfOverLen);
                    a.getPosition().mulAdd(tempVec, -halfOverLen);

                    // отлет друг от лруга зависит от радиуса
                    float sumScl = bot.getHitArea().radius * 2 + a.getHitArea().radius;
                    bot.getVelocity().mulAdd(tempVec, 200.0f * a.getHitArea().radius / sumScl);
                    bot.getVelocity().set(MathUtils.cosDeg(bot.getAngle()) * gameLevel * MathUtils.random(10),
                            MathUtils.sinDeg(bot.getAngle()) * gameLevel * MathUtils.random(10));
                    a.getVelocity().mulAdd(tempVec, -200.0f * bot.getHitArea().radius / sumScl);

                    // урон от столкновения
                    a.takeDamage(1);
                    bot.takeDamage(gameLevel);
                }
            }
        }
    }

    public void bulletsHitShip() {
        for (int i = 0; i < bulletController.getActiveList().size(); i++) {
            Bullet b = bulletController.getActiveList().get(i);

            if(b.getOwner().getOwnerType() == OwnerType.BOT) {
                if (hero.getPowerForceField() > 0 & hero.getHitAreaForceField().contains(b.getPosition())) {

                    particleController.getEffectBuilder().effectBulletCollideWithAsteroid(b);
                    b.deActivate();
                    hero.damageForceField(b.getOwner().getCurrentWeapon().getDamage());
                }

                if (hero.getPowerForceField() <= 0 & hero.getHitArea().contains(b.getPosition())) {

                    particleController.getEffectBuilder().effectBulletCollideWithAsteroid(b);
                    b.deActivate();
                    hero.takeDamage(b.getOwner().getCurrentWeapon().getDamage());
                }
            }

            if(b.getOwner().getOwnerType() == OwnerType.PLAYER) {
                for (int j = 0; j < botController.getActiveList().size(); j++) {
                    Bot bot = botController.getActiveList().get(j);
                    if (bot.getHitArea().contains(b.getPosition())) {
                        particleController.getEffectBuilder().effectBulletCollideWithAsteroid(b);
                        b.deActivate();
                        bot.takeDamage(b.getOwner().getCurrentWeapon().getDamage());
                        hero.addScore(bot.getHpMax() * 300);
                    }
                }
            }
        }
    }

    public void collisionBotHero() {
        for (int i = 0; i < botController.getActiveList().size(); i++) {
            Bot bot = botController.getActiveList().get(i);
            if (hero.getPowerForceField() > 0 & hero.getHitAreaForceField().overlaps(bot.getHitArea())) {
                float dst = bot.getPosition().dst(hero.getPosition());
                float halfOverLen = (bot.getHitArea().radius + hero.getHitAreaForceField().radius - dst)/ 2.0f;
                tempVec.set(hero.getPosition()).sub(bot.getPosition()).nor();
                hero.getPosition().mulAdd(tempVec, halfOverLen);
                bot.getPosition().mulAdd(tempVec, -halfOverLen);

                // отлет друг от лруга зависит от радиуса
                float sumScl = hero.getHitArea().radius * 2 + bot.getHitArea().radius;
                hero.getVelocity().mulAdd(tempVec, 200.0f * bot.getHitArea().radius / sumScl);
                bot.getVelocity().mulAdd(tempVec, -200.0f * hero.getHitArea().radius / sumScl);
                bot.getVelocity().set(MathUtils.cosDeg(bot.getAngle()) * gameLevel * MathUtils.random(10),
                        MathUtils.sinDeg(bot.getAngle()) * gameLevel * MathUtils.random(10));

                // урон от столкновения

                hero.addScore(bot.getHpMax() * 50);
                bot.takeDamage(2 * gameLevel);
                hero.damageForceField(2 * gameLevel);
            }

            if (hero.getPowerForceField() == 0 & hero.getHitArea().overlaps(bot.getHitArea())) {
                float dst = bot.getPosition().dst(hero.getPosition());
                float halfOverLen = (bot.getHitArea().radius + hero.getHitArea().radius - dst)/ 2.0f;
                tempVec.set(hero.getPosition()).sub(bot.getPosition()).nor();
                hero.getPosition().mulAdd(tempVec, halfOverLen);
                bot.getPosition().mulAdd(tempVec, -halfOverLen);

                // отлет друг от лруга зависит от радиуса
                float sumScl = hero.getHitArea().radius * 2 + bot.getHitArea().radius;
                hero.getVelocity().mulAdd(tempVec, 200.0f * bot.getHitArea().radius / sumScl);
                bot.getVelocity().mulAdd(tempVec, -200.0f * hero.getHitArea().radius / sumScl);
                bot.getVelocity().set(MathUtils.cosDeg(bot.getAngle()) * gameLevel * MathUtils.random(10),
                        MathUtils.sinDeg(bot.getAngle()) * gameLevel * MathUtils.random(10));

                // урон от столкновения

                hero.addScore(bot.getHpMax() * 50);
                bot.takeDamage(2 * gameLevel);
                hero.takeDamage(2 * gameLevel);
            }
        }
    }

    public void dispose() {
        background.dispose();
    }
}
