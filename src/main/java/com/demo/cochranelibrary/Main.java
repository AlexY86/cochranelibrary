package com.demo.cochranelibrary;

import java.util.Scanner;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import java.net.HttpURLConnection;

public class Main {
    public static void main(String[] args) throws IOException {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("You can choose from the following Topics");
            List<Topic> topicList = new TopicBuilder().getTopics();

            StringBuilder stringBuilder = new StringBuilder();
            int i = 0;
            for (Topic topica : topicList) {
                if (topicList.indexOf(topica) != 0) {
                    stringBuilder.append(" , ");
                }
                stringBuilder.append(i);
                stringBuilder.append(" - ");
                stringBuilder.append(topica.getTopic());
                i++;
            }
            System.out.println(stringBuilder.toString());

            int userChoiceNumber = -1;
            boolean checkInt = true;
            while (checkInt)
                try {
                    String userChoice = scanner.nextLine().trim();
                    userChoiceNumber = Integer.parseInt(userChoice);
                    checkInt = false;
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number");
                    System.out.println(stringBuilder.toString());
                }

            Topic topic = topicList.get(userChoiceNumber);
            System.out.println("Please enter the name of the file to save as");
            String fileName = scanner.nextLine().trim();

            String numberOfResults = "0";
            List<String> pageContent = new ArrayList<>();
            List<Article> articles = new ArrayList<>();

            HttpURLConnection urlConnection = (HttpURLConnection) topic.getUrl().openConnection();

            if (urlConnection.getResponseCode() == 200) {
                System.out.println("Connection Success " + topicList.get(userChoiceNumber).getTopic());
                InputStream stream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                String line = bufferedReader.readLine();
                while (line != null) {
                    if (line.contains("<span class=\"results-number\">")) {
                        String extractResults = line.substring(line.indexOf("<span class=\"results-number\">"));
                        numberOfResults = extractResults.substring(29, extractResults.indexOf("</span")).trim();
                    }
                    if (line.contains("<div class=\"search-results-section-body\">")) {
                        String[] stringArray = line.split("<div class=\"search-results-item-body\">");
                        for (String str : stringArray) {
                            pageContent.add(str);
                        }
                        pageContent.remove(0);

                        for (String str : pageContent) {
                            Article article = new Article();
                            article.setTopic(topic.getTopic());
                            String link = "https://www.cochranelibrary.com"
                                    + str.substring(str.indexOf("/cdsr"), str.indexOf("full")) + "full";
                            article.setUrl(link.trim());
                            String title = str.substring(str.indexOf("full") + 6, str.indexOf("</a>"));
                            article.setTitle(title.trim());
                            String authors = str.substring(str.indexOf("authors") + 15, str.indexOf("</div>"));
                            article.setAuthor(authors.trim());
                            String tempString = str.substring(str.indexOf("date") + 12);
                            String dateString = tempString.substring(0, tempString.indexOf("</div>"));
                            article.setDate(dateString.trim());
                            articles.add(article);
                        }
                    }
                    line = bufferedReader.readLine();
                }
            } else {
                System.out.println("Connection failed " + urlConnection.getResponseCode());
            }
            if (articles.size() == 0) {
                return;
            }
            FileWriter writer = new FileWriter(fileName + ".txt");
            writer.write("Number of Results: " + numberOfResults + " for Search Topic: "
                    + topicList.get(userChoiceNumber).getTopic());
            writer.write("\r\n");
            writer.write("\r\n");
            for (Article article : articles) {
                writer.write(article.getUrl() + " | " + article.getTopic() + " | " + article.getTitle() + " | "
                        + article.getAuthor() + " | " + article.getDate());
                writer.write("\r\n");
                writer.write("\r\n");
            }
            writer.close();
            System.out.println("text file: " + fileName + " created");
        }
    }

}
