package crest.jira.data.miner.chart;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class UnresolvedIssuesChart extends AbstractChart {

  private static final String PERIOD_IDENTIFIER = "Period Identifier";
  private static final String BLOCKER_IDENTIFIER = "Blocker Unresolved (%)";
  private static final String CRITICAL_IDENTIFIER = "Critical Unresolved (%)";
  private static final String MINOR_IDENTIFIER = "Minor Unresolved (%)";
  private static final String TRIVIAL_IDENTIFIER = "Trivial Unresolved (%)";
  private static final String MAJOR_IDENTIFIER = "Major Unresolved (%)";

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  private void buildChart(Stage stage) throws IOException {
    CategoryAxis periodAxis = new CategoryAxis();
    periodAxis.setLabel(TIME_PERIOD_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(RELATIVE_FREQUENCY_LABEL);

    ScatterChart<String, Number> scatterChart = new ScatterChart<String, Number>(periodAxis,
        counterAxis);

    List<Series<String, Number>> chartSeries = getSeries(getCsvFileLocation(),
        PERIOD_IDENTIFIER, BLOCKER_IDENTIFIER, CRITICAL_IDENTIFIER, MAJOR_IDENTIFIER,
        MINOR_IDENTIFIER, TRIVIAL_IDENTIFIER);
    scatterChart.getData().addAll(chartSeries);

    showAndSaveChart(scatterChart, "Unresolved Issues Percentage", stage);

  }

}
