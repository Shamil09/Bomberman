package com.geekbrains.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Map implements Serializable {
    public enum CellType {
        EMPTY('0'), WALL('1'), BOX('2'), BOMB('_'), PLAYER_START('s'), BONUS('_');

        private char fileSymbol;

        CellType(char fileSymbol) {
            this.fileSymbol = fileSymbol;
        }

        public static CellType getCellTypeFromChar(char in) {
            for (int i = 0; i < CellType.values().length; i++) {
                if (CellType.values()[i].fileSymbol == in) {
                    return CellType.values()[i];
                }
            }
            throw new IllegalArgumentException("Invalid map cell symbol");
        }
    }

    private int mapX, mapY;
    private CellType[][] data;
    private transient TextureRegion textureGrass, textureWall, textureBox, textureKey, textureBonus;
    private Vector2 startPosition;
    private int exitPositionX, exitPositionY;

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }

    public Vector2 getStartPosition() {
        return startPosition;
    }

    public CellType getCellType(int cellX, int cellY) {
        return data[cellX][cellY];
    }

    public void reloadResources() {
        textureBox = Assets.getInstance().getAtlas().findRegion("box");
        textureGrass = Assets.getInstance().getAtlas().findRegion("grass");
        textureWall = Assets.getInstance().getAtlas().findRegion("wall");
        textureKey = Assets.getInstance().getAtlas().findRegion("key");
        textureBonus = Assets.getInstance().getAtlas().findRegion("bonus");
    }

    public Map() {
        textureBox = Assets.getInstance().getAtlas().findRegion("box");
        textureGrass = Assets.getInstance().getAtlas().findRegion("grass");
        textureWall = Assets.getInstance().getAtlas().findRegion("wall");
        textureKey = Assets.getInstance().getAtlas().findRegion("key");
        textureBonus = Assets.getInstance().getAtlas().findRegion("bonus");
    }

    public void loadMap(String mapName) {
        BufferedReader br = null;
        try {
            br = Gdx.files.internal(mapName).reader(8192);
            String str;
            mapX = Integer.parseInt(br.readLine());
            mapY = Integer.parseInt(br.readLine());
            data = new CellType[mapX][mapY];
            for (int i = mapY - 1; i >= 0; i--) {
                str = br.readLine();
                for (int j = 0; j < mapX; j++) {
                    char c = str.charAt(j);
                    data[j][i] = CellType.getCellTypeFromChar(c);
                    if (data[j][i] == CellType.PLAYER_START) {
                        startPosition = new Vector2(j * Rules.CELL_SIZE + Rules.CELL_HALF_SIZE, i * Rules.CELL_SIZE + Rules.CELL_HALF_SIZE);
                        data[j][i] = CellType.EMPTY;
                    }
                }
            }

            do {
                exitPositionX = MathUtils.random(0, mapX - 1);
                exitPositionY = MathUtils.random(0, mapY - 1);
            } while (data[exitPositionX][exitPositionY] != CellType.BOX);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < mapX; i++) {
            for (int j = 0; j < mapY; j++) {
                batch.draw(textureGrass, i * Rules.CELL_SIZE, j * Rules.CELL_SIZE);
                if (i == exitPositionX && j == exitPositionY) {
                    batch.draw(textureKey, i * Rules.CELL_SIZE, j * Rules.CELL_SIZE);
                }
                if (data[i][j] == CellType.WALL) {
                    batch.draw(textureWall, i * Rules.CELL_SIZE, j * Rules.CELL_SIZE);
                }
                if (data[i][j] == CellType.BONUS) {
                    batch.draw(textureBonus, i * Rules.CELL_SIZE, j * Rules.CELL_SIZE);
                }
                if (data[i][j] == CellType.BOX) {
                    batch.draw(textureBox, i * Rules.CELL_SIZE, j * Rules.CELL_SIZE);
                }

            }
        }
    }

    public boolean isCellEmpty(int cellX, int cellY) {
        return data[cellX][cellY] == CellType.EMPTY;
    }

    public boolean isCellPassable(int cellX, int cellY) {
        return data[cellX][cellY] == CellType.EMPTY || data[cellX][cellY] == CellType.BONUS;
    }

    public boolean isCellDestructable(int cellX, int cellY) {
        return data[cellX][cellY] == CellType.BOX;
    }

    public boolean isCellBomb(int cellX, int cellY) {
        return data[cellX][cellY] == CellType.BOMB;
    }

    public boolean isCellUndestructable(int cellX, int cellY) {
        return data[cellX][cellY] == CellType.WALL;
    }

    public void setBombCell(int cellX, int cellY) {
        data[cellX][cellY] = CellType.BOMB;
    }

    public void clearCell(int cellX, int cellY) {
        data[cellX][cellY] = CellType.EMPTY;
    }

    public boolean checkCellForKey(int cellX, int cellY) {
        return exitPositionX == cellX && exitPositionY == cellY;
    }

    public boolean checkCellForBonus(int cellX, int cellY) {
        return data[cellX][cellY] == CellType.BONUS;
    }

    public void setBonus(int cellX, int cellY) {
        data[cellX][cellY] = CellType.BONUS;
    }
}
