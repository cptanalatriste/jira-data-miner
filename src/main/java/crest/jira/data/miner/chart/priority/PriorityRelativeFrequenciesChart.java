package crest.jira.data.miner.chart.priority;

import crest.jira.data.miner.chart.AbstractChart;
import crest.jira.data.miner.csv.JiraCsvConfiguration;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class PriorityRelativeFrequenciesChart extends AbstractChart<String, Number> {

  private static final String CHART_TITLE = "Relative Frequency according Priority";

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  private void buildChart(Stage stage) throws IOException {

    String[] seriesLabels = getPriorityLabelsBySuffix(JiraCsvConfiguration.RELATIVE_SUFIX);
    List<Series<String, Number>> chartSeries = getSeries(getCsvFileLocation(),
        JiraCsvConfiguration.TIME_PERIOD_IDENTIFIER, seriesLabels);

    showAndSaveChart(CHART_TITLE, stage, chartSeries);
  }

  @Override
  public XYChart<String, Number> getChart() {
    CategoryAxis periodAxis = new CategoryAxis();
    periodAxis.setLabel(PERIOD);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(RELATIVE_FREQUENCY_LABEL);

    StackedAreaChart<String, Number> stackedBarChart = new StackedAreaChart<String, Number>(
        periodAxis, counterAxis);
    return stackedBarChart;
  }

}
