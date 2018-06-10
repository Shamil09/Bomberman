package com.geekbrains.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Bot implements Poolable, Serializable {
    public Rectangle getHitBox() {
        return hitBox;
    }

    public enum State {
        IDLE(0), MOVE(1);

        private int animationIndex;

        State(int animationIndex) {
            this.animationIndex = animationIndex;
        }
    }

    public enum Direction {
        LEFT(-1, 0), RIGHT(1, 0), UP(0, 1), DOWN(0, -1);

        private int dx;
        private int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    private transient GameScreen gs;
    private transient Animation[] animations;
    private Vector2 position;
    private Vector2 velocity;
    private float pathCounter;
    private float speed;
    private State currentState;
    private boolean active;
    private Rectangle hitBox;

    public Vector2 getPosition() {
        return position;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public int getCellX() {
        return (int) (position.x / Rules.CELL_SIZE);
    }

    public int getCellY() {
        return (int) (position.y / Rules.CELL_SIZE);
    }

    public void reloadResources(GameScreen gs) {
        this.gs = gs;
        this.animations = new Animation[Bot.State.values().length];
        for (int i = 0; i < Bot.State.values().length; i++) {
            this.animations[i] = new Animation();
            this.animations[i].activate(0, 0, 1, new TextureRegion(Assets.getInstance().getAtlas().findRegion("bot")).split(Rules.CELL_SIZE, Rules.CELL_SIZE)[i], 0.1f, true);
        }
    }

    public Bot(GameScreen gs) {
        this.gs = gs;
        this.position = new Vector2(440.0f, 440.0f);
        this.velocity = new Vector2(0.0f, 0.0f);
        this.speed = 40.0f;
        this.pathCounter = -1;
        this.animations = new Animation[Bot.State.values().length];
        this.active = false;
        this.hitBox = new Rectangle(position.x, position.y, Rules.CELL_SIZE, Rules.CELL_SIZE);
        for (int i = 0; i < Bot.State.values().length; i++) {
            this.animations[i] = new Animation();
            this.animations[i].activate(0, 0, 1, new TextureRegion(Assets.getInstance().getAtlas().findRegion("bot")).split(Rules.CELL_SIZE, Rules.CELL_SIZE)[i], 0.1f, true);
        }
        this.currentState = State.IDLE;
    }

    public void destroy() {
        active = false;
    }

    public void init() {
        int cx = -1, cy = -1;
        do {
            cx = MathUtils.random(0, gs.getMap().getMapX() - 1);
            cy = MathUtils.random(0, gs.getMap().getMapY() - 1);
        } while (!gs.getMap().isCellEmpty(cx, cy));
        position.set(cx * Rules.CELL_SIZE + Rules.CELL_HALF_SIZE, cy * Rules.CELL_SIZE + Rules.CELL_HALF_SIZE);
        currentState = State.IDLE;
        pathCounter = -1;
        velocity.set(0.0f, 0.0f);
        active = true;
        hitBox.setPosition(position);
    }

    public void render(SpriteBatch batch) {
        batch.draw(animations[currentState.animationIndex].getCurrentRegion(), position.x - Rules.CELL_HALF_SIZE, position.y - Rules.CELL_HALF_SIZE);
    }

    public void update(float dt) {
        animations[currentState.animationIndex].update(dt);

        if (pathCounter > 0.0f) {
            position.mulAdd(velocity, dt);
            pathCounter += velocity.len() * dt;
            if (pathCounter >= Rules.CELL_SIZE) {
                position.x = getCellX() * Rules.CELL_SIZE + Rules.CELL_HALF_SIZE;
                position.y = getCellY() * Rules.CELL_SIZE + Rules.CELL_HALF_SIZE;
                pathCounter = -1.0f;
                currentState = State.IDLE;
            }
        }

        if (currentState == State.IDLE) {
            Direction direction = Direction.values()[MathUtils.random(0, 3)];
            if (gs.getMap().isCellPassable(getCellX() + direction.dx, getCellY() + direction.dy)) {
                velocity.set(direction.dx * speed, direction.dy * speed);
                pathCounter = 0.1f;
                currentState = State.MOVE;
            }
        }

        hitBox.setPosition(position);
    }
}
