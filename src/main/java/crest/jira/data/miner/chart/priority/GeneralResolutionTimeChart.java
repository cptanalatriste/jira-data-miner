package crest.jira.data.miner.chart.priority;

import crest.jira.data.miner.report.model.IssueListMetricGenerator;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.List;

public class GeneralResolutionTimeChart extends AbstractChart {

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  private void buildChart(Stage stage) throws IOException {
    List<Series<String, Number>> chartSeries = getSeries(getCsvFileLocation(),
        IssueListMetricGenerator.PERIOD_IDENTIFIER,
        IssueListMetricGenerator.TOTAL_IDENTIFIER + IssueListMetricGenerator.RESTIME_MED_SUFFIX);

    showAndSaveChart("General Resolution Time", stage, chartSeries);

  }

  @Override
  public XYChart<String, Number> getChart() {
    CategoryAxis periodAxis = new CategoryAxis();
    periodAxis.setLabel(TIME_PERIOD_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(TIME_LABEL);

    LineChart<String, Number> lineChart = new LineChart<String, Number>(periodAxis, counterAxis);
    return lineChart;
  }

}
