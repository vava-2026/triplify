package com.triplify.ui.shared.component.section_header.view;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;

public class SectionHeaderView extends HBox {

    private static final URL FXML_URL = SectionHeaderView.class.getResource(
            "/com/triplify/ui/shared/component/section_header/view/AppSectionHeader.fxml"
    );
    private static final URL CSS_URL = SectionHeaderView.class.getResource(
            "/com/triplify/ui/shared/component/editor/css/editor_components.css"
    );

    @FXML private FontIcon iconNode;
    @FXML private Label titleLabel;
    @FXML private Label tagLabel;

    public SectionHeaderView() {
        FXMLLoader loader = new FXMLLoader(FXML_URL);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AppSectionHeader.fxml", e);
        }

        if (CSS_URL != null) {
            getStylesheets().add(CSS_URL.toExternalForm());
        }
    }

    public StringProperty titleProperty() {
        return titleLabel.textProperty();
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public String getIconLiteral() {
        return iconNode.getIconLiteral();
    }

    public void setIconLiteral(String iconLiteral) {
        iconNode.setIconLiteral(iconLiteral);
    }

    public StringProperty tagTextProperty() {
        return tagLabel.textProperty();
    }

    public String getTagText() {
        return tagLabel.getText();
    }

    public void setTagText(String tagText) {
        tagLabel.setText(tagText);
    }

    public boolean isTagVisible() {
        return tagLabel.isVisible();
    }

    public void setTagVisible(boolean visible) {
        tagLabel.setVisible(visible);
        tagLabel.setManaged(visible);
    }
}
