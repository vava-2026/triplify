package com.triplify.ui.shared.header.view;

import com.triplify.ui.shared.header.viewmodel.LanguageIslandViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class LanguageIslandView implements Initializable {

    @FXML private Label languageLabel;

    private final LanguageIslandViewModel viewModel = new LanguageIslandViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageLabel.textProperty().bind(viewModel.languageCodeBinding());
    }

    @FXML
    private void onClicked() {
        viewModel.toggle();
    }
}

