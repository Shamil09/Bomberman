package com.geekbrains.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.io.Serializable;
import java.util.List;


public class Bomb implements Poolable, Serializable {
    private Bomberman owner;
    private transient TextureRegion texture;
    private transient GameScreen gs;
    private int cellX, cellY;
    private int radius;
    private float time;
    private float maxTime;
    private boolean active;

    @Override
    public boolean isActive() {
        return active;
    }

    public int getCellX() {
        return cellX;
    }

    public int getCellY() {
        return cellY;
    }

    public void reloadResources(GameScreen gs, TextureRegion texture) {
        this.gs = gs;
        this.texture = texture;
    }

    public Bomb(GameScreen gs, TextureRegion texture) {
        this.gs = gs;
        this.texture = texture;
        this.owner = null;
    }

    public void update(float dt) {
        time += dt;
        if (time >= maxTime) {
            boom();
        }
    }

    public void boom() {
        boolean left = true, right = true, up = true, down = true;
        active = false;
        boomInCell(cellX, cellY);
        for (int i = 1; i <= radius; i++) {
            if (right && cellX + i < gs.getMap().getMapX() - 1)
                right = boomInCell(cellX + i, cellY);
            if (left && cellX - i > 0) left = boomInCell(cellX - i, cellY);
            if (up && cellY + i < gs.getMap().getMapY() - 1) up = boomInCell(cellX, cellY + i);
            if (down && cellY - i > 0) down = boomInCell(cellX, cellY - i);
        }
    }

    private boolean boomInCell(int cellX, int cellY) {
        if (gs.getMap().isCellUndestructable(cellX, cellY)) {
            return false;
        }
        gs.getAnimationEmitter().createAnimation((cellX) * Rules.CELL_SIZE + Rules.CELL_HALF_SIZE, cellY * Rules.CELL_SIZE + Rules.CELL_HALF_SIZE, 4.0f, AnimationEmitter.AnimationType.EXPLOSION);
        List<Bot> damagedBots = gs.getBotEmitter().getBotsInCell(cellX, cellY);
        for (int i = 0; i < damagedBots.size(); i++) {
            damagedBots.get(i).destroy();
            gs.getAnimationEmitter().createAnimation(damagedBots.get(i).getPosition().x, damagedBots.get(i).getPosition().y, 2.0f, AnimationEmitter.AnimationType.BLOOD);
            owner.addScore(500);
        }
        if (gs.getPlayer().getPosition().dst(cellX * Rules.CELL_SIZE + Rules.CELL_HALF_SIZE, cellY * Rules.CELL_SIZE + Rules.CELL_HALF_SIZE) < Rules.CELL_SIZE * 0.8f) {
            gs.getPlayer().takeDamage(1);
        }
        if (gs.getMap().isCellDestructable(cellX, cellY)) {
            if (gs.getMap().getCellType(cellX, cellY) == Map.CellType.BOX) {
                gs.getMap().clearCell(cellX, cellY);
                if (MathUtils.random(0, 100) < Rules.BONUS_CHANCE) {
                    gs.getMap().setBonus(cellX, cellY);
                }
            } else {
                gs.getMap().clearCell(cellX, cellY);
            }
            owner.addScore(100);
            return false;
        }
        if (gs.getMap().isCellBomb(cellX, cellY)) {
            gs.getMap().clearCell(cellX, cellY);
            gs.getBombEmitter().tryToDetonateBomb(cellX, cellY);
            return false;
        }
        return true;
    }

    public void activate(Bomberman owner, int cellX, int cellY, float maxTime, int radius) {
        this.owner = owner;
        this.cellX = cellX;
        this.cellY = cellY;
        this.gs.getMap().setBombCell(cellX, cellY);
        this.maxTime = maxTime;
        this.radius = radius;
        this.time = 0.0f;
        this.active = true;
    }

    public void detonate() {
        this.time = this.maxTime;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, cellX * Rules.CELL_SIZE, cellY * Rules.CELL_SIZE);
    }
}
