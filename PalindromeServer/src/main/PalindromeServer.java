package palindrome.server;

import java.net.*;
import java.io.*;

public class PalindromeServer {
    static int clientCount = 0;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(1024);
            System.out.println("Сервер запущен и ожидает подключений...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientCount++;
                System.out.println("Клиент " + clientCount + " подключен");

                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead = input.read(buffer);
                String word = new String(buffer, 0, bytesRead, "UTF-8").trim();

                System.out.println("Получено слово: " + word);

                boolean isPalindrome = checkPalindrome(word);
                String response = isPalindrome ? "Это палиндром" : "Это не палиндром";

                output.write(response.getBytes("UTF-8"));
                System.out.println("Отправлен ответ: " + response);

                clientSocket.close();
                System.out.println("Клиент " + clientCount + " отключен");
            }
        } catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    private static boolean checkPalindrome(String word) {
        String cleanWord = word.replaceAll("\\s+", "").toLowerCase();
        int length = cleanWord.length();
        for (int i = 0; i < length / 2; i++) {
            if (cleanWord.charAt(i) != cleanWord.charAt(length - 1 - i)) {
                return false;
            }
        }
        return true;
    }
}