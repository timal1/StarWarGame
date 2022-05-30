package com.starwar.app.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.starwar.app.screen.utils.Assets;

public class Weapon {
    private GameController gc;
    private Ship ship;
    private float firePeriod;
    private int damage;
    private float bulletSpeed;
    private int maxBullet;
    private int currentBullet;
    private Sound soundShoot;
    private Vector3[] slots;

    public int getDamage() {
        return damage;
    }

    public int getMaxBullet() {
        return maxBullet;
    }

    public int getCurBullet() {
        return currentBullet;
    }

    public Weapon(GameController gc, Ship ship, float firePeriod, int damage,
                  float bulletSpeed, int maxBullet, Vector3[] slots) {
        this.gc = gc;
        this.ship = ship;
        this.firePeriod = firePeriod;
        this.damage = damage;
        this.bulletSpeed = bulletSpeed;
        this.maxBullet = maxBullet;
        this.currentBullet = maxBullet;
        this.slots = slots;
        this.soundShoot = Assets.getInstance().getAssetManager().get("audio/shoot.mp3");
    }

    public void fire() {
        if (currentBullet > 0) {
            currentBullet--;
            soundShoot.play();

            for (int i = 0; i < slots.length; i++) {
                float x, y, vx, vy;
                x = ship.getPosition().x + slots[i].x * MathUtils.cosDeg(ship.getAngle() + slots[i].y);
                y = ship.getPosition().y + slots[i].x * MathUtils.sinDeg(ship.getAngle() + slots[i].y);
                vx = ship.getVelocity().x + bulletSpeed * MathUtils.cosDeg(ship.getAngle() + slots[i].z);
                vy = ship.getVelocity().y + bulletSpeed * MathUtils.sinDeg(ship.getAngle() + slots[i].z);

                gc.getBulletController().setup(ship, x, y, vx, vy);
            }
        }
    }

    public int addAmmos(int amount) {
        int oldCurrentBullet = currentBullet;
        currentBullet += amount;
        if (currentBullet > maxBullet) {
            currentBullet = maxBullet;
        }
        return currentBullet - oldCurrentBullet;
    }
}
