package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Главный класс приложения для телефонного справочника.
 * Реализует графический интерфейс с помощью JavaFX.
 */
public class PhoneBookApp extends Application {
    // Инициализация логгера
    private static final Logger logger = LogManager.getLogger(PhoneBookApp.class);

    private ContactManager contactManager; // Менеджер контактов
    private ListView<String> contactListView; // Список контактов
    private VBox detailsBox; // Панель для отображения деталей контакта
    private Contact selectedContact; // Выбранный контакт
    private TextField searchField; // Поле для поиска
    private double scale = 1.0; // Масштаб для увеличения/уменьшения шрифта

    /**
     * Инициализация графического интерфейса и загрузка данных.
     *
     * @param primaryStage Основная сцена приложения.
     */
    @Override
    public void start(Stage primaryStage) {
        contactManager = new ContactManager(PhoneBookData.loadData()); // Загрузка данных
        setupUI(primaryStage); // Настройка пользовательского интерфейса
        updateContactListView(); // Обновление списка контактов
    }

    /**
     * Настройка пользовательского интерфейса.
     *
     * @param primaryStage Основная сцена приложения.
     */
    private void setupUI(Stage primaryStage) {
        searchField = createSearchField(); // Создание поля для поиска

        // Создание кнопок
        Button addButton = new Button("Добавить контакт");
        Button deleteButton = new Button("Удалить контакт");
        Button showAllButton = new Button("Показать все");
        Button zoomInButton = new Button("+");
        Button zoomOutButton = new Button("-");

        // Создание верхней панели с кнопками
        HBox topBar = createTopBar(addButton, deleteButton, showAllButton, zoomInButton, zoomOutButton);

        contactListView = new ListView<>(); // Список для отображения контактов
        contactListView.setOnMouseClicked(e -> showContactDetails()); // Обработчик для отображения деталей контакта

        detailsBox = new VBox(10); // Панель для отображения деталей контакта
        detailsBox.setPadding(new Insets(10));

        BorderPane root = new BorderPane(); // Основной контейнер
        root.setTop(topBar); // Верхняя панель
        root.setLeft(contactListView); // Список контактов слева
        root.setCenter(detailsBox); // Панель для деталей контакта по центру

        // Обработчики событий для кнопок
        addButton.setOnAction(e -> addContactDialog());
        deleteButton.setOnAction(e -> deleteContact());
        zoomInButton.setOnAction(e -> changeZoom(0.1));
        zoomOutButton.setOnAction(e -> changeZoom(-0.1));
        showAllButton.setOnAction(e -> showAllContacts());

        // Обработчик события для изменения масштаба с помощью клавиш
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleZoom);

        Scene scene = new Scene(root, 700, 500); // Создание сцены
        primaryStage.setTitle("Телефонный справочник"); // Установка заголовка окна
        primaryStage.setScene(scene); // Установка сцены в окно
        primaryStage.show(); // Отображение окна
    }

    /**
     * Создание поля для поиска.
     *
     * @return Поле для поиска.
     */
    private TextField createSearchField() {
        TextField searchField = new TextField();
        searchField.setPromptText("Поиск по имени или номеру...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterContacts());
        return searchField;
    }

    /**
     * Создание верхней панели с кнопками.
     *
     * @param buttons Кнопки для панели.
     * @return Контейнер с кнопками.
     */
    private HBox createTopBar(Button... buttons) {
        HBox topBar = new HBox(10, searchField);
        topBar.getChildren().addAll(buttons);
        topBar.setPadding(new Insets(10));
        return topBar;
    }

    /**
     * Отображение всех контактов.
     */
    private void showAllContacts() {
        logger.info("Показаны все контакты"); // Логируем отображение всех контактов
        searchField.clear(); // Очистка поля поиска
        updateContactListView(); // Обновление списка контактов
    }

    /**
     * Обновление списка контактов в ListView.
     */
    private void updateContactListView() {
        logger.debug("Обновление списка контактов"); // Логируем обновление списка
        contactListView.getItems().setAll(contactManager.getSortedContactDisplayList());
    }

    /**
     * Показать детали выбранного контакта.
     */
    private void showContactDetails() {
        selectedContact = getSelectedContact(); // Получение выбранного контакта
        if (selectedContact == null) {
            logger.warn("Контакт не выбран!"); // Логируем, если контакт не выбран
            return;
        }

        clearDetailsBox(); // Очистка панели с деталями
        addContactLabels(); // Добавление меток с данными контакта
        addAddNumberButton(); // Добавление кнопки для добавления номера
    }

    /**
     * Получение выбранного контакта.
     *
     * @return Выбранный контакт, либо null, если контакт не выбран.
     */
    private Contact getSelectedContact() {
        String selectedName = contactListView.getSelectionModel().getSelectedItem();
        return contactManager.contacts().stream()
                .filter(c -> c.getFullName().equals(selectedName))
                .findFirst().orElse(null);
    }

    /**
     * Очистка панели с деталями.
     */
    private void clearDetailsBox() {
        detailsBox.getChildren().clear();
    }

    /**
     * Добавление меток с данными контакта.
     */
    private void addContactLabels() {
        detailsBox.getChildren().addAll(
                new Label("ФИО: " + selectedContact.getFullName()),
                new Label("-------------------------------------"),
                new Label("Номера телефонов:")
        );
        selectedContact.getPhoneNumbers().stream()
                .map(PhoneNumber::toString)
                .map(Label::new)
                .forEach(detailsBox.getChildren()::add);
    }

    /**
     * Добавление кнопки для добавления нового номера.
     */
    private void addAddNumberButton() {
        Button addNumberButton = new Button("Добавить номер");
        addNumberButton.setOnAction(e -> addPhoneNumberDialog());
        detailsBox.getChildren().add(addNumberButton);
    }

    /**
     * Окно для добавления нового контакта.
     */
    private void addContactDialog() {
        logger.info("Открыто окно для добавления нового контакта"); // Логируем открытие окна добавления контакта
        Dialog<Contact> dialog = createContactDialog(); // Создание диалога для добавления контакта
        Optional<Contact> result = dialog.showAndWait();

        result.ifPresent(contact -> {
            contactManager.saveContacts(); // Сохранение контактов
            updateContactListView(); // Обновление списка контактов
        });
    }

    /**
     * Создает и возвращает ComboBox для выбора типа телефона.
     *
     * @return Комбинированный список с вариантами типов номеров (например, мобильный, домашний).
     */
    private ComboBox<String> createPhoneTypeComboBox() {
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Мобильный", "Домашний", "Рабочий", "Другой"); // Добавляем возможные типы
        typeBox.setValue("Мобильный"); // Устанавливаем значение по умолчанию
        return typeBox;
    }

    /**
     * Создание диалога для добавления контакта.
     *
     * @return Диалог для добавления контакта.
     */
    private Dialog<Contact> createContactDialog() {
        Dialog<Contact> dialog = new Dialog<>();
        dialog.setTitle("Добавить контакт");

        TextField nameField = new TextField();
        TextField numberField = new TextField();
        ComboBox<String> typeBox = createPhoneTypeComboBox();

        VBox layout = createContactDialogLayout(nameField, numberField, typeBox);
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> handleDialogResult(button, nameField, numberField, typeBox));
        return dialog;
    }

    /**
     * Создание макета для диалога добавления контакта.
     *
     * @param nameField   Поле для ввода имени.
     * @param numberField Поле для ввода номера телефона.
     * @param typeBox     Комбинированный список для выбора типа номера.
     * @return Макет для диалога.
     */
    private VBox createContactDialogLayout(TextField nameField, TextField numberField, ComboBox<String> typeBox) {
        VBox layout = new VBox(10,
                new Label("ФИО:"), nameField,
                new Label("Номер телефона:"), numberField,
                new Label("Тип номера:"), typeBox
        );
        layout.setPadding(new Insets(10));
        return layout;
    }

    /**
     * Обработка результата диалога добавления контакта.
     *
     * @param button      Кнопка, на которую нажал пользователь.
     * @param nameField   Поле с именем.
     * @param numberField Поле с номером телефона.
     * @param typeBox     Комбинированный список с типом номера.
     * @return Новый контакт или null, если операция была отменена.
     */
    private Contact handleDialogResult(ButtonType button, TextField nameField, TextField numberField, ComboBox<String> typeBox) {
        if (button == ButtonType.OK) {
            try {
                contactManager.addContact(nameField.getText(), numberField.getText(), typeBox.getValue());
                return new Contact(nameField.getText());
            } catch (IllegalArgumentException ex) {
                showAlert(ex.getMessage()); // Показать ошибку, если что-то пошло не так
            }
        }
        return null;
    }

    /**
     * Открывает диалоговое окно для добавления нового номера телефона для выбранного контакта.
     * Если контакт не выбран, метод завершится.
     */
    private void addPhoneNumberDialog() {
        if (selectedContact == null) return; // Если контакт не выбран, выходим

        Dialog<PhoneNumber> dialog = createPhoneNumberDialog();
        Optional<PhoneNumber> result = dialog.showAndWait();

        // Проверяем, если результат есть, обновляем информацию
        result.ifPresent(phoneNumber -> {
            // Номер успешно добавлен, делаем дополнительные действия (например, сохранение)
            contactManager.saveContacts();
            showContactDetails(); // Обновляем детали контакта
        });
    }

    /**
     * Создает диалоговое окно для ввода нового номера телефона.
     *
     * @return Диалог для добавления номера телефона.
     */
    private Dialog<PhoneNumber> createPhoneNumberDialog() {
        Dialog<PhoneNumber> dialog = new Dialog<>();
        dialog.setTitle("Добавить номер");

        TextField numberField = new TextField();
        ComboBox<String> typeBox = createPhoneTypeComboBox();

        VBox layout = new VBox(10, new Label("Номер телефона:"), numberField, new Label("Тип номера:"), typeBox);
        layout.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> handlePhoneNumberDialogResult(button, numberField, typeBox));
        return dialog;
    }

    /**
     * Обработка результата диалога добавления номера телефона.
     *
     * @param button Кнопка, на которую нажал пользователь.
     * @param numberField Поле для ввода номера телефона.
     * @param typeBox Комбинированный список для выбора типа номера.
     * @return Добавленный номер телефона, если нажата кнопка OK, иначе null.
     */
    private PhoneNumber handlePhoneNumberDialogResult(ButtonType button, TextField numberField, ComboBox<String> typeBox) {
        if (button == ButtonType.OK) {
            try {
                // Добавляем новый номер и сохраняем контакты
                contactManager.addPhoneNumber(selectedContact, numberField.getText(), typeBox.getValue());
                contactManager.saveContacts(); // Сохранение контактов
                showContactDetails(); // Обновление деталей контакта
                return new PhoneNumber(numberField.getText(), typeBox.getValue());
            } catch (IllegalArgumentException ex) {
                showAlert(ex.getMessage()); // Показать ошибку, если введены неверные данные
            }
        }
        // При нажатии на кнопку Cancel окно автоматически закрывается
        // и ничего не происходит
        return null;
    }

    /**
     * Удаляет выбранный контакт из списка.
     * После удаления обновляется список контактов и очищаются детали.
     */
    private void deleteContact() {
        Optional.ofNullable(contactListView.getSelectionModel().getSelectedItem())
                .ifPresent(selectedName -> {
                    contactManager.deleteContact(selectedName); // Удаление контакта
                    updateContactListView(); // Обновление списка
                    clearDetailsBox(); // Очистка панели деталей
                });
    }

    /**
     * Фильтрует список контактов по введенному тексту в поле поиска.
     * Обновляет отображаемый список контактов.
     */
    private void filterContacts() {
        String searchText = searchField.getText();
        contactListView.getItems().setAll(contactManager.filterContacts(searchText)); // Фильтрация и обновление списка
    }

    /**
     * Изменяет масштаб шрифта для списка контактов и панели деталей.
     *
     * @param delta Изменение масштаба (положительное для увеличения, отрицательное для уменьшения).
     */
    private void changeZoom(double delta) {
        scale += delta;
        String style = "-fx-font-size: " + scale * 12 + "px;";
        contactListView.setStyle(style); // Изменение стиля для списка контактов
        detailsBox.setStyle(style); // Изменение стиля для панели с деталями
        logger.debug("Масштаб изменен на: {}", scale); // Логируем изменение масштаба
    }

    /**
     * Обрабатывает событие изменения масштаба с помощью клавиш.
     * Увеличивает или уменьшает масштаб шрифта при нажатии клавиш "+" или "-".
     *
     * @param event Событие нажатия клавиши.
     */
    private void handleZoom(KeyEvent event) {
        if (event.getCode() == KeyCode.PLUS) changeZoom(0.1);
        if (event.getCode() == KeyCode.MINUS) changeZoom(-0.1);
    }

    /**
     * Показывает сообщение об ошибке в случае неправильного ввода или других проблем.
     *
     * @param message Сообщение об ошибке, которое будет показано пользователю.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Точка входа для запуска приложения.
     *
     * @param args Аргументы командной строки.
     */
    public static void main(String[] args) {
        launch(args);
    }
}


