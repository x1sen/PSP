import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.logging.*;

public class UDPServer {
    private static final int PORT = 9876;
    private static final String LOG_FILE = "calculations.log";
    private static final double MAX_Z = Math.pow(15, 1.0/5.0);
    private static final Logger logger = Logger.getLogger(UDPServer.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            logger.info("Сервер запущен на порту " + PORT);
            logger.info("Максимальное значение z: " + String.format(Locale.US, "%.5f", MAX_Z));

            byte[] receiveData = new byte[1024];

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    try {
                        ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData());
                        double x = buffer.getDouble();
                        double y = buffer.getDouble();
                        double z = buffer.getDouble();

                        if (Math.pow(z, 5) >= 15) {
                            String errorMsg = "Ошибка: z^5 должно быть < 15 (z < " +
                                    String.format(Locale.US, "%.5f", MAX_Z) + ")";
                            logger.warning(String.format(
                                    "Отклонено: x=%.2f, y=%.2f, z=%.2f - %s",
                                    x, y, z, errorMsg));
                            sendErrorResponse(serverSocket, receivePacket, errorMsg);
                            continue;
                        }

                        double result = calculateFunction(x, y, z);
                        logCalculation(x, y, z, result);

                        ByteBuffer responseBuffer = ByteBuffer.allocate(Double.BYTES);
                        responseBuffer.putDouble(result);
                        byte[] sendData = responseBuffer.array();

                        sendResponse(serverSocket, receivePacket, sendData);

                        logger.info(String.format(
                                "Обработаны данные: x=%.6f, y=%.6f, z=%.6f, результат=%.6f",
                                x, y, z, result));

                    } catch (BufferUnderflowException e) {
                        logger.warning("Некорректный формат данных от клиента");
                        sendErrorResponse(serverSocket, receivePacket,
                                "Ошибка: некорректный формат данных");
                    } catch (IllegalArgumentException e) {
                        logger.warning(e.getMessage());
                        sendErrorResponse(serverSocket, receivePacket, e.getMessage());
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Ошибка обработки запроса", e);
                }
            }
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Ошибка создания сокета", e);
        }
    }

    private static void sendResponse(DatagramSocket socket,
                                     DatagramPacket receivedPacket, byte[] responseData) throws IOException {
        InetAddress clientAddress = receivedPacket.getAddress();
        int clientPort = receivedPacket.getPort();
        DatagramPacket sendPacket = new DatagramPacket(
                responseData, responseData.length, clientAddress, clientPort);
        socket.send(sendPacket);
    }

    private static void sendErrorResponse(DatagramSocket socket,
                                          DatagramPacket receivedPacket, String error) throws IOException {
        byte[] errorData = error.getBytes();
        sendResponse(socket, receivedPacket, errorData);
    }

    private static double calculateFunction(double x, double y, double z) {
        double numerator = 2 * Math.cos(x - Math.PI/6);
        double denominator = Math.exp(0.5) + Math.pow(Math.sin(y), 2);

        if (Math.abs(denominator) < 1e-10) {
            throw new IllegalArgumentException("Ошибка: знаменатель слишком мал");
        }

        double zPart = (3 - Math.pow(z, 5)/5);
        if (Math.abs(zPart) < 1e-10) {
            throw new IllegalArgumentException("Ошибка: z^5 слишком близко к 15");
        }

        return (numerator / denominator) * (1 + Math.pow(z, 2)/zPart);
    }

    private static void logCalculation(double x, double y, double z, double result) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.printf("%s: x=%.6f, y=%.6f, z=%.6f, σ=%.6f%n",
                    new Date(), x, y, z, result);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка записи в файл", e);
        }
    }
}