package com.triplify.ui.shared.toast;

import javafx.scene.layout.StackPane;

public interface ToastService {

    void attach(StackPane appRoot);

    void error(String message);
    void error(String title, String message);

    void success(String message);
    void success(String title, String message);

    void info(String message);
    void info(String title, String message);

    void warning(String message);
    void warning(String title, String message);
}
