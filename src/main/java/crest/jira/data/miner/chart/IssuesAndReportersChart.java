package crest.jira.data.miner.chart;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class IssuesAndReportersChart extends AbstractChart {

  private static final String NUMBER_REPORTERS_IDENTIFIER = "Number of Reporters";
  private static final String TOTAL_IDENTIFIER = "Total";
  private static final String PERIOD_IDENTIFIER = "Period Identifier";
  private static final String ISSUES_PER_REPORTER_IDENTIFIER = "Average Issues per Reporter";

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  private void buildChart(Stage stage) throws FileNotFoundException, IOException {

    CategoryAxis periodAxis = new CategoryAxis();
    periodAxis.setLabel(TIME_PERIOD_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(FREQUENCY_LABEL);

    ScatterChart<String, Number> scatterChart = new ScatterChart<String, Number>(periodAxis,
        counterAxis);

    Map<String, Series<String, Number>> chartSeries = getSeries(getCsvFileLocation(),
        PERIOD_IDENTIFIER, TOTAL_IDENTIFIER, NUMBER_REPORTERS_IDENTIFIER,
        ISSUES_PER_REPORTER_IDENTIFIER);
    scatterChart.getData().addAll(chartSeries.values());

    showAndSaveChart(scatterChart, "Issues and Reporters", stage);
  }

}
