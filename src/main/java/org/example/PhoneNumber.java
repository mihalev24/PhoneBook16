package org.example;

import java.io.Serializable;

/**
 * Класс, представляющий номер телефона.
 * Каждый номер телефона имеет тип (например, мобильный или домашний) и сам номер.
 */
public class PhoneNumber implements Serializable {
    private final String number; // Номер телефона
    private final String type;   // Тип номера (сотовый, домашний и т.д.)

    /**
     * Конструктор для создания объекта номера телефона.
     *
     * @param number номер телефона
     * @param type тип номера (например, "мобильный", "домашний")
     */
    public PhoneNumber(String number, String type) {
        this.number = number;
        this.type = type;
    }

    /**
     * Получение номера телефона.
     *
     * @return номер телефона
     */
    public String getNumber() {
        return number;
    }

    /**
     * Переопределенный метод toString для удобного вывода информации о номере телефона.
     * Выводит тип номера и сам номер телефона.
     *
     * @return строковое представление номера телефона
     */
    @Override
    public String toString() {
        return type + ": " + number;
    }
}
