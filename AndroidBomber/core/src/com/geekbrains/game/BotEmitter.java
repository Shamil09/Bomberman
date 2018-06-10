package com.geekbrains.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by FlameXander on 01.06.2018.
 */

public class BotEmitter extends ObjectPool<Bot> implements Serializable {
    private transient GameScreen gs;
    private int maxBotsCount;
    private float spawnTimer;
    private List<Bot> tmpList;
    private Rectangle tmpRect;

    @Override
    protected Bot newObject() {
        return new Bot(gs);
    }

    public void reloadResources(GameScreen gs) {
        this.gs = gs;
        for (int i = 0; i < activeList.size(); i++) {
            activeList.get(i).reloadResources(gs);
        }
        for (int i = 0; i < freeList.size(); i++) {
            freeList.get(i).reloadResources(gs);
        }
    }

    public BotEmitter(GameScreen gs) {
        this.gs = gs;
        this.maxBotsCount = 10;
        this.tmpList = new ArrayList<Bot>();
        this.tmpRect = new Rectangle(0, 0, Rules.CELL_SIZE - 10, Rules.CELL_SIZE - 10);
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < activeList.size(); i++) {
            activeList.get(i).render(batch);
        }
    }

    public List<Bot> getBotsInCell(int cellX, int cellY) {
        tmpList.clear();
        tmpRect.x = cellX * Rules.CELL_SIZE + 5;
        tmpRect.y = cellY * Rules.CELL_SIZE + 5;
        for (int i = 0; i < activeList.size(); i++) {
            if (tmpRect.overlaps(activeList.get(i).getHitBox())) {
                tmpList.add(activeList.get(i));
            }
        }
        return tmpList;
    }

    public void update(float dt) {
        if (activeList.size() < maxBotsCount) {
            spawnTimer += dt;
            if (spawnTimer > 2.0f) {
                spawnTimer = 0.0f;
                getActiveElement().init();
            }
        }
        for (int i = 0; i < activeList.size(); i++) {
            activeList.get(i).update(dt);
        }
        checkPool();
    }
}
