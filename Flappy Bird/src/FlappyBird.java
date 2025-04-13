import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    // Background
    int frameWidth = 360;
    int frameHeight = 640;
    Image backgroundImage;
    Image birdImage;
    Image lowerPipeImage;
    Image upperPipeImage;

    // player
    int playerStartPosX = frameWidth / 8;
    int playerStartPosY = frameHeight / 2;
    int playerWidth = 34;
    int playerHeight = 24;
    Player player;

    // pipes attributes
    int pipeStartPosX = frameWidth;
    int pipeStartPosY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;
    ArrayList<Pipe> pipes;

    // game logic
    Timer gameLoop;
    Timer pipesCooldown;
    int gravity = 1;
    int score = 0;
    boolean gameOver = false;
    boolean gameStarted = false;
    Font scoreFont = new Font("Arial", Font.BOLD, 32);

    // Start button
    JButton startButton;
    private JButton restartButton;
    private JButton exitButton;


    // constructor
    public FlappyBird() {
        setPreferredSize(new Dimension(frameWidth, frameHeight));
        setFocusable(true);
        setLayout(null);
        addKeyListener(this);

        // load images
        backgroundImage = new ImageIcon(getClass().getResource("/assets/background.png")).getImage();
        birdImage = new ImageIcon(getClass().getResource("/assets/bird.png")).getImage();
        lowerPipeImage = new ImageIcon(getClass().getResource("/assets/lowerPipe.png")).getImage();
        upperPipeImage = new ImageIcon(getClass().getResource("/assets/upperPipe.png")).getImage();

        player = new Player(playerStartPosX, playerStartPosY, playerWidth, playerHeight, birdImage);
        pipes = new ArrayList<>();

        pipesCooldown = new Timer(3500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameStarted && !gameOver) {
                    placePipes();
                }
            }
        });

        gameLoop = new Timer(1000 / 60, this);

        createStartButton();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, frameWidth, frameHeight, null);

        if (!gameStarted) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            String message = "Press Start to Begin";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(message);
            int textHeight = fm.getHeight();
            g.drawString(message, (frameWidth - textWidth) / 2, (frameHeight - textHeight) / 2 - 80);
            return;
        }

        g.drawImage(player.getImage(), player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight(), null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.getImage(), pipe.getPosX(), pipe.getPosY(), pipe.getWidth(), pipe.getHeight(), null);
        }

        g.setColor(Color.WHITE);
        g.setFont(scoreFont);
        g.drawString("Score: " + score, 10, 50);

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String msg = "Game Over";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(msg);
            int centerX = (frameWidth - textWidth) / 2;
            int centerY = frameHeight / 2;
            g.drawString(msg, centerX, centerY);

            // Tampilkan instruksi restart
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            String restartMsg = "Press 'R' to Restart";
            int restartTextWidth = g.getFontMetrics().stringWidth(restartMsg);
            g.drawString(restartMsg, (frameWidth - restartTextWidth) / 2, centerY + 70);

            showGameOverButtons();
        }
    }

    public void showGameOverButtons() {
        if (exitButton == null) {
            exitButton = new JButton("Exit");
            exitButton.setBounds((frameWidth - 100) / 2, frameHeight / 2 + 80, 100, 40);
            exitButton.setFont(new Font("Arial", Font.BOLD, 14));
            exitButton.setBackground(Color.RED);
            exitButton.setFocusPainted(false);
            exitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
        }

        add(exitButton);
        repaint();
    }


    public void restartGame() {
        score = 0;
        gameOver = false;
        pipes.clear();
        player.setPosY(frameHeight / 2);
        player.setVelocityY(0);
        repaint();
    }


    public void placePipes() {
        int randomPosY = (int) (pipeStartPosY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = frameHeight / 4;

        Pipe upperPipe = new Pipe(pipeStartPosX, randomPosY, pipeWidth, pipeHeight, upperPipeImage);
        pipes.add(upperPipe);

        Pipe lowerPipe = new Pipe(pipeStartPosX, (randomPosY + openingSpace + pipeHeight), pipeWidth, pipeHeight, lowerPipeImage);
        pipes.add(lowerPipe);
    }

    public void move() {
        if (!gameStarted || gameOver) return;

        player.setVelocityY(player.getVelocityY() + gravity);
        player.setPosY(player.getPosY() + player.getVelocityY());
        player.setPosY(Math.max(player.getPosY(), 0));

        for (Pipe pipe : pipes) {
            pipe.setPosX(pipe.getPosX() + pipe.getVelocityX());

            if (checkCollision(player, pipe)) {
                gameOver = true;
            }
        }

        for (int i = 0; i < pipes.size(); i += 2) {
            Pipe upperPipe = pipes.get(i);
            Pipe lowerPipe = pipes.get(i + 1);

            if (!upperPipe.isPassed() && upperPipe.getPosX() + pipeWidth < player.getPosX()) {
                upperPipe.setPassed(true);
                lowerPipe.setPassed(true);
                score++;
            }
        }

        // Game over if bird falls below screen
        if (player.getPosY() + player.getHeight() >= frameHeight) {
            gameOver = true;
        }
    }

    public boolean checkCollision(Player player, Pipe pipe) {
        Rectangle playerRect = new Rectangle(player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight());
        Rectangle pipeRect = new Rectangle(pipe.getPosX(), pipe.getPosY(), pipe.getWidth(), pipe.getHeight());
        return playerRect.intersects(pipeRect);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver && gameStarted) {
            player.setVelocityY(-10);
        }

        // Restart jika game over dan tekan 'R'
        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            restartGame(); // panggil fungsi restart
            if (exitButton != null) remove(exitButton);
            exitButton = null;

            gameStarted = true; // langsung mulai lagi
            gameLoop.start();
            pipesCooldown.start();
            repaint();
        }

    }


    @Override
    public void keyReleased(KeyEvent e) {}

    public void createStartButton() {
        startButton = new JButton("Start");
        int buttonWidth = 100;
        int buttonHeight = 50;
        startButton.setBounds((frameWidth - buttonWidth) / 2, (frameHeight - buttonHeight) / 2, buttonWidth, buttonHeight);
        startButton.setFont(new Font("Arial", Font.BOLD, 20));
        startButton.setBackground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameStarted = true;
                startButton.setVisible(false);
                gameLoop.start();
                pipesCooldown.start();
            }
        });
        add(startButton);
        startButton.setVisible(true);
    }
}
