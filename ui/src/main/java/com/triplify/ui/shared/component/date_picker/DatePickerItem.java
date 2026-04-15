package com.triplify.ui.shared.component.date_picker;

import com.triplify.ui.shared.model.FieldVariant;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

public class DatePickerItem extends VBox {

    private static final URL FXML_URL = DatePickerItem.class.getResource(
            "/com/triplify/ui/shared/component/date_picker/view/AppDatePicker.fxml"
    );
    private static final String THEME_STYLESHEET = "/com/triplify/ui/shared/css/theme.css";
    private static final String COMPONENT_STYLESHEET = "/com/triplify/ui/shared/component/date_picker/css/date_picker.css";
    private static final double POPUP_OFFSET_Y = 50.0;

    @FXML private HBox shell;
    @FXML private TextField textField;
    @FXML private Button triggerButton;

    private final Popup popup = new Popup();
    private final VBox popupRoot = new VBox(10);
    private final HBox popupHeader = new HBox(18);
    private final HBox monthNav = new HBox(6);
    private final HBox yearNav = new HBox(6);
    private final Label monthLabel = new Label();
    private final Label yearLabel = new Label();
    private final GridPane weekdayGrid = new GridPane();
    private final GridPane dayGrid = new GridPane();

    private DateTimeFormatter formatter;
    private FieldVariant lastVariant;
    private LocalDate value;
    private YearMonth displayedMonth;

    public DatePickerItem() {
        this("dd/MM/yyyy", FieldVariant.FILLED);
    }

    public DatePickerItem(String formatPattern) {
        this(formatPattern, FieldVariant.FILLED);
    }

    public DatePickerItem(String formatPattern, FieldVariant variant) {
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AppDatePicker.fxml", e);
        }

        getStyleClass().add("app-date-item");
        setMaxWidth(Double.MAX_VALUE);
        shell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textField, Priority.ALWAYS);

        configureTriggerButton();
        configureFieldBehavior();
        configurePopup();
        setFormatPattern(formatPattern);
        applyVariant(variant);
    }

    private void configureTriggerButton() {
        FontIcon icon = new FontIcon("fth-calendar");
        icon.getStyleClass().add("app-date-trigger-icon");
        icon.setIconSize(14);
        triggerButton.setGraphic(icon);
        triggerButton.setFocusTraversable(false);
        triggerButton.setOnAction(event -> togglePopup());
    }

    private void configureFieldBehavior() {
        textField.setEditable(false);
        textField.setFocusTraversable(false);
        shell.setOnMouseClicked(event -> togglePopup());
        textField.setOnMouseClicked(event -> togglePopup());
        updateFocusedState();
    }

    private void configurePopup() {
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);
        popup.setConsumeAutoHidingEvents(false);
        popup.setOnHidden(event -> updateFocusedState());

        popupRoot.getStyleClass().add("app-date-calendar-popup");
        popupHeader.getStyleClass().add("app-date-calendar-header");
        monthNav.getStyleClass().add("app-date-nav-group");
        yearNav.getStyleClass().add("app-date-nav-group");
        weekdayGrid.getStyleClass().add("app-date-weekday-grid");
        dayGrid.getStyleClass().add("app-date-day-grid");
        weekdayGrid.setHgap(6);
        dayGrid.setHgap(6);
        dayGrid.setVgap(6);

        monthLabel.getStyleClass().add("app-date-calendar-title");
        yearLabel.getStyleClass().add("app-date-calendar-title");

        monthNav.getChildren().addAll(
                createNavButton("fth-chevron-left", () -> shiftMonth(-1)),
                monthLabel,
                createNavButton("fth-chevron-right", () -> shiftMonth(1))
        );
        yearNav.getChildren().addAll(
                createNavButton("fth-chevron-left", () -> shiftYear(-1)),
                yearLabel,
                createNavButton("fth-chevron-right", () -> shiftYear(1))
        );
        popupHeader.getChildren().addAll(monthNav, yearNav);
        popupRoot.getChildren().addAll(popupHeader, weekdayGrid, dayGrid);
        popup.getContent().setAll(popupRoot);

        popupRoot.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                installPopupStylesheets(newScene);
            }
        });
    }

    private Button createNavButton(String iconLiteral, Runnable action) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.getStyleClass().add("app-date-nav-icon");
        icon.setIconSize(13);

        Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().add("app-date-nav-button");
        button.setFocusTraversable(false);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void installPopupStylesheets(Scene scene) {
        addStylesheetIfMissing(scene.getStylesheets(), THEME_STYLESHEET);
        addStylesheetIfMissing(scene.getStylesheets(), COMPONENT_STYLESHEET);
    }

    private void addStylesheetIfMissing(List<String> stylesheets, String resourcePath) {
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return;
        }

        String externalForm = resource.toExternalForm();
        if (!stylesheets.contains(externalForm)) {
            stylesheets.add(externalForm);
        }
    }

    private void shiftMonth(int delta) {
        displayedMonth = displayedMonth.plusMonths(delta);
        renderCalendar();
    }

    private void shiftYear(int delta) {
        displayedMonth = displayedMonth.plusYears(delta);
        renderCalendar();
    }

    private void togglePopup() {
        if (popup.isShowing()) {
            popup.hide();
            return;
        }
        showPopup();
    }

    private void showPopup() {
        Bounds bounds = shell.localToScreen(shell.getBoundsInLocal());
        if (bounds == null) {
            return;
        }

        displayedMonth = YearMonth.from(value != null ? value : LocalDate.now());
        renderCalendar();
        popup.show(shell, bounds.getMinX(), bounds.getMaxY() + POPUP_OFFSET_Y);
        updateFocusedState();
    }

    private void renderCalendar() {
        Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        WeekFields weekFields = WeekFields.of(locale);
        DayOfWeek firstDayOfWeek = weekFields.getFirstDayOfWeek();

        String monthText = displayedMonth.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, locale);
        monthLabel.setText(capitalize(monthText));
        yearLabel.setText(Integer.toString(displayedMonth.getYear()));

        renderWeekdays(firstDayOfWeek, locale);
        renderDays(firstDayOfWeek, locale);
    }

    private void renderWeekdays(DayOfWeek firstDayOfWeek, Locale locale) {
        weekdayGrid.getChildren().clear();
        for (int i = 0; i < 7; i++) {
            DayOfWeek day = firstDayOfWeek.plus(i);
            Label label = new Label(shortDayName(day, locale));
            label.getStyleClass().add("app-date-weekday-cell");
            label.setMaxWidth(Double.MAX_VALUE);
            weekdayGrid.add(label, i, 0);
        }
    }

    private void renderDays(DayOfWeek firstDayOfWeek, Locale locale) {
        dayGrid.getChildren().clear();

        LocalDate firstOfMonth = displayedMonth.atDay(1);
        int offset = Math.floorMod(firstOfMonth.getDayOfWeek().getValue() - firstDayOfWeek.getValue(), 7);
        LocalDate current = firstOfMonth.minusDays(offset);
        LocalDate today = LocalDate.now();

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                LocalDate cellDate = current;
                Button cell = new Button(Integer.toString(cellDate.getDayOfMonth()));
                cell.setFocusTraversable(false);
                cell.getStyleClass().add("app-date-day-cell");

                if (!YearMonth.from(cellDate).equals(displayedMonth)) {
                    cell.getStyleClass().add("app-date-day-cell-muted");
                }
                if (today.equals(cellDate)) {
                    cell.getStyleClass().add("app-date-day-cell-today");
                }
                if (value != null && value.equals(cellDate)) {
                    cell.getStyleClass().add("app-date-day-cell-selected");
                }

                cell.setOnAction(event -> {
                    setValue(cellDate);
                    displayedMonth = YearMonth.from(cellDate);
                    popup.hide();
                });

                dayGrid.add(cell, col, row);
                current = current.plusDays(1);
            }
        }
    }

    private String shortDayName(DayOfWeek dayOfWeek, Locale locale) {
        String name = dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale).replace(".", "");
        if (name.length() > 2) {
            return capitalize(name.substring(0, 2));
        }
        return capitalize(name);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private void updateFocusedState() {
        if (popup.isShowing()) {
            if (!shell.getStyleClass().contains("app-date-shell-focused")) {
                shell.getStyleClass().add("app-date-shell-focused");
            }
            return;
        }

        shell.getStyleClass().remove("app-date-shell-focused");
    }

    private void applyVariant(FieldVariant variant) {
        if (lastVariant == variant) {
            return;
        }
        if (lastVariant != null) {
            shell.getStyleClass().remove(toStyleClass(lastVariant));
        }
        if (variant != null) {
            shell.getStyleClass().add(toStyleClass(variant));
        }
        lastVariant = variant;
    }

    private static String toStyleClass(FieldVariant variant) {
        return switch (variant) {
            case OUTLINED -> "app-date-shell-outlined";
            case FILLED -> "app-date-shell-filled";
            case GHOST -> "app-date-shell-ghost";
        };
    }

    public void setFormatPattern(String formatPattern) {
        formatter = DateTimeFormatter.ofPattern(formatPattern);
        setPromptText(formatPattern.toLowerCase(Locale.ROOT));
        updateDisplayText();
    }

    private void updateDisplayText() {
        textField.setText(value == null ? "" : formatter.format(value));
    }

    public LocalDate getValue() {
        return value;
    }

    public void setValue(LocalDate value) {
        this.value = value;
        updateDisplayText();
    }

    public void setPromptText(String promptText) {
        textField.setPromptText(promptText);
    }

    public void show() {
        showPopup();
    }
}
