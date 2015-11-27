package crest.jira.data.miner.chart;

import crest.jira.data.miner.report.model.IssueListMetricGenerator;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class PriorityFrequenciesChart extends AbstractChart {

  private static final String CHART_TITLE = "Frequency according Priority";
  private static final String PERIOD_IDENTIFIER = "Period Identifier";

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
    counterAxis.setLabel(FREQUENCY_LABEL);

    StackedBarChart<String, Number> stackedChart = new StackedBarChart<String, Number>(periodAxis,
        counterAxis);

    List<Series<String, Number>> chartSeries = getSeries(getCsvFileLocation(), PERIOD_IDENTIFIER,
        getPriorityLabelsBySuffix(IssueListMetricGenerator.FREQUENCIES_SUFIX));
    stackedChart.getData().addAll(chartSeries);

    showAndSaveChart(stackedChart, CHART_TITLE, stage);
  }

}
