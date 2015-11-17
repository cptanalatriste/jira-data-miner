package crest.jira.data.miner.chart;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.util.Map;

public class PriorityChangesChart extends AbstractChart {

  private static final String PERIOD_IDENTIFIER = "Period Identifier";
  private static final String PRIORITY_CHANGES_IDENTIFIER = "Priority Changes";

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  private void buildChart(Stage stage) {
    CategoryAxis periodAxis = new CategoryAxis();
    periodAxis.setLabel(TIME_PERIOD_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(FREQUENCY_LABEL);

    ScatterChart<String, Number> scatterChart = new ScatterChart<String, Number>(periodAxis,
        counterAxis);

    Map<String, Series<String, Number>> chartSeries = getSeries(getCsvFileLocation(),
        PERIOD_IDENTIFIER, PRIORITY_CHANGES_IDENTIFIER);
    scatterChart.getData().addAll(chartSeries.values());

    showChart(scatterChart, "Priority Changes", stage);
  }
}
