package com.triplify.ui.pages.account;

import com.google.inject.Inject;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.application.usecase.statistic.StatisticService;
import com.triplify.application.usecase.statistic.dto.GetDisplayedStatisticsRequest;
import com.triplify.application.usecase.statistic.dto.StatisticResponse;
import com.triplify.domain.model.enums.StatisticType;
import com.triplify.ui.shared.util.Localization;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class StatisticsController extends SimpleLifecycleAwareController {
    private static final Logger log = LoggerFactory.getLogger(StatisticsController.class);
    private static final int COLUMNS_PER_ROW = 5;

    @FXML private Label titleLabel;
    @FXML private GridPane statisticsGrid;

    @Inject private StatisticService statisticService;
    @Inject private UserSessionContext sessionContext;

    private final Map<StatisticType, Label> valueLabels = new EnumMap<>(StatisticType.class);

    @FXML
    public void initialize() {
        Localization.bindText(titleLabel.textProperty(), "statistics.page.title");
        initializeStatisticsGrid();
    }

    private void initializeStatisticsGrid() {
        statisticsGrid.getChildren().clear();
        statisticsGrid.getColumnConstraints().clear();
        valueLabels.clear();

        StatisticType[] displayedTypes = getDisplayedStatisticTypes();

        double columnWidth = 100.0 / COLUMNS_PER_ROW;
        for (int i = 0; i < Math.min(displayedTypes.length, COLUMNS_PER_ROW); i++) {
            javafx.scene.layout.ColumnConstraints col = new javafx.scene.layout.ColumnConstraints();
            col.setPercentWidth(columnWidth);
            statisticsGrid.getColumnConstraints().add(col);
        }

        int columnIndex = 0;
        for (StatisticType type : displayedTypes) {
            VBox card = createStatisticCard(type);
            GridPane.setColumnIndex(card, columnIndex);
            GridPane.setRowIndex(card, 0);
            statisticsGrid.getChildren().add(card);
            columnIndex++;
        }
    }

    private StatisticType[] getDisplayedStatisticTypes() {
        return java.util.Arrays.stream(StatisticType.values())
                .filter(StatisticType::isDisplayed)
                .toArray(StatisticType[]::new);
    }

    private VBox createStatisticCard(StatisticType type) {
        VBox card = new VBox();
        card.getStyleClass().add("stat-card");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox.setVgrow(card, Priority.ALWAYS);
        card.setPadding(new Insets(20, 10, 20, 20));

        StackPane iconCircle = new StackPane();
        iconCircle.getStyleClass().addAll("icon-circle", type.getIconColor());
        iconCircle.setMaxWidth(40);
        iconCircle.setMinWidth(40);
        iconCircle.setMaxHeight(40);
        iconCircle.setMinHeight(40);

        FontIcon icon = new FontIcon();
        icon.setIconLiteral(type.getIcon());
        icon.setIconSize(24);
        icon.getStyleClass().add("card-icon");
        iconCircle.getChildren().add(icon);

        Label labelText = new Label();
        Localization.bindText(labelText.textProperty(), type.getLabelKey());
        labelText.getStyleClass().add("card-label");
        labelText.setWrapText(true);

        Label valueLabel = new Label("0");
        valueLabel.getStyleClass().add("card-value");
        valueLabels.put(type, valueLabel);

        card.getChildren().addAll(iconCircle, labelText, valueLabel);
        return card;
    }

    @Override
    public void onLifecycleShow() {
        sessionContext.getCurrent().ifPresentOrElse(user -> {
            var result = statisticService.getDisplayedStatistics(new GetDisplayedStatisticsRequest(user.userId()));
            result.onSuccess(this::renderStatistics);
            result.onFailure(error -> log.warn("Failed to load statistics: {}", error.code()));
        }, this::renderZeros);
    }

    private void renderStatistics(List<StatisticResponse> statistics) {
        renderZeros();
        for (StatisticResponse statistic : statistics) {
            Label label = valueLabels.get(statistic.type());
            if (label != null) {
                label.setText(Long.toString(statistic.amount()));
            }
        }
    }

    private void renderZeros() {
        valueLabels.values().forEach(label -> label.setText("0"));
    }
}
