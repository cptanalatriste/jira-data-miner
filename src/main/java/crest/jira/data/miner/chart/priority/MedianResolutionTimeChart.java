package crest.jira.data.miner.chart.priority;

import crest.jira.data.miner.report.model.IssueListMetricGenerator;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MedianResolutionTimeChart extends AbstractChart {

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
        getPriorityLabelsBySuffix(IssueListMetricGenerator.RESTIME_MED_SUFFIX));

    showAndSaveChart("Median Resolution Time", stage, chartSeries);
  }

  @Override
  public XYChart<String, Number> getChart() {
    CategoryAxis periodAxis = new CategoryAxis();
    periodAxis.setLabel(TIME_PERIOD_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(FREQUENCY_LABEL);

    BarChart<String, Number> scatterChart = new BarChart<String, Number>(periodAxis, counterAxis);
    return scatterChart;
  }

}
