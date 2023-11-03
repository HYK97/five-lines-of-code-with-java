package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Game extends JPanel {
    private static final int TILE_SIZE = 30;
    private static final int FPS = 30;
    private static final int SLEEP = 1000 / FPS;

    private enum Tile {
        AIR,
        FLUX,
        UNBREAKABLE,
        PLAYER,
        STONE,
        FALLING_STONE,
        BOX,
        FALLING_BOX,
        KEY1,
        LOCK1,
        KEY2,
        LOCK2
    }

    private enum Input {
        UP, DOWN, LEFT, RIGHT
    }

    private int playerX = 1;
    private int playerY = 1;
    private Tile[][] map = {
            {Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE},
            {Tile.UNBREAKABLE, Tile.PLAYER, Tile.AIR, Tile.FLUX, Tile.FLUX, Tile.UNBREAKABLE, Tile.AIR, Tile.UNBREAKABLE},
            {Tile.UNBREAKABLE, Tile.STONE, Tile.UNBREAKABLE, Tile.BOX, Tile.FLUX, Tile.UNBREAKABLE, Tile.AIR, Tile.UNBREAKABLE},
            {Tile.UNBREAKABLE, Tile.KEY1, Tile.STONE, Tile.FLUX, Tile.FLUX, Tile.UNBREAKABLE, Tile.AIR, Tile.UNBREAKABLE},
            {Tile.UNBREAKABLE, Tile.STONE, Tile.FLUX, Tile.FLUX, Tile.FLUX, Tile.LOCK1, Tile.AIR, Tile.UNBREAKABLE},
            {Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE, Tile.UNBREAKABLE}
    };

    private List<Input> inputs = new ArrayList<>();

    public Game() {
        setPreferredSize(new Dimension(map[0].length * TILE_SIZE, map.length * TILE_SIZE));

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
                repaint();
            }
        }, 0, SLEEP);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
        });
        setFocusable(true);
        requestFocus();
    }

    private void handleKeyPress(int keyCode) {
        if (keyCode == KeyEvent.VK_LEFT) {
            inputs.add(Input.LEFT);
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            inputs.add(Input.RIGHT);
        } else if (keyCode == KeyEvent.VK_UP) {
            inputs.add(Input.UP);
        } else if (keyCode == KeyEvent.VK_DOWN) {
            inputs.add(Input.DOWN);
        }
    }

    private void remove(Tile tile) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] == tile) {
                    map[y][x] = Tile.AIR;
                }
            }
        }
    }

    private void moveToTile(int newX, int newY) {
        map[playerY][playerX] = Tile.AIR;
        map[newY][newX] = Tile.PLAYER;
        playerX = newX;
        playerY = newY;
    }

    private void moveHorizontal(int dx) {
        if (map[playerY][playerX + dx] == Tile.FLUX
                || map[playerY][playerX + dx] == Tile.AIR) {
            moveToTile(playerX + dx, playerY);
        } else if ((map[playerY][playerX + dx] == Tile.STONE
                || map[playerY][playerX + dx] == Tile.BOX)
                && map[playerY][playerX + dx + dx] == Tile.AIR
                && map[playerY + 1][playerX + dx] != Tile.AIR) {
            map[playerY][playerX + dx + dx] = map[playerY][playerX + dx];
            moveToTile(playerX + dx, playerY);
        } else if (map[playerY][playerX + dx] == Tile.KEY1) {
            remove(Tile.LOCK1);
            moveToTile(playerX + dx, playerY);
        } else if (map[playerY][playerX + dx] == Tile.KEY2) {
            remove(Tile.LOCK2);
            moveToTile(playerX + dx, playerY);
        }
    }

    private void moveVertical(int dy) {
        if (map[playerY + dy][playerX] == Tile.FLUX
                || map[playerY + dy][playerX] == Tile.AIR) {
            moveToTile(playerX, playerY + dy);
        } else if (map[playerY + dy][playerX] == Tile.KEY1) {
            remove(Tile.LOCK1);
            moveToTile(playerX, playerY + dy);
        } else if (map[playerY + dy][playerX] == Tile.KEY2) {
            remove(Tile.LOCK2);
            moveToTile(playerX, playerY + dy);
        }
    }

    private void update() {
        handleInputs();
        updateMap();
    }

    private void updateMap() {
        for (int y = map.length - 1; y >= 0; y--) {
            for (int x = 0; x < map[y].length; x++) {
                updateTitle(y, x);
            }
        }
    }

    private void updateTitle(int y, int x) {
        if ((map[y][x] == Tile.STONE || map[y][x] == Tile.FALLING_STONE)
                && map[y + 1][x] == Tile.AIR) {
            map[y + 1][x] = Tile.FALLING_STONE;
            map[y][x] = Tile.AIR;
        } else if ((map[y][x] == Tile.BOX || map[y][x] == Tile.FALLING_BOX)
                && map[y + 1][x] == Tile.AIR) {
            map[y + 1][x] = Tile.FALLING_BOX;
            map[y][x] = Tile.AIR;
        } else if (map[y][x] == Tile.FALLING_STONE) {
            map[y][x] = Tile.STONE;
        } else if (map[y][x] == Tile.FALLING_BOX) {
            map[y][x] = Tile.BOX;
        }
    }

    private void handleInputs() {
        while (!inputs.isEmpty()) {
            Input current = inputs.remove(inputs.size() - 1);
            handleInput(current);
        }
    }

    private void handleInput(Input current) {
        if (current == Input.LEFT) {
            moveHorizontal(-1);
        } else if (current == Input.RIGHT) {
            moveHorizontal(1);
        } else if (current == Input.UP) {
            moveVertical(-1);
        } else if (current == Input.DOWN) {
            moveVertical(1);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {

        draw(g);
    }

    private void draw(Graphics g) {
        //init paint
        g.clearRect(0, 0, this.getWidth(), this.getHeight());

        // Draw map
        drawMap(g);

        // Draw player
        drawPlayer(g);
    }

    private void drawPlayer(Graphics g) {
        g.setColor(new Color(0xFF0000));
        g.fillRect(playerX * TILE_SIZE, playerY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    private void drawMap(Graphics g) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] == Tile.FLUX)
                    g.setColor(new Color(0xCCFFCC));
                else if (map[y][x] == Tile.UNBREAKABLE)
                    g.setColor(new Color(0x999999));
                else if (map[y][x] == Tile.STONE || map[y][x] == Tile.FALLING_STONE)
                    g.setColor(new Color(0x0000CC));
                else if (map[y][x] == Tile.BOX || map[y][x] == Tile.FALLING_BOX)
                    g.setColor(new Color(0x8B4513));
                else if (map[y][x] == Tile.KEY1 || map[y][x] == Tile.LOCK1)
                    g.setColor(new Color(0xFFCC00));
                else if (map[y][x] == Tile.KEY2 || map[y][x] == Tile.LOCK2)
                    g.setColor(new Color(0x00CCFF));

                if (map[y][x] != Tile.AIR && map[y][x] != Tile.PLAYER)
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Game");
            Game game = new Game();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
