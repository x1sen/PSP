import java.io.*;
import java.net.*;
import java.util.logging.*;

public class TaxServer {
    private static final Logger logger = Logger.getLogger(TaxServer.class.getName());

    public static void main(String[] args) {

        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.OFF);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        logger.addHandler(handler);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);

        try (ServerSocket serverSocket = new ServerSocket(2525)) {
            logger.info("Сервер запущен и ожидает подключений...");

            while (!Thread.currentThread().isInterrupted()) {
                try (Socket clientSocket = serverSocket.accept();
                     ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                     ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream())) {

                    logger.info("Клиент подключен: " + clientSocket.getInetAddress());

                    while (true) {
                        try {
                            double salary = inputStream.readDouble();
                            logger.info("Получена зарплата: " + salary);

                            double tax = calculateTax(salary);
                            logger.info("Рассчитанный налог: " + tax);

                            outputStream.writeDouble(tax);
                            outputStream.flush();
                        } catch (EOFException e) {
                            logger.info("Клиент отключился");
                            break;
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "Ошибка связи с клиентом", e);
                            break;
                        }
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Ошибка при работе с клиентом", e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при запуске сервера", e);
        }
    }

    private static double calculateTax(double salary) {
        if (salary < 100_000) {
            return salary * 0.05;
        } else if (salary <= 500_000) {
            return salary * 0.10;
        } else {
            return salary * 0.15;
        }
    }
}