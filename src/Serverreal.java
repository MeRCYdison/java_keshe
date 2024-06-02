import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

// 服务器主窗口类
class ServerOutline extends JFrame implements Runnable {
    private JTextArea jta = new JTextArea(); // 文本区域，用于显示信息
    private ServerSocket ss = null; // 服务器套接字
    private Socket s = null; // 客户端套接字
    public String[] words = new String[1000]; // 单词数组，暂未使用
    public ArrayList<String> readString = new ArrayList<>(); // 存储从文件读取的单词和翻译
    public ArrayList<String[]> wordandTranslations = new ArrayList<>(); // 存储单词和翻译的二维数组
    public ArrayList<String[]> candidateWords = new ArrayList<>(); // 用于存储备选单词

    private String word = null; // 当前选中的单词
    private String meaning = null; // 当前单词的意思
    double similarity;

    // 从文件中读取单词和翻译
    public boolean readWords() {
        try {
            File file = new File("src/WordList.txt");
            FileReader fr = new FileReader(file);
            BufferedReader brWords = new BufferedReader(fr);

            String str;
            while ((str = brWords.readLine()) != null) {
                readString.add(str);
            }
            fr.close();
            brWords.close();

            // 读取文件中的单词和翻译，存储到二维数组中
            for (String line : readString) {
                String[] wordTranslation = extractWordAndTranslation(line);
                if (wordTranslation != null) {
                    wordandTranslations.add(wordTranslation);
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 提取英文与翻译
    public String[] extractWordAndTranslation(String input) {
        //处理奇怪格式的txt文本
        input = input.trim();//移除input字符串的首尾空白字符
        input = input.replaceAll("\\s+", " ");//将input字符串中的所有连续的空白字符替换为一个空格。
        String[] parts = input.split(" ");
        if (parts.length >= 2) {
            String word = parts[0];
            String translation = parts[1];
            return new String[]{word, translation};
        } else {
            return null;
        }
    }

    // 获取随机单词及其翻译
    public String[] getRandomWordAndTranslation() {
        Random random = new Random();
        int index = random.nextInt(wordandTranslations.size());
        return wordandTranslations.get(index);
    }

    // 生成备选单词
    public void generateCandidateWords() {
        char[] correctWordChars = word.toCharArray();
        int correctWordLength = correctWordChars.length;


        // 使用一个二维数组，存储备选单词及其与正确答案的相似度
        ArrayList<String[]> similarWords = new ArrayList<>();

        // 遍历所有单词，计算相似度
        for (String[] candidateWordTranslation : wordandTranslations) {
            String candidateWord = candidateWordTranslation[0];

            // 排除随机单词本身
            if (candidateWord.equals(word)) {
                continue;
            }

            char[] candidateWordChars = candidateWord.toCharArray();
            int commonLettersCount = 0;

            // 计算候选单词与正确单词的相同字母数量
            for (char correctChar : correctWordChars) {
                for (char candidateChar : candidateWordChars) {
                    if (correctChar == candidateChar) {
                        commonLettersCount++;
                        break;
                    }
                }
            }
            int candidateWordLength = candidateWordChars.length;

            if (correctWordLength< candidateWordLength) {
                 similarity = (double) commonLettersCount / candidateWordLength;
            }else {
                 similarity = (double) commonLettersCount / correctWordLength;
                // 计算相似度
            }
            // 将备选单词及其相似度加入列表
            similarWords.add(new String[]{candidateWord, String.valueOf(similarity)});
        }

        // 按相似度排序
        similarWords.sort((a, b) -> Double.compare(Double.parseDouble(b[1]), Double.parseDouble(a[1])));

        // 选择最相似的3个备选单词,不使用选择相似度>0.3的单词
        for (int i = 0; i < similarWords.size(); i++) {
            //相似度<0.3的单词不考虑
//            if (Double.parseDouble(similarWords.get(i)[1]) < 0.3) {
//                continue;
//            }
            if (candidateWords.size() >= 3){
                break;
            }
            String[] candidateWord = similarWords.get(i);
            boolean isDuplicate = false;
            for (String[] existingCandidate : candidateWords) {
                if (existingCandidate[0].equals(candidateWord[0])) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                candidateWords.add(candidateWord);
                // 显示备选单词
                jta.append(candidateWord[0] + "\n" + candidateWord[1] + "\n");
            }
        }
    }

    // 构造函数
    ServerOutline() {
        try {
            readWords();
            this.setTitle("服务器");
            jta.setFont(new Font("SansSerif", Font.BOLD, 20));
            JScrollPane scrollPane = new JScrollPane(jta);
            this.add(scrollPane, BorderLayout.CENTER);
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.setSize(500, 400);
            this.setVisible(true);
            if (readWords()) {
                jta.append("单词列表已读取\n");
            }
            ss = new ServerSocket(6862);
            jta.append("服务器已启动,等待客户端连接\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 服务器线程的执行方法
    @Override
    public void run() {
        while (true) {
            try {
                s = ss.accept();
                jta.append("客户端请求单词\n");
                ServerThread st = new ServerThread(s);
                st.start();
            } catch (Exception e) {
                e.printStackTrace();
                jta.append("游戏异常退出\n");
            }
        }
    }

    // 服务器处理客户端请求的线程
    class ServerThread extends Thread {
        private Socket s = null;
        private PrintStream ps = null;
        private BufferedReader br = null;

        // 构造函数
        public ServerThread(Socket s) throws Exception {
            this.s = s;
            ps = new PrintStream(s.getOutputStream());
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        }

        // 线程执行的方法
        @Override
        public void run() {
            try {
                // 接收客户端的请求
                String request = br.readLine();
                if (request.equals("REQUEST_WORD_TRANSLATION")) {
                    // 获取随机单词和翻译，并存储备选单词
                    String[] randomWordTranslation = getRandomWordAndTranslation();
                    word = randomWordTranslation[0];
                    meaning = randomWordTranslation[1];
                    jta.append("随机单词：" + word + "\n");
                    jta.append("释义：" + meaning + "\n");

                    // 生成备选单词
                    jta.append("备选单词：\n");
                    generateCandidateWords();

                    // 构造要发送给客户端的字符串：单词、翻译、备选单词
                    StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append(word).append(" ").append(meaning).append(" ");
                    for (String[] candidate : candidateWords) {
                        responseBuilder.append(candidate[0]).append(" ");
                    }
                    String response = responseBuilder.toString();

                    // 发送响应给客户端
                    ps.println(response);
                    // 清空备选单词列表
                    candidateWords.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

// 服务器入口类
public class Serverreal {
    public static void main(String[] args) throws Exception {
        ServerOutline serverOutline = new ServerOutline();
        new Thread(serverOutline).start();
    }
}
