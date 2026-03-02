package com.triplify.ui.shared.component.button.viewmodel;

import com.triplify.ui.shared.component.button.model.ButtonVariant;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AppButtonViewModel {

    private final StringProperty label = new SimpleStringProperty("");
    private final ObjectProperty<ButtonVariant> variant = new SimpleObjectProperty<>(ButtonVariant.PRIMARY);
    private final StringProperty icon = new SimpleStringProperty(null);
    private final BooleanProperty disabled = new SimpleBooleanProperty(false);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty requireConfirm = new SimpleBooleanProperty(false);
    private final StringProperty confirmMessage = new SimpleStringProperty("Are you sure?");

    private Runnable onAction;

    public StringProperty labelProperty() { return label; }
    public String getLabel() { return label.get(); }
    public void setLabel(String v) { label.set(v); }

    public ObjectProperty<ButtonVariant> variantProperty() { return variant; }
    public ButtonVariant getVariant() { return variant.get(); }
    public void setVariant(ButtonVariant v) { variant.set(v); }

    public StringProperty iconProperty() { return icon; }
    public String getIcon() { return icon.get(); }
    public void setIcon(String v) { icon.set(v); }

    public BooleanProperty disabledProperty() { return disabled; }
    public boolean isDisabled() { return disabled.get(); }
    public void setDisabled(boolean v) { disabled.set(v); }

    public BooleanProperty loadingProperty() { return loading; }
    public boolean isLoading() { return loading.get(); }
    public void setLoading(boolean v) { loading.set(v); }

    public BooleanProperty requireConfirmProperty() { return requireConfirm; }
    public boolean isRequireConfirm() { return requireConfirm.get(); }
    public void setRequireConfirm(boolean v) { requireConfirm.set(v); }

    public StringProperty confirmMessageProperty() { return confirmMessage; }
    public String getConfirmMessage() { return confirmMessage.get(); }
    public void setConfirmMessage(String v) { confirmMessage.set(v); }

    public void setOnAction(Runnable callback) { this.onAction = callback; }

    public void execute() {
        if (disabled.get() || loading.get()) return;
        if (onAction != null) onAction.run();
    }
}

