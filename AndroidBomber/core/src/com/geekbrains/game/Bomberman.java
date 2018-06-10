package com.geekbrains.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Bomberman implements Serializable {
    public enum State {
        IDLE(0), MOVE(1);

        private int animationIndex;

        State(int animationIndex) {
            this.animationIndex = animationIndex;
        }
    }

    public enum UpgradeType {
        HP, BOMBRADIUS, MONEY, LIFE;
    }

    private transient GameScreen gs;
    private transient Animation[] animations;
    private transient TextureRegion hpTexture, baseHpTexture;
    private Vector2 position;
    private Vector2 velocity;
    private State currentState;
    private transient StringBuilder tmpStringBuilder;
    private char prefferedDirection;
    private float pathCounter;
    private float speed;
    private int score, scoreToShow;
    private int lifes, hp, maxHp;
    private int bombPower;
    private float damagedTimer;

    public void reloadResources(GameScreen gs) {
        this.gs = gs;
        this.animations = new Animation[State.values().length];
        for (int i = 0; i < State.values().length; i++) {
            this.animations[i] = new Animation();
            this.animations[i].activate(0, 0, 1, new TextureRegion(Assets.getInstance().getAtlas().findRegion("bomber")).split(Rules.CELL_SIZE, Rules.CELL_SIZE)[i], 0.1f, true);
        }
        this.tmpStringBuilder = new StringBuilder(32);
        this.baseHpTexture = Assets.getInstance().getAtlas().findRegion("hpBar").split(80, 80)[0][1];
        this.hpTexture = Assets.getInstance().getAtlas().findRegion("hpBar").split(80, 80)[0][0];
    }

    public void setPrefferedDirection(char prefferedDirection) {
        this.prefferedDirection = prefferedDirection;
    }

    public Vector2 getPosition() {
        return position;
    }

    public int getCellX() {
        return (int) (position.x / Rules.CELL_SIZE);
    }

    public int getCellY() {
        return (int) (position.y / Rules.CELL_SIZE);
    }

    public boolean isVulnerable() {
        return damagedTimer < 0.001f;
    }

    public void startNewLevel() {
        this.position.set(gs.getMap().getStartPosition());
        this.damagedTimer = 0.0f;
        this.currentState = State.IDLE;
        this.velocity.set(0, 0);
        this.pathCounter = -1;
        this.prefferedDirection = '-';
    }

    public Bomberman(GameScreen gs) {
        this.gs = gs;
        this.position = new Vector2(0.0f, 0.0f);
        this.velocity = new Vector2(0.0f, 0.0f);
        this.speed = 200.0f;
        this.pathCounter = -1;
        this.bombPower = 1;
        this.animations = new Animation[State.values().length];
        this.maxHp = 3;
        this.lifes = 3;
        this.hp = this.maxHp;
        for (int i = 0; i < State.values().length; i++) {
            this.animations[i] = new Animation();
            this.animations[i].activate(0, 0, 1, new TextureRegion(Assets.getInstance().getAtlas().findRegion("bomber")).split(Rules.CELL_SIZE, Rules.CELL_SIZE)[i], 0.1f, true);
        }
        this.currentState = State.IDLE;
        this.score = 0;
        this.scoreToShow = 0;
        this.tmpStringBuilder = new StringBuilder(32);
        this.baseHpTexture = Assets.getInstance().getAtlas().findRegion("hpBar").split(80, 80)[0][1];
        this.hpTexture = Assets.getInstance().getAtlas().findRegion("hpBar").split(80, 80)[0][0];
    }

    public void upgrade(UpgradeType type) {
        switch (type) {
            case HP:
                maxHp++;
                hp = maxHp;
                break;
            case LIFE:
                if (MathUtils.random(0, 100) < 8) {
                    lifes++;
                }
                break;
            case MONEY:
                score += 100000;
                break;
            case BOMBRADIUS:
                bombPower++;
                break;
        }
    }

    public void render(SpriteBatch batch) {
        if (isVulnerable()) {
            batch.draw(animations[currentState.animationIndex].getCurrentRegion(), position.x - Rules.CELL_HALF_SIZE, position.y - Rules.CELL_HALF_SIZE);
        } else {
            if (damagedTimer % 0.4f < 0.2f) {
                batch.draw(animations[currentState.animationIndex].getCurrentRegion(), position.x - Rules.CELL_HALF_SIZE, position.y - Rules.CELL_HALF_SIZE);
            }
        }
    }

    public void takeDamage(int dmg) {
        if (isVulnerable()) {
            hp -= dmg;
            damagedTimer = 3.0f;
            if (hp <= 0) {
                lifes--;
                maxHp = 3;
                hp = maxHp;
                bombPower = 1;
            }
        }
    }

    public void renderGUI(SpriteBatch batch, BitmapFont font) {
        tmpStringBuilder.setLength(0);
        tmpStringBuilder.append("Score: ").append(scoreToShow);
        font.draw(batch, tmpStringBuilder, 940, 680);
        for (int i = 0; i < maxHp; i++) {
            batch.draw(baseHpTexture, 100 + i * 40, 620);
            if (i < hp) {
                batch.draw(hpTexture, 100 + i * 40, 620);
            }
        }
        batch.setColor(0.2f, 1f, 0.2f, 1);
        batch.draw(baseHpTexture, 20, 610, 100, 100);
        batch.setColor(1, 1, 1, 1);
        tmpStringBuilder.setLength(0);
        tmpStringBuilder.append("x").append(lifes);
        font.draw(batch, tmpStringBuilder, 50, 665);
    }

    public void update(float dt) {
        animations[currentState.animationIndex].update(dt);
        if (!isVulnerable()) {
            damagedTimer -= dt;
            if (damagedTimer < 0.0f) {
                damagedTimer = 0.0f;
            }
        }

        if (scoreToShow < score) {
            int amountToAdd = (int)((score - scoreToShow) * 0.01f);
            if(amountToAdd < 4) {
                amountToAdd = 4;
            }
            scoreToShow += amountToAdd;
            if (scoreToShow > score) {
                scoreToShow = score;
            }
        }

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

        if ((Gdx.input.isKeyPressed(Input.Keys.D) || prefferedDirection == 'R') && pathCounter < 0.0f && gs.getMap().isCellPassable(getCellX() + 1, getCellY())) {
            velocity.set(speed, 0.0f);
            pathCounter = 0.1f;
            currentState = State.MOVE;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.A) || prefferedDirection == 'L') && pathCounter < 0.0f && gs.getMap().isCellPassable(getCellX() - 1, getCellY())) {
            velocity.set(-speed, 0.0f);
            pathCounter = 0.1f;
            currentState = State.MOVE;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.W) || prefferedDirection == 'U') && pathCounter < 0.0f && gs.getMap().isCellPassable(getCellX(), getCellY() + 1)) {
            velocity.set(0.0f, speed);
            pathCounter = 0.1f;
            currentState = State.MOVE;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.S) || prefferedDirection == 'D') && pathCounter < 0.0f && gs.getMap().isCellPassable(getCellX(), getCellY() - 1)) {
            velocity.set(0.0f, -speed);
            pathCounter = 0.1f;
            currentState = State.MOVE;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            setupBomb();
        }
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            prefferedDirection = '-';
        }
    }

    public void setupBomb() {
        if (!gs.getBombEmitter().isBombInCell(getCellX(), getCellY()) && gs.getMap().isCellEmpty(getCellX(), getCellY())) {
            gs.getBombEmitter().getActiveElement().activate(this, getCellX(), getCellY(), 2.5f, bombPower);
        }
    }

    public void addScore(int amount) {
        score += amount;
    }
}
