package com.triplify.ui.pages.settings;

import com.triplify.ui.shared.component.entry.model.Entry;
import com.triplify.ui.shared.component.entry.model.EntryVariant;
import com.triplify.ui.shared.component.select.model.Select;
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
                .items(Arrays.asList(
                        Entry.builder(1, "One").variant(EntryVariant.PRIMARY).icon("fth-globe").build(),
                        Entry.builder(2, "Two").variant(EntryVariant.SECONDARY).build(),
                        Entry.builder(3, "Three").variant(EntryVariant.DANGER).build(),
                        Entry.builder(1, "Option A").build(),
                        Entry.builder(2, "Option B").variant(EntryVariant.SECONDARY).build(),
                        Entry.builder(3, "Option C").variant(EntryVariant.DANGER).build(),
                        Entry.builder(4, "Option D").variant(EntryVariant.MUTED).build(),
                        Entry.builder(5, "Option E").variant(EntryVariant.DANGER).build()
                ))
                .onSelect(entry -> System.out.println("Selected: " + entry.getValue()))
                .build());

        contentDemo.getChildren().add(selectView);
    }
}
