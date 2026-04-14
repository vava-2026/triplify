package com.triplify.ui.pages.placeholder;

import com.google.inject.Inject;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.ui.i18n.I18n;
import com.triplify.ui.routing.AppPage;
import com.triplify.ui.routing.PageAccessService;
import com.triplify.ui.routing.TriplifyRouterContext;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.kordamp.ikonli.javafx.FontIcon;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.Comparator;
import java.util.stream.Collectors;

public class PlaceholderPageController extends SimpleLifecycleAwareController {

    @FXML private FontIcon pageIcon;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label routeLabel;
    @FXML private Label routeValueLabel;
    @FXML private Label rolesLabel;
    @FXML private Label rolesValueLabel;

    @Inject private PageAccessService pageAccessService;

    private final ObjectProperty<AppPage> currentPage = new SimpleObjectProperty<>();

    @FXML
    public void initialize() {
        titleLabel.textProperty().bind(Bindings.createStringBinding(
                () -> currentPage.get() == null ? "" : I18n.t(currentPage.get().getLabelKey()),
                currentPage,
                I18n.bundleProperty()));
        subtitleLabel.textProperty().bind(Bindings.createStringBinding(
                () -> I18n.t("page.placeholder.subtitle"),
                I18n.bundleProperty()));
        routeLabel.textProperty().bind(Bindings.createStringBinding(
                () -> I18n.t("page.placeholder.routeLabel"),
                I18n.bundleProperty()));
        routeValueLabel.textProperty().bind(Bindings.createStringBinding(
                () -> currentPage.get() == null ? "" : currentPage.get().getRouteId(),
                currentPage));
        rolesLabel.textProperty().bind(Bindings.createStringBinding(
                () -> I18n.t("page.placeholder.rolesLabel"),
                I18n.bundleProperty()));
        rolesValueLabel.textProperty().bind(Bindings.createStringBinding(
                () -> currentPage.get() == null ? "" : currentPage.get().getAllowedRoles().stream()
                        .sorted(Comparator.comparingInt(Enum::ordinal))
                        .map(this::translateRole)
                        .collect(Collectors.joining(", ")),
                currentPage,
                I18n.bundleProperty()));
    }

    @Override
    public void onLifecycleShow() {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(false);

        AppPage page = pageAccessService.getPage(getRouter().getCurrentDestination());
        currentPage.set(page);
        pageIcon.setIconLiteral(page.getIcon());
        pageIcon.setIconSize(28);
    }

    private String translateRole(RoleEnum role) {
        return I18n.t(pageAccessService.getRoleLabelKey(role));
    }
}
