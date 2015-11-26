package crest.jira.data.miner.chart;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.util.Map;

public class MedianResolutionTimeChart extends AbstractChart {

  private static final String PERIOD_IDENTIFIER = "Period Identifier";
  private static final String BLOCKER_IDENTIFIER = "Blocker Resolution Time (med)";
  private static final String CRITICAL_IDENTIFIER = "Critical Resolution Time (med)";
  private static final String MINOR_IDENTIFIER = "Minor Resolution Time (med)";
  private static final String TRIVIAL_IDENTIFIER = "Trivial Resolution Time (med)";
  private static final String MAJOR_IDENTIFIER = "Major Resolution Time (med)";

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
        PERIOD_IDENTIFIER, BLOCKER_IDENTIFIER, CRITICAL_IDENTIFIER, MAJOR_IDENTIFIER,
        MINOR_IDENTIFIER, TRIVIAL_IDENTIFIER);
    scatterChart.getData().addAll(chartSeries.values());

    showChart(scatterChart, "Median Resolution Time", stage);

  }

}
