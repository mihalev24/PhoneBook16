package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс для работы с контатками.
 * Включает методы для сохранения и удаления контактов.
 */
public record ContactManager(List<Contact> contacts) {

    // Логгер Log4j
    private static final Logger logger = LogManager.getLogger(ContactManager.class);

    /**
     * Конструктор для инициализации списка контактов.
     *
     * @param contacts список контактов
     */
    public ContactManager {
        logger.info("Инициализирован менеджер контактов с {} контактами.", contacts.size());
    }

    /**
     * Метод для добавления нового контакта.
     * Проверяет корректность ФИО и номера телефона перед добавлением контакта.
     *
     * @param fullName   полное имя контакта
     * @param phoneNumber номер телефона контакта
     * @param phoneType  тип номера телефона
     * @throws IllegalArgumentException если ФИО или номер телефона некорректны
     */
    public void addContact(String fullName, String phoneNumber, String phoneType) {
        logger.info("Попытка добавить контакт: {} с номером {} ({})", fullName, phoneNumber, phoneType);

        try {
            validateFullName(fullName);
            validatePhoneNumber(phoneNumber);

            Contact contact = new Contact(fullName);
            contact.addPhoneNumber(phoneNumber, phoneType);
            contacts.add(contact);

            logger.info("Контакт добавлен успешно: {}", fullName);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при добавлении контакта: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Метод для добавления нового номера телефона к существующему контакту.
     * Проверяет корректность номера телефона перед добавлением.
     *
     * @param contact    контакт, к которому добавляется номер
     * @param phoneNumber новый номер телефона
     * @param phoneType  тип нового номера телефона
     * @throws IllegalArgumentException если номер телефона некорректен
     */
    public void addPhoneNumber(Contact contact, String phoneNumber, String phoneType) {
        logger.info("Попытка добавить номер {} ({}) к контакту {}", phoneNumber, phoneType, contact.getFullName());

        try {
            validatePhoneNumber(phoneNumber);
            contact.addPhoneNumber(phoneNumber, phoneType);

            logger.info("Номер {} ({}) успешно добавлен к контакту {}", phoneNumber, phoneType, contact.getFullName());
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при добавлении номера телефона: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Метод для удаления контакта.
     * Удаляет контакт по полному имени.
     *
     * @param fullName полное имя контакта для удаления
     */
    public void deleteContact(String fullName) {
        logger.info("Попытка удалить контакт с именем: {}", fullName);

        boolean removed = contacts.removeIf(contact -> contact.getFullName().equals(fullName));
        if (removed) {
            logger.info("Контакт удалён: {}", fullName);
        } else {
            logger.warn("Контакт с именем {} не найден для удаления.", fullName);
        }
    }

    /**
     * Метод для проверки ФИО на пустоту.
     * Генерирует исключение, если ФИО пустое.
     *
     * @param fullName полное имя контакта
     * @throws IllegalArgumentException если ФИО пустое
     */
    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            logger.error("ФИО не может быть пустым.");
            throw new IllegalArgumentException("ФИО не может быть пустым.");
        }
    }

    /**
     * Метод для проверки номера телефона на корректность.
     * Генерирует исключение, если номер телефона содержит некорректные символы.
     *
     * @param number номер телефона
     * @throws IllegalArgumentException если номер телефона некорректен
     */
    private void validatePhoneNumber(String number) {
        if (number == null || !number.matches("[0-9+()\\- *#]+")) {
            logger.error("Номер телефона некорректен: {}", number);
            throw new IllegalArgumentException("Номер телефона может содержать только цифры, пробелы, +, (), #, *");
        }
    }

    /**
     * Метод для получения отсортированного списка контактов по алфавиту.
     * Сортирует контакты по ФИО без учета регистра и группирует по первой букве ФИО.
     *
     * @return отсортированный и сгруппированный список контактов
     */
    public List<String> getSortedContactDisplayList() {
        logger.info("Получение отсортированного списка контактов...");
        List<String> sortedContacts = contacts.stream()
                .sorted(Comparator.comparing(Contact::getFullName, String::compareToIgnoreCase))
                .collect(Collectors.groupingBy(
                        contact -> contact.getFullName().substring(0, 1).toUpperCase(),
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet()
                .stream()
                .flatMap(entry -> Stream.concat(
                        Stream.of(entry.getKey()),
                        entry.getValue().stream().map(Contact::getFullName)
                ))
                .collect(Collectors.toList());

        logger.info("Список контактов отсортирован. Количество групп: {}", sortedContacts.size());
        return sortedContacts;
    }

    /**
     * Метод для фильтрации контактов по имени или номеру.
     * Возвращает список ФИО контактов, которые соответствуют строке поиска.
     *
     * @param searchText строка поиска
     * @return список ФИО контактов, которые соответствуют строке поиска
     */
    public List<String> filterContacts(String searchText) {
        logger.info("Фильтрация контактов по строке: {}", searchText);
        List<String> filteredContacts = contacts.stream()
                .filter(contact -> contact.getFullName().toLowerCase().contains(searchText.toLowerCase())
                        || contact.getPhoneNumbers().stream()
                        .anyMatch(p -> p.getNumber().contains(searchText)))
                .map(Contact::getFullName)
                .collect(Collectors.toList());

        logger.info("Фильтрация завершена. Найдено контактов: {}", filteredContacts.size());
        return filteredContacts;
    }

    /**
     * Метод для сохранения контактов в данные.
     * Сохраняет текущий список контактов.
     */
    public void saveContacts() {
        logger.info("Сохранение контактов...");
        PhoneBookData.saveData(contacts);
        logger.info("Контакты успешно сохранены.");
    }
}
