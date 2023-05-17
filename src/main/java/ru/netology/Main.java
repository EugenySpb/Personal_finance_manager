package ru.netology;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final int PORT = 8989;
    static final String CATEGORIES_FILE = "categories.tsv";
    static final String OTHER_CATEGORY = "другое";
    static final String SUM = "sum";
    static final String CATEGORY = "category";

    public static void main(String[] args) {
        CategoryManager categoryManager = CategoryManager.loadData();
        if (categoryManager == null) {
            categoryManager = new CategoryManager(CATEGORIES_FILE, OTHER_CATEGORY);
        }
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);
            ObjectMapper mapper = new ObjectMapper();
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
                        System.out.println("Получен запрос: " + requestBody);
                        Buy buy = mapper.readValue(requestBody.toString(), Buy.class);

                        String title = buy.getTitle().toLowerCase();
                        String date = buy.getDate();
                        double sum = buy.getSum();

                        String category = categoryManager.getCategory(title);
                        categoryManager.addExpense(category, sum, date);
                        String maxCategory = categoryManager.getMaxCategory();
                        double maxSum = categoryManager.getMaxSum();
                        String maxYearCategory = categoryManager.getMaxYearCategory(date);
                        String maxMonthCategory = categoryManager.getMaxMonthCategory(date);
                        String maxDayCategory = categoryManager.getMaxDayCategory(date);


                        JsonObject result = new JsonObject();
                        JsonObject maxCategoryObject = new JsonObject();
                        JsonObject maxYearCategoryObject = new JsonObject();
                        JsonObject maxMonthCategoryObject = new JsonObject();
                        JsonObject maxDayCategoryObject = new JsonObject();

                        maxCategoryObject.addProperty(CATEGORY, maxCategory);
                        maxCategoryObject.addProperty(SUM, maxSum);
                        result.add("maxCategory", maxCategoryObject);

                        maxYearCategoryObject.addProperty(CATEGORY, maxYearCategory.split(": ")[1]);
                        maxYearCategoryObject.addProperty(SUM, maxYearCategory.split(": ")[0]);
                        result.add("maxYearCategory", maxYearCategoryObject);

                        maxMonthCategoryObject.addProperty(CATEGORY, maxMonthCategory.split(": ")[1]);
                        maxMonthCategoryObject.addProperty(SUM, maxMonthCategory.split(": ")[0]);
                        result.add("maxMonthCategory", maxMonthCategoryObject);

                        maxDayCategoryObject.addProperty(CATEGORY, maxDayCategory.split(":" )[1]);
                        maxDayCategoryObject.addProperty(SUM, maxDayCategory.split(": ")[0]);
                        result.add("maxDayCategory", maxDayCategoryObject);

                        categoryManager.saveData();

                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: application/json");
                        out.println();
                        out.println(result);

                        System.out.println("Отправлен ответ: " + result);
                        System.out.println();
                    }
                    categoryManager.saveData();
                } catch (IOException e) {
                    System.out.println("Ошибка при обработке подключения");
                    e.printStackTrace();
                }
            }
        } catch (
                IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }
    }
}