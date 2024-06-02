import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

// 客户端界面类
class Clientoutline extends JFrame {

    // 连接服务按钮
    private JButton linkonbutton = new JButton("连接服务");
    // 显示文本区域
    private JTextArea jta = new JTextArea();
    private JTextArea fileContentArea = new JTextArea();

    // 引导标签
    private JLabel introLabel = new JLabel("这是一个六级单词强化记忆游戏");
    // 退出游戏按钮
    private JButton exitButton = new JButton("退出游戏");
    // 读取已掌握单词按钮
    private JButton readMasteredWordsButton = new JButton("读取已掌握单词");
    // 读取未掌握单词按钮
    private JButton readUnmasteredWordsButton = new JButton("读取未掌握单词");

    // 构造函数
    Clientoutline() {
        try {
            // 设置窗口属性
            this.setTitle("六级单词强化记忆");
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.setSize(1200, 800);
            this.setLocationRelativeTo(null);

            // 设置按钮和文本区域的字体
            linkonbutton.setFont(new Font("SansSerif", Font.PLAIN, 50));
            jta.setFont(new Font("SansSerif", Font.PLAIN, 50));
            introLabel.setFont(new Font("SansSerif", Font.BOLD, 60));
            exitButton.setFont(new Font("SansSerif", Font.PLAIN, 50));
            readMasteredWordsButton.setFont(new Font("SansSerif", Font.PLAIN, 50));
            readUnmasteredWordsButton.setFont(new Font("SansSerif", Font.PLAIN, 50));

            // 设置连接服务按钮和退出游戏按钮的尺寸
            linkonbutton.setPreferredSize(new Dimension(250, 150));
            exitButton.setPreferredSize(new Dimension(250, 150));

            // 使用网格包布局
            this.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.anchor = GridBagConstraints.NORTH;

            // 添加引导标签和连接服务按钮
            this.add(introLabel, gbc);
            gbc.anchor = GridBagConstraints.CENTER;
            this.add(linkonbutton, gbc);

            // 添加按钮，并设置间距
            gbc.insets = new Insets(50, 0, 0, 0);
            this.add(exitButton, gbc);
            this.add(readMasteredWordsButton, gbc);
            this.add(readUnmasteredWordsButton, gbc);

            // 设置窗口可见
            this.setVisible(true);

            // 连接服务按钮的事件监听器
            linkonbutton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose(); // 关闭当前窗口
                    new GameWindow().setVisible(true); // 打开游戏窗口
                }
            });

            // 退出游戏按钮的事件监听器
            exitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0); // 退出程序
                }
            });

            // 读取已掌握单词按钮的事件监听器
            readMasteredWordsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 创建文本区域和滚动窗格
                    JTextArea textArea = new JTextArea();
                    textArea.setFont(new Font("SansSerif", Font.PLAIN, 25));
                    String content = readFile("已掌握单词.txt");
                    textArea.setText(content);
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    JFrame frame = new JFrame();
                    frame.add(scrollPane, BorderLayout.CENTER);
                    frame.setSize(500, 400);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }
            });

            // 读取未掌握单词按钮的事件监听器
            readUnmasteredWordsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 创建文本区域和滚动窗格
                    JTextArea textArea = new JTextArea();
                    textArea.setFont(new Font("SansSerif", Font.PLAIN, 25));
                    String content = readFile("未掌握单词.txt");
                    textArea.setText(content);
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    JFrame frame = new JFrame();
                    frame.add(scrollPane, BorderLayout.CENTER);
                    frame.setSize(500, 400);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 读取文件内容的方法
    private String readFile(String fileName) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}

// 游戏窗口类
class GameWindow extends JFrame {
    // 游戏是否已开始
    private boolean gameStarted = false;
    // 开始游戏按钮
    private JButton startButton = new JButton("开始游戏");
    // 结束游戏按钮
    private JButton endButton = new JButton("结束游戏");
    // 用户输入答案的文本框
    private JTextField answerField;
    // 回答提示标签
    private JLabel responseLabel = new JLabel();
    // 分数标签
    private JLabel scoreLabel = new JLabel("Score: " + score);
    // 选项标签
    private JLabel label1 = new JLabel(" ");
    private JLabel label2 = new JLabel(" ");
    private JLabel label3 = new JLabel(" ");
    private JLabel label4 = new JLabel(" ");
    // 正确答案和翻译
    private String correctAnswer;
    private String translation;
    // 客户端线程对象
    public Clienthread ct;
    // 计时器
    private Timer timer1;
    // 文本框垂直位置
    private int yPosition = 0;
    // 分数
    private static int score = 10;
    // 边框
    public Border border = BorderFactory.createLineBorder(Color.BLACK);

    // 结束游戏方法
    public void finishbutton(){
        timer1.stop(); // 停止计时器
        dispose(); // 关闭当前窗口
        score = 10; // 重置分数
        ct.closeConnection(); // 关闭客户端连接
        new Clientoutline().setVisible(true); // 打开客户端界面
    }

    // 分数用完的对话框
    public void showScoreOverDialog() {
        JLabel messageLabel = new JLabel("分数用完了！点击按钮重新开始");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
        Object[] options = {"重新开始", "退出游戏"};
        int choice = JOptionPane.showOptionDialog(null, messageLabel, "分数用完了！", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        if (choice == JOptionPane.YES_OPTION) {
            finishbutton(); // 重新开始游戏
        } else {
            System.exit(0); // 退出游戏
        }
    }

    // 开始游戏方法
    private void startGame() {
        try {
            gameStarted = true;
            scoreLabel.setText("Score: " + score);
            ct = new Clienthread();
            ct.requestWordAndTranslation();
            String[] response = ct.getResponse();
            responseLabel.setText(response[1]);
            List<JLabel> labels = Arrays.asList(label1, label2, label3, label4);
            List<String> words = Arrays.asList(response[0], response[2], response[3], response[4]);
            Collections.shuffle(words);
            for (int i = 0; i < labels.size(); i++) {
                labels.get(i).setText(words.get(i));
            }
            correctAnswer = response[0];
            translation = response[1];
            yPosition = 0;
            timer1.start();
            answerField.setText("");
            startButton.setVisible(false);
            endButton.setVisible(true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private boolean wordExistsInFile(String word, String fileName) {
            String fileContent = readFile(fileName);
            System.out.println("文件"+fileName+"中存在单词"+word);
            return fileContent.contains(word + " ");
        }

    // 将单词写入文件的方法
    private void writeWordToFile(String word, String translation, String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            if (wordExistsInFile(word, fileName)) {
                return;
            }

            FileWriter writer = new FileWriter(fileName, true);
            writer.write(word + " " + translation + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 将单词移除文件的方法
    // 将单词从文件中移除的方法
    private void removeWordFromFile(String word, String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                return;
            }

            File tempFile = new File("temp.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(word + " ")) {
                    writer.write(line + System.getProperty("line.separator"));
                }
            }

            reader.close();
            writer.close();

            if (!file.delete()) {
                System.out.println("无法删除文件");
                return;
            }

            if (!tempFile.renameTo(file)) {
                System.out.println("无法重命名文件");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFile(String fileName) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }


    // 构造函数
    GameWindow() {
        // 设置窗口属性
        this.setTitle("游戏窗口");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(1200, 1000);
        this.setLocationRelativeTo(null);

        // 设置按钮和标签的字体
        startButton.setFont(new Font("SansSerif", Font.PLAIN, 50));
        endButton.setFont(new Font("SansSerif", Font.PLAIN, 50));
        endButton.setVisible(false);
        responseLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
        responseLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 创建面板和添加组件
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(endButton);

        JPanel responsePanel = new JPanel();
        responsePanel.add(responseLabel);
        responsePanel.setBorder(border);
        responsePanel.setPreferredSize(new Dimension(1200, 600));

        JPanel choosePanel = new JPanel(new GridLayout(2, 2));
        label1.setBorder(border);label1.setFont(new Font("SansSerif", Font.PLAIN, 30));label1.setHorizontalAlignment(JLabel.CENTER);
        label2.setBorder(border);label2.setFont(new Font("SansSerif", Font.PLAIN, 30));label2.setHorizontalAlignment(JLabel.CENTER);
        label3.setBorder(border);label3.setFont(new Font("SansSerif", Font.PLAIN, 30));label3.setHorizontalAlignment(JLabel.CENTER);
        label4.setBorder(border);label4.setFont(new Font("SansSerif", Font.PLAIN, 30));label4.setHorizontalAlignment(JLabel.CENTER);
        choosePanel.add(label1);
        choosePanel.add(label2);
        choosePanel.add(label3);
        choosePanel.add(label4);

        JPanel answerPanel = new JPanel(new FlowLayout());
        answerField = new JTextField();
        answerField.setPreferredSize(new Dimension(200, 40));
        answerField.setFont(new Font("SansSerif", Font.PLAIN, 30));
        JButton confirmButton = new JButton("确认答案");
        confirmButton.setFont(new Font("SansSerif", Font.PLAIN, 22));
        answerPanel.add(answerField);
        answerPanel.add(confirmButton);

        JPanel scorePanel = new JPanel(new FlowLayout());
        scoreLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
        scorePanel.add(scoreLabel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(responsePanel, BorderLayout.CENTER);
        mainPanel.add(choosePanel, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(answerPanel, BorderLayout.CENTER);
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(scorePanel, BorderLayout.SOUTH);

        // 创建计时器
        timer1 = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                yPosition += 2;
                responseLabel.setLocation(responseLabel.getX(), yPosition);

                if (responseLabel.getY() + responseLabel.getHeight() >= responsePanel.getHeight()) {
                    timer1.stop();
                    score--;
                    scoreLabel.setText("Score: " + score);
                    JLabel timeoutmessageLabel = new JLabel("您未回答！正确答案为" + correctAnswer);
                    timeoutmessageLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
                    JOptionPane.showMessageDialog(null, timeoutmessageLabel, "结果", JOptionPane.ERROR_MESSAGE);
                    writeWordToFile(correctAnswer, translation, "未掌握单词.txt");
                    removeWordFromFile(correctAnswer, "已掌握单词.txt");
                    if (score <= 0) {
                        showScoreOverDialog();
                    } else {
                        startGame();
                    }
                }
            }
        });

        this.setVisible(true);
        // 开始游戏按钮的事件监听器
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        // 结束游戏按钮的事件监听器
        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameStarted = false;
                finishbutton();
            }
        });

        // 确认答案按钮的事件监听器
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameStarted) {
                    String userAnswer = answerField.getText();
                    if (userAnswer.equals(correctAnswer)) {
                        timer1.stop();
                        score++;
                        scoreLabel.setText("Score: " + score);
                        JLabel correctmessageLabel = new JLabel("答案正确！");
                        correctmessageLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
                        JOptionPane.showMessageDialog(null, correctmessageLabel, "回答正确", JOptionPane.INFORMATION_MESSAGE);
                        writeWordToFile(correctAnswer, translation, "已掌握单词.txt");
                        removeWordFromFile(correctAnswer, "未掌握单词.txt");
                        startGame();
                    } else {
                        timer1.stop();
                        score -= 2;
                        scoreLabel.setText("Score: " + score);
                        JLabel wrongmessageLabel = new JLabel("答案错误！正确答案为" + correctAnswer);
                        wrongmessageLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
                        JOptionPane.showMessageDialog(null, wrongmessageLabel, "回答错误", JOptionPane.ERROR_MESSAGE);
                        writeWordToFile(correctAnswer, translation, "未掌握单词.txt");
                        removeWordFromFile(correctAnswer, "已掌握单词.txt");
                        if (score <= 0) {
                            showScoreOverDialog();
                        } else {
                            startGame();
                        }
                    }
                }
            }
        });

        // 文本框键盘监听器，按下回车确认答案
        answerField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameStarted && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String userAnswer = answerField.getText();
                    if (userAnswer.equals(correctAnswer)) {
                        timer1.stop();
                        score++;
                        scoreLabel.setText("Score: " + score);
                        JLabel correctmessageLabel = new JLabel("答案正确！");
                        correctmessageLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
                        JOptionPane.showMessageDialog(null, correctmessageLabel, "回答正确", JOptionPane.INFORMATION_MESSAGE);
                        writeWordToFile(correctAnswer, translation, "已掌握单词.txt");
                        removeWordFromFile(correctAnswer, "未掌握单词.txt");
                        startGame();
                    } else {
                        timer1.stop();
                        score -= 2;
                        scoreLabel.setText("Score: " + score);
                        JLabel wrongmessageLabel = new JLabel("答案错误！正确答案为" + correctAnswer);
                        wrongmessageLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
                        JOptionPane.showMessageDialog(null, wrongmessageLabel, "回答错误", JOptionPane.ERROR_MESSAGE);
                        writeWordToFile(correctAnswer, translation, "未掌握单词.txt");
                        removeWordFromFile(correctAnswer, "已掌握单词.txt");

                        if (score <= 0) {
                            showScoreOverDialog();
                        } else {
                            startGame();
                        }
                    }
                }
            }
        });
    }
}

// 客户端线程类
class Clienthread extends Thread {
    private Socket s = null;
    private PrintStream ps = null;
    private BufferedReader br = null;

    // 构造函数，连接服务器
    public Clienthread() throws Exception {
        s = new Socket("localhost", 6862);
        ps = new PrintStream(s.getOutputStream());
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    // 请求单词和翻译
    public void requestWordAndTranslation() {
        try {
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.println("REQUEST_WORD_TRANSLATION");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获取服务器响应
    public String[] getResponse() {
        try {
            String response = br.readLine();
            String[] responseArray = response.split(" ", 5);
            return responseArray;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 关闭连接
    public void closeConnection() {
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 线程运行方法
    @Override
    public void run() {
        try {
            String response = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// 主类
public class Clientreal {
    public static void main(String[] args) throws Exception {
        Clientoutline Clientoutline = new Clientoutline();
    }
}


