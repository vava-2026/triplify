package com.triplify.ui.shared.header.view;

import com.triplify.ui.i18n.I18n;
import com.triplify.ui.shared.header.viewmodel.HeaderViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class HeaderView implements Initializable {

    @FXML private Label pageTitle;
    @FXML private TextField searchField;
    @FXML private Label languageLabel;

    private final HeaderViewModel viewModel = new HeaderViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pageTitle.textProperty().bind(viewModel.pageTitleBinding());

        searchField.promptTextProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> I18n.t("header.search.prompt"),
                        I18n.bundleProperty()));

        searchField.textProperty().bindBidirectional(viewModel.searchTextProperty());

        languageLabel.textProperty().bind(viewModel.languageCodeBinding());
    }

    public HeaderViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    private void onLanguageClicked(MouseEvent event) {
        viewModel.onLanguageClicked();
    }
}
