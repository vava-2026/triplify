package com.triplify.ui.pages.settings;

import com.triplify.application.shared.ColorTheme;
import com.triplify.ui.shared.component.select.entry.model.Entry;
import com.triplify.ui.shared.component.select.model.Select;
import com.triplify.ui.shared.model.FieldVariant;
import com.triplify.ui.shared.component.select.view.SelectView;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.Arrays;

public class SettingsController extends SimpleLifecycleAwareController {

    @FXML
    private VBox contentDemo;

    @FXML
    public void initialize() {
        SelectView<Integer> selectView = new SelectView<>();
        selectView.update(Select.<Integer>builder()
                .placeholder("Choose a number...")
                .variant(FieldVariant.GHOST)
                .items(Arrays.asList(
                        Entry.builder(1, "One").colorTheme(ColorTheme.BLUE).icon("fth-globe").build(),
                        Entry.builder(2, "Two").colorTheme(ColorTheme.GREEN).emoji("\uD83C\uDFD6\uFE0F").build(),
                        Entry.builder(3, "Three").colorTheme(ColorTheme.RED).emoji("\uD83D\uDD25").build(),
                        Entry.builder(1, "Option A").build(),
                        Entry.builder(2, "Option B").colorTheme(ColorTheme.ORANGE).build(),
                        Entry.builder(3, "Option C").colorTheme(ColorTheme.PURPLE).build(),
                        Entry.builder(4, "Option D").colorTheme(ColorTheme.TEAL).build(),
                        Entry.builder(5, "Option E").colorTheme(ColorTheme.PINK).build()
                ))
                .onSelect(entry -> System.out.println("Selected: " + entry.getValue()))
                .build());

        contentDemo.getChildren().add(selectView);
    }
}
