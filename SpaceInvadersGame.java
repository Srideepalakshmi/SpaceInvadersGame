import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.io.*;
import javax.sound.sampled.*;

class SpaceInvadersGame extends JPanel {
    private Image backgroundImage;
    private Image playerRocketImage;
    private List<Enemy> enemies;
    private List<Bullet> bullets;
    private List<EnemyBullet> enemyBullets;
    private int score = 0;
    private int level = 1;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean gameStarted = true;
    private Clip shootingSound;
    private Clip enemyExplosionSound;

    public SpaceInvadersGame() {
        backgroundImage = new ImageIcon("backgroundSkin.jpg").getImage();
        playerRocketImage = new ImageIcon("shipSkin.gif").getImage(); 
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        enemyBullets = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int x = (int) (Math.random() * 500);
            int y = (int) (Math.random() * 300);
            enemies.add(new Enemy(x, y));
        }     
        loadSoundEffects();  
        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameStarted && !gameOver) {    
                    for (Enemy enemy : enemies) {
                        enemy.move();
                        if (new Random().nextInt(50) == 0) { 
                            enemyBullets.add(new EnemyBullet(enemy.getX() + enemy.getWidth() / 2, enemy.getY() + enemy.getHeight()));
                        }
                    }
                    moveBullets();
                    moveEnemyBullets();
                    checkCollisions();
                    repaint(); 
                }
            }
        });
        timer.start();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (gameStarted && !gameOver) {
                    if (keyCode == KeyEvent.VK_LEFT) {
                        Player.x -= 5; 
                    } else if (keyCode == KeyEvent.VK_RIGHT) {
                        Player.x += 5; 
                    } else if (keyCode == KeyEvent.VK_SPACE) {
                        bullets.add(new Bullet(Player.x + 20, Player.y));
                        playShootingSound();
                    }
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();
    }

    private void loadSoundEffects() {
        try {
            AudioInputStream shootingStream = AudioSystem.getAudioInputStream(new File("bulletSound.wav"));
            shootingSound = AudioSystem.getClip();
            shootingSound.open(shootingStream);
            AudioInputStream explosionStream = AudioSystem.getAudioInputStream(new File("damageSound.wav"));
            enemyExplosionSound = AudioSystem.getClip();
            enemyExplosionSound.open(explosionStream);
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    private void playShootingSound() {
        if (shootingSound.isRunning()) {
            shootingSound.stop();
        }
        shootingSound.setFramePosition(0);
        shootingSound.start();
    }

    private void playEnemyExplosionSound() {
        if (enemyExplosionSound.isRunning()) {
            enemyExplosionSound.stop();
        }
        enemyExplosionSound.setFramePosition(0);
        enemyExplosionSound.start();
    }

    public void startGame() {
        gameStarted = true;
        gameOver = false;
        score = 0;
        level = 1;
        lives = 3;
        enemies.clear();
        bullets.clear();
        enemyBullets.clear();
        for (int i = 0; i < 5; i++) {
            int x = (int) (Math.random() * 500);
            int y = (int) (Math.random() * 300);
            enemies.add(new Enemy(x, y));
        }
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        drawPlayerRocket(g);
        drawEnemies(g);
        drawBullets(g);
        drawEnemyBullets(g);
        drawStats(g);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over", 350, 300);
        }
    }

    private void drawBackground(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }

    private void drawPlayerRocket(Graphics g) {
        g.drawImage(playerRocketImage, Player.x, Player.y, this);
    }

    private void drawEnemies(Graphics g) {
        for (Enemy enemy : enemies) {
            g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);
        }
    }

    private void drawBullets(Graphics g) {
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                g.setColor(Color.YELLOW);
                g.fillRect((int) bullet.getX(), (int) bullet.getY(), 2, 10); 
            }
        }
    }

    private void drawEnemyBullets(Graphics g) {
        for (EnemyBullet bullet : enemyBullets) {
            if (bullet.isActive()) {
                g.setColor(Color.RED);
                g.fillRect((int) bullet.getX(), (int) bullet.getY(), 2, 10); 
            }
        }
    }

    private void drawStats(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Level: " + level, 20, 60);
        g.drawString("Lives: " + lives, 20, 90);
    }

    private void moveBullets() {
        for (Bullet bullet : bullets) {
            bullet.move();
        }      
        bullets.removeIf(bullet -> !bullet.isActive());
    }

    private void moveEnemyBullets() {
        for (EnemyBullet bullet : enemyBullets) {
            bullet.move();
        } 
        enemyBullets.removeIf(bullet -> !bullet.isActive());
    }

    private void checkCollisions() {    
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            int enemyX = enemy.getX();
            int enemyY = enemy.getY();
            int enemyWidth = enemy.getWidth();
            int enemyHeight = enemy.getHeight();
            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                double bulletX = bullet.getX();
                double bulletY = bullet.getY();
                if (bulletX >= enemyX && bulletX <= enemyX + enemyWidth &&
                        bulletY >= enemyY && bulletY <= enemyY + enemyHeight) {                   
                    bulletIterator.remove();
                    enemyIterator.remove();
                    score += 10;
                    playEnemyExplosionSound();                   
                    if (enemies.isEmpty()) {
                        level++;
                        spawnNewEnemies();
                    }
                }
            }
        }

        for (EnemyBullet bullet : enemyBullets) {
            double bulletX = bullet.getX();
            double bulletY = bullet.getY();
            int playerX = Player.x;
            int playerY = Player.y;
            int playerWidth = playerRocketImage.getWidth(this);
            int playerHeight = playerRocketImage.getHeight(this);
            if (bulletX >= playerX && bulletX <= playerX + playerWidth &&
                    bulletY >= playerY && bulletY <= playerY + playerHeight) {            
                bullet.setActive(false);
                lives--;
                if (lives <= 0) {              
                    gameOver = true;
                }
            }
        }        
        for (Enemy enemy : enemies) {
            int enemyX = enemy.getX();
            int enemyY = enemy.getY();
            int enemyWidth = enemy.getWidth();
            int enemyHeight = enemy.getHeight();
            int playerX = Player.x;
            int playerY = Player.y;
            int playerWidth = playerRocketImage.getWidth(this);
            int playerHeight = playerRocketImage.getHeight(this);

            if (playerX + playerWidth >= enemyX && playerX <= enemyX + enemyWidth &&
                    playerY + playerHeight >= enemyY && playerY <= enemyY + enemyHeight) {                
                lives--;
                if (lives <= 0) {         
                    gameOver = true;
                } else {
                    Player.x = 500;
                }
            }
        }
    }

    private void spawnNewEnemies() {
        enemies.clear();
        for (int i = 0; i < 5 + level; i++) {
            int x = (int) (Math.random() * 500);
            int y = (int) (Math.random() * 300);
            enemies.add(new Enemy(x, y));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Space Invaders Game");
                SpaceInvadersGame gamePanel = new SpaceInvadersGame();
                frame.add(gamePanel);
                frame.setSize(1000, 600);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                gamePanel.startGame();
            }
        });
    }
}

class Player {
    static int x = 500;
    static int y = 500;
}

class Enemy {
    private int x, y;
    private Image image;
    private int width, height;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        ImageIcon icon = new ImageIcon("boss3.gif");
        this.image = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Adjust the size here
        this.width = 50; 
        this.height = 50; 
    }

    public void move() {
        y += 2;
        if (y > 600) {
            y = 0;
            x = (int) (Math.random() * 500);
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Image getImage() { return image; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
    
class Bullet {
    private double x, y;
    private boolean active;

    public Bullet(double x, double y) {
        this.x = x;
        this.y = y;
        this.active = true;
    }

    public void move() {
        y -= 10;
        if (y < 0) {
            active = false;
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isActive() { return active; }
}

class EnemyBullet {
    private double x, y;
    private boolean active;

    public EnemyBullet(double x, double y) {
        this.x = x;
        this.y = y;
        this.active = true;
    }

    public void move() {
        y += 10;
        if (y > 600) {
            active = false;
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
