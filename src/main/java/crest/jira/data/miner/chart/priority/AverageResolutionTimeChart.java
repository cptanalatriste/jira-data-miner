package crest.jira.data.miner.chart.priority;

import crest.jira.data.miner.chart.AbstractChart;
import crest.jira.data.miner.report.model.CsvConfiguration;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AverageResolutionTimeChart extends AbstractChart<String, Number> {

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  private void buildChart(Stage stage) throws IOException {

    List<Series<String, Number>> chartSeries = getSeries(getCsvFileLocation(),
        CsvConfiguration.TIME_PERIOD_IDENTIFIER,
        getPriorityLabelsBySuffix(CsvConfiguration.RESTIME_AVG_SUFFIX));

    showAndSaveChart("Average Resolution Time", stage, chartSeries);
  }

  @Override
  public XYChart<String, Number> getChart() {
    CategoryAxis periodAxis = new CategoryAxis();
    periodAxis.setLabel(PERIOD);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(COUNT_LABEL);

    StackedBarChart<String, Number> stackedBar = new StackedBarChart<String, Number>(periodAxis,
        counterAxis);
    return stackedBar;
  }

}
