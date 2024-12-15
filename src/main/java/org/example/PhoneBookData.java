package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для работы с данными телефонной книги.
 * Включает методы для сохранения и загрузки списка контактов в/из файла.
 */
public class PhoneBookData {
    // Константа для имени файла, в котором будут храниться данные
    private static final String FILE_NAME = "phonebook.dat";

    /**
     * Метод для сохранения данных о контактах в файл.
     * Этот метод записывает список контактов в файл в сериализованном виде.
     *
     * @param contacts список контактов, которые нужно сохранить
     */
    public static void saveData(List<Contact> contacts) {
        // Попытка записать данные в файл
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            // Записываем список контактов в файл
            oos.writeObject(contacts);
        } catch (IOException e) {
            // Если возникает ошибка, выводим стек трейс
            e.printStackTrace();
        }
    }

    /**
     * Метод для загрузки данных из файла.
     * Этот метод загружает список контактов из файла, если файл существует и данные корректны.
     *
     * @return список контактов, загруженных из файла, или пустой список, если файл не существует или произошла ошибка
     */
    @SuppressWarnings("unchecked")
    public static List<Contact> loadData() {
        // Создаем объект для работы с файлом
        File file = new File(FILE_NAME);

        // Если файл не существует, возвращаем пустой список
        if (!file.exists()) {
            return new ArrayList<>();
        }

        // Попытка загрузить данные из файла
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            // Читаем объект из файла и приводим его к типу List<Contact>
            return (List<Contact>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Если возникает ошибка при чтении или если класс не найден, выводим стек трейс
            e.printStackTrace();
            // Возвращаем пустой список в случае ошибки
            return new ArrayList<>();
        }
    }
}
