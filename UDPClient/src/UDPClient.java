import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.text.*;
import java.util.logging.*;

public class UDPClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 9876;
    private static final double MAX_Z = Math.pow(15, 1.0/5.0);
    private static final Logger logger = Logger.getLogger(UDPClient.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }

    public static void main(String[] args) {
        logger.info("Максимальное значение z: " + String.format(Locale.US, "%.5f", MAX_Z));
        logger.info("(z^5 должно быть меньше 15)\n");
        logger.info("Форматы чисел: 12.34, 12,34, 1/9\n");

        try (DatagramSocket clientSocket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);

            while (true) {
                try {
                    System.out.println("Введите параметры x y z (через пробел) или 'exit' для выхода:");
                    String input = scanner.nextLine().trim();

                    if ("exit".equalsIgnoreCase(input)) {
                        break;
                    }


                    input = input.replace(',', '.');
                    input = input.replaceAll("[^0-9./-]", " ").replaceAll("\\s+", " ").trim();
                    String[] parts = input.split(" ");

                    if (parts.length != 3) {
                        logger.warning("Ошибка: требуется ровно 3 числа, разделенных пробелами");
                        continue;
                    }


                    double x = parseNumber(parts[0]);
                    double y = parseNumber(parts[1]);
                    double z = parseNumber(parts[2]);


                    if (z >= MAX_Z) {
                        logger.warning(String.format(
                                "Ошибка: z = %.5f (должно быть < %.5f)", z, MAX_Z));
                        logger.warning(String.format(
                                "z^5 = %.2f (должно быть < 15)\n", Math.pow(z, 5)));
                        continue;
                    }

                    ByteBuffer buffer = ByteBuffer.allocate(3 * Double.BYTES);
                    buffer.putDouble(x);
                    buffer.putDouble(y);
                    buffer.putDouble(z);

                    DatagramPacket sendPacket = new DatagramPacket(
                            buffer.array(), buffer.array().length, serverAddress, SERVER_PORT);
                    clientSocket.send(sendPacket);

                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);

                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    if (response.startsWith("Ошибка:")) {
                        logger.warning("Сервер сообщил: " + response + "\n");
                    } else {
                        try {
                            double result = ByteBuffer.wrap(receivePacket.getData()).getDouble();
                            System.out.printf(Locale.US, "Результат: σ = %.6f%n%n", result);
                        } catch (BufferUnderflowException e) {
                            logger.warning("Некорректный ответ от сервера: " + response);
                        }
                    }

                } catch (NumberFormatException | ParseException e) {
                    logger.warning("Ошибка: введите три числа в формате 12.34, 12,34 или 1/9\n");
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Ошибка связи с сервером", e);
                }
            }
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Не удалось найти сервер", e);
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Ошибка создания сокета", e);
        }
    }

    private static double parseNumber(String numStr) throws NumberFormatException, ParseException {
        if (numStr.contains("/")) {
            String[] fraction = numStr.split("/");
            if (fraction.length != 2) {
                throw new NumberFormatException("Некорректный формат дроби");
            }
            double numerator = Double.parseDouble(fraction[0]);
            double denominator = Double.parseDouble(fraction[1]);
            if (denominator == 0) {
                throw new NumberFormatException("Деление на ноль");
            }
            return numerator / denominator;
        }
        return NumberFormat.getInstance(Locale.US).parse(numStr).doubleValue();
    }
}