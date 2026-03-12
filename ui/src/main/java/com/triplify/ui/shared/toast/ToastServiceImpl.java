package com.triplify.ui.shared.toast;

import com.google.inject.Singleton;
import com.triplify.ui.shared.util.FxmlLoaderHelper;
import com.triplify.ui.shared.util.FxmlLoadResult;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;


@Singleton
public class ToastServiceImpl implements ToastService {

    private static final Logger log = LoggerFactory.getLogger(ToastServiceImpl.class);
    private static final URL FXML_URL = ToastServiceImpl.class.getResource("/com/triplify/ui/shared/toast/ToastView.fxml");

    private final FxmlLoaderHelper fxmlLoader;

    private VBox container;
    private StackPane root;

    @com.google.inject.Inject
    public ToastServiceImpl(FxmlLoaderHelper fxmlLoader) {
        this.fxmlLoader = fxmlLoader;
    }

    public void attach(StackPane appRoot) {
        this.root = appRoot;

        container = new VBox(8);
        container.setAlignment(Pos.TOP_RIGHT);
        container.setPickOnBounds(false);
        container.setMouseTransparent(false);
        StackPane.setAlignment(container, Pos.TOP_RIGHT);
        StackPane.setMargin(container, new Insets(16, 16, 0, 0));

        appRoot.getChildren().add(container);
    }

    @Override
    public void error(String message) {
        show(ToastType.ERROR, ToastType.ERROR.getDefaultTitle(), message);
    }

    @Override
    public void error(String title, String message) {
        show(ToastType.ERROR, title, message);
    }

    @Override
    public void success(String message) {
        show(ToastType.SUCCESS, ToastType.SUCCESS.getDefaultTitle(), message);
    }

    @Override
    public void success(String title, String message) {
        show(ToastType.SUCCESS, title, message);
    }

    @Override
    public void info(String message) {
        show(ToastType.INFO, ToastType.INFO.getDefaultTitle(), message);
    }

    @Override
    public void info(String title, String message) {
        show(ToastType.INFO, title, message);
    }

    @Override
    public void warning(String message) {
        show(ToastType.WARNING, ToastType.WARNING.getDefaultTitle(), message);
    }

    @Override
    public void warning(String title, String message) {
        show(ToastType.WARNING, title, message);
    }

    private void show(ToastType type, String title, String message) {
        Platform.runLater(() -> showOnFxThread(type, title, message));
    }

    private void showOnFxThread(ToastType type, String title, String message) {
        if (container == null) {
            log.warn("ToastService not attached - call attach(StackPane) in MainApp.start()");
            return;
        }
        if (FXML_URL == null) {
            log.error("ToastView.fxml not found");
            return;
        }

        FxmlLoadResult<javafx.scene.Node, ToastView> result = fxmlLoader.load(FXML_URL);
        ToastView view = result.controller();
        javafx.scene.Node node = result.node();

        node.setClip(null);

        container.getChildren().add(node);
        view.setOnDone(() -> container.getChildren().remove(node));
        view.show(type, title, message);
    }
}
