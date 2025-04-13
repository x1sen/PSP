import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.*;

public class PalindromeClient extends Frame {
    private static final Logger logger = Logger.getLogger(PalindromeClient.class.getName());

    private final TextField ipField = new TextField("127.0.0.1", 15);
    private final TextField portField = new TextField("1024", 5);
    private final TextField wordField = new TextField(20);
    private final TextArea resultArea = new TextArea(10, 40);
    private final Button connectBtn = new Button("Подключиться");
    private final Button checkBtn = new Button("Проверить");
    private final Button disconnectBtn = new Button("Отключиться");

    private Socket socket;

    public static void main(String[] args) {
        new PalindromeClient();
    }

    public PalindromeClient() {
        configureWindow();
        setupEventHandlers();
    }

    private void configureWindow() {
        setTitle("Проверка палиндромов");
        setLayout(new FlowLayout());

        Panel topPanel = new Panel();
        topPanel.add(new Label("IP:"));
        topPanel.add(ipField);
        topPanel.add(new Label("Порт:"));
        topPanel.add(portField);
        topPanel.add(connectBtn);
        topPanel.add(disconnectBtn);
        add(topPanel);

        Panel midPanel = new Panel();
        midPanel.add(new Label("Слово:"));
        midPanel.add(wordField);
        midPanel.add(checkBtn);
        add(midPanel);

        add(resultArea);

        checkBtn.setEnabled(false);
        disconnectBtn.setEnabled(false);

        setSize(500, 300);
        setVisible(true);
    }

    private void setupEventHandlers() {
        connectBtn.addActionListener(this::handleConnect);
        checkBtn.addActionListener(this::handleCheck);
        disconnectBtn.addActionListener(this::handleDisconnect);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
                dispose();
            }
        });
    }

    private void handleConnect(ActionEvent event) {
        connect();
    }

    private void handleCheck(ActionEvent event) {
        checkWord();
    }

    private void handleDisconnect(ActionEvent event) {
        disconnect();
    }

    private void connect() {
        try {
            socket = new Socket(ipField.getText(), Integer.parseInt(portField.getText()));
            logger.info("Подключено к серверу " + ipField.getText() + ":" + portField.getText());

            resultArea.append("Подключено к серверу\n");
            connectBtn.setEnabled(false);
            checkBtn.setEnabled(true);
            disconnectBtn.setEnabled(true);

            readServerResponse();

        } catch (NumberFormatException e) {
            logger.warning("Неверный формат порта: " + portField.getText());
            resultArea.append("Ошибка: неверный формат порта\n");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка подключения", e);
            resultArea.append("Ошибка подключения: " + e.getMessage() + "\n");
        }
    }

    private void checkWord() {
        String word = wordField.getText().trim();
        if (word.isEmpty()) {
            logger.warning("Попытка проверки пустой строки");
            resultArea.append("Введите слово для проверки\n");
            return;
        }

        try {
            OutputStream out = socket.getOutputStream();
            out.write(word.getBytes(StandardCharsets.UTF_8));
            logger.info("Отправлено слово на проверку: " + word);

            readServerResponse();
            wordField.setText("");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при проверке слова", e);
            resultArea.append("Ошибка связи с сервером\n");
            disconnect();
        }
    }

    private void readServerResponse() throws IOException {
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = in.read(buffer);
        String response = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);

        logger.info("Получен ответ от сервера: " + response.trim());
        resultArea.append(response);
    }

    private void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                logger.info("Соединение с сервером закрыто");
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка при отключении", e);
        } finally {
            connectBtn.setEnabled(true);
            checkBtn.setEnabled(false);
            disconnectBtn.setEnabled(false);
            resultArea.append("Отключено от сервера\n");
        }
    }
}