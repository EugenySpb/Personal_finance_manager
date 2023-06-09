package ru.netology.service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int PORT = 8989;
    static BuyProcessor processor;
    public void start () {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server running on port " + PORT);
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                    String request;

                    while ((request = in.readLine()) != null) {
                        if (request.isEmpty()) {
                            break;
                        }
                    }
                    // Читаем тело запроса
                    StringBuilder requestBody = new StringBuilder();
                    while (in.ready()) {
                        requestBody.append((char) in.read());
                    }

                    if (!requestBody.toString().equals("")) {
                        if (processor == null) {
                            processor = new BuyProcessor();  // Создание экземпляра BuyProcessor
                        }
                        processor.process(requestBody, out);
                    }

                } catch (IOException e) {
                    System.out.println("Connection processing error");
                    e.printStackTrace();
                }
            }
        } catch (
                IOException e) {
            System.out.println("Can't start server");
            e.printStackTrace();
        }
    }
}
