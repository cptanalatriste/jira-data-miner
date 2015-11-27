package crest.jira.data.miner.chart;

import crest.jira.data.miner.report.model.IssueListMetricGenerator;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class PriorityRelativeFrequenciesChart extends AbstractChart {

  private static final String CHART_TITLE = "Relative Frequency according Priority";
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
    counterAxis.setLabel(RELATIVE_FREQUENCY_LABEL);

    StackedAreaChart<String, Number> stackedBarChart = new StackedAreaChart<String, Number>(
        periodAxis, counterAxis);

    String[] seriesLabels = getPriorityLabelsBySuffix(IssueListMetricGenerator.RELATIVE_SUFIX);
    List<Series<String, Number>> chartSeries = getSeries(getCsvFileLocation(), PERIOD_IDENTIFIER,
        seriesLabels);
    stackedBarChart.getData().addAll(chartSeries);

    showAndSaveChart(stackedBarChart, CHART_TITLE, stage);
  }

}
