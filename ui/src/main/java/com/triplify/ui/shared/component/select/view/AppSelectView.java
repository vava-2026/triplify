package com.triplify.ui.shared.component.select.view;

import com.triplify.ui.shared.component.select.model.SelectEntry;
import com.triplify.ui.shared.component.select.model.SelectVariant;
import com.triplify.ui.shared.component.select.viewmodel.AppSelectViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class AppSelectView<T> implements Initializable {

    private static final URL FXML_URL = AppSelectView.class.getResource(
            "/com/triplify/ui/shared/component/select/view/AppSelect.fxml");
    private static final URL CSS_URL = AppSelectView.class.getResource(
            "/com/triplify/ui/shared/component/select/css/select.css");

    @FXML
    private ComboBox<SelectEntry<T>> comboBox;

    private AppSelectViewModel<T> viewModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public ComboBox<SelectEntry<T>> getComboBox() {
        return comboBox;
    }

    public AppSelectViewModel<T> getViewModel() {
        return viewModel;
    }

    @FXML
    private void onSelectionChanged() {
        SelectEntry<T> value = comboBox.getValue();
        if (value != null && viewModel != null) {
            viewModel.setSelectedItem(value);
            viewModel.triggerSelect(value);
        }
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {
        private String placeholder = "Select...";
        private SelectVariant variant = SelectVariant.PRIMARY;
        private ObservableList<SelectEntry<T>> items = FXCollections.observableArrayList();
        private SelectEntry<T> selectedItem = null;
        private boolean disabled = false;
        private Consumer<SelectEntry<T>> onSelect = null;

        private Builder() {
        }

        public Builder<T> placeholder(String v) {
            this.placeholder = v;
            return this;
        }

        public Builder<T> variant(SelectVariant v) {
            this.variant = v;
            return this;
        }

        public Builder<T> items(ObservableList<SelectEntry<T>> v) {
            this.items = v;
            return this;
        }

        public Builder<T> selectedItem(SelectEntry<T> v) {
            this.selectedItem = v;
            return this;
        }

        public Builder<T> disabled(boolean v) {
            this.disabled = v;
            return this;
        }

        public Builder<T> onSelect(Consumer<SelectEntry<T>> r) {
            this.onSelect = r;
            return this;
        }

        public ComboBox<SelectEntry<T>> build() {
            if (FXML_URL == null) throw new IllegalStateException("AppSelect.fxml not found");
            FXMLLoader loader = new FXMLLoader(FXML_URL);
            try {
                loader.load();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load AppSelect.fxml", e);
            }
            AppSelectView<T> view = loader.getController();
            view.configure(placeholder, variant, items, selectedItem, disabled, onSelect);
            return view.getComboBox();
        }
    }

    private void configure(String placeholder,
                           SelectVariant variant,
                           ObservableList<SelectEntry<T>> items,
                           SelectEntry<T> selectedItem,
                           boolean disabled,
                           Consumer<SelectEntry<T>> onSelect) {
        viewModel = new AppSelectViewModel<>();
        viewModel.setPlaceholder(placeholder);
        viewModel.setVariant(variant);
        viewModel.setItems(items);
        viewModel.setDisabled(disabled);
        viewModel.setOnSelect(onSelect);

        if (CSS_URL != null) comboBox.getStylesheets().add(CSS_URL.toExternalForm());

        comboBox.getStyleClass().add("app-select");
        if (variant != null) comboBox.getStyleClass().add(variant.getStyleClass());

        comboBox.setItems(viewModel.getItems());
        comboBox.disableProperty().bind(viewModel.disabledProperty());
        comboBox.promptTextProperty().bind(viewModel.placeholderProperty());

        // Reusable cell — also used by Search
        comboBox.setCellFactory(lv -> new AppSelectEntryCell<>(variant));
        comboBox.setButtonCell(new AppSelectEntryCell<>(variant));

        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SelectEntry<T> e) {
                return e == null ? "" : e.getLabel();
            }

            @Override
            public SelectEntry<T> fromString(String s) {
                return null;
            }
        });

        //comboBox.setOnAction(e -> onSelectionChanged());
        if (selectedItem != null) comboBox.setValue(selectedItem);
    }
}
