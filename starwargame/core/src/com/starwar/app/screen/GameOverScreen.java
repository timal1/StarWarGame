package com.starwar.app.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.starwar.app.game.Background;
import com.starwar.app.game.Hero;
import com.starwar.app.screen.utils.Assets;

import static com.starwar.app.screen.ScreenManager.ScreenType.MENU;

public class GameOverScreen extends AbstractScreen {

    private BitmapFont font72;
    private BitmapFont font48;
    private BitmapFont font24;
    private Background background;
    private Hero defeatedHero;
    private StringBuilder sb;
    private Sound soundGameOver;

    public GameOverScreen(SpriteBatch batch) {
        super(batch);
    }

    public void setDefeatedHero(Hero defeatedHero) {
        this.defeatedHero = defeatedHero;
    }

    @Override
    public void show() {
        this.font72 = Assets.getInstance().getAssetManager().get("fonts/font72.ttf");
        this.font48 = Assets.getInstance().getAssetManager().get("fonts/font48.ttf");
        this.font24 = Assets.getInstance().getAssetManager().get("fonts/font24.ttf");
        this.background = new Background(null);
        this.sb = new StringBuilder();

        this.soundGameOver = Assets.getInstance().getAssetManager().get("audio/gameover.mp3");
        this.soundGameOver.play();
    }

    public void update(float dt) {
       background.update(dt);
       if (Gdx.input.justTouched()) {
           ScreenManager.getInstance().changeScreen(MENU);
       }
    }

    @Override
    public void render(float delta) {
        update(delta);
        ScreenUtils.clear(1, 0.0f, 0.0f,1);

        batch.begin();
   //     background.render(batch);
        font72.draw(batch, "GAME OVER", defeatedHero.getPosition().x - 650,
                defeatedHero.getPosition().y + 100,
                ScreenManager.SCREEN_WIDTH, 1, false);
        sb.setLength(0);
        sb.append("SCORE: ").append(defeatedHero.getScore()).append("\n");
        sb.append("MONEY: ").append(defeatedHero.getMoney()).append("\n");
        font48.draw(batch, sb, defeatedHero.getPosition().x - 650,
                defeatedHero.getPosition().y - 50,
                ScreenManager.SCREEN_WIDTH, 1, false);
        font24.draw(batch, "Tap screen to return to menu", defeatedHero.getPosition().x - 650,
                defeatedHero.getPosition().y - 300,
                ScreenManager.SCREEN_WIDTH, 1, false);
        batch.end();
    }

    @Override
    public void dispose() {
        background.dispose();
    }
}
