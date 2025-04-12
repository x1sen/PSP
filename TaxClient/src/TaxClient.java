import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Locale;
import java.util.logging.*;

public class TaxClient {
    private static final Logger logger = Logger.getLogger(TaxClient.class.getName());

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.OFF);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        logger.addHandler(handler);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);

        try (Scanner scanner = new Scanner(System.in)) {
            logger.info("Подключение к серверу...");

            try (Socket socket = new Socket("localhost", 2525);
                 ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

                logger.info("Подключение установлено");
                System.out.println("Введите зарплаты сотрудников (для выхода введите 0):");

                while (true) {
                    try {
                        System.out.print("Зарплата: ");
                        String input = scanner.nextLine().trim().replaceAll("\\s+", "");

                        if (input.isEmpty()) {
                            continue;
                        }

                        if (input.equalsIgnoreCase("0")) {
                            break;
                        }

                        try {
                            double salary = Double.parseDouble(input);
                            outputStream.writeDouble(salary);
                            outputStream.flush();

                            double tax = inputStream.readDouble();
                            System.out.printf("Сумма налога: %.2f руб.%n", tax);
                        } catch (NumberFormatException e) {
                            System.out.println("Ошибка: введите корректное число (например 1000000)");
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Ошибка связи с сервером", e);
                        break;
                    }
                }
            } catch (ConnectException e) {
                logger.log(Level.SEVERE, "Не удалось подключиться к серверу", e);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Ошибка ввода-вывода", e);
            }
        }
        logger.info("Работа завершена");
    }
}