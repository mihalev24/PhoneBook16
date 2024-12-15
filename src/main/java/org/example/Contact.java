package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий контакт в телефонной книге.
 * Контакт состоит из ФИО и списка номеров телефонов.
 */
public class Contact implements Serializable {
    private final String fullName; // ФИО контакта
    private final List<PhoneNumber> phoneNumbers; // Список номеров телефонов контакта

    /**
     * Конструктор для создания контакта с ФИО.
     * Инициализирует список номеров телефонов.
     *
     * @param fullName полное имя контакта
     */
    public Contact(String fullName) {
        this.fullName = fullName;
        this.phoneNumbers = new ArrayList<>(); // Инициализация списка номеров телефонов
    }

    /**
     * Получение ФИО контакта.
     *
     * @return полное имя контакта
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Получение списка номеров телефонов контакта.
     *
     * @return список номеров телефонов
     */
    public List<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    /**
     * Добавление нового номера телефона для контакта.
     *
     * @param number номер телефона
     * @param type тип номера (например, "мобильный", "домашний")
     */
    public void addPhoneNumber(String number, String type) {
        // Добавляем новый номер в список
        phoneNumbers.add(new PhoneNumber(number, type));
    }

    /**
     * Переопределенный метод toString для удобного вывода информации о контакте.
     * Выводит ФИО контакта и список номеров телефонов.
     *
     * @return строковое представление контакта
     */
    @Override
    public String toString() {
        return fullName + " - " + phoneNumbers; // Вывод ФИО и номеров телефона
    }
}
