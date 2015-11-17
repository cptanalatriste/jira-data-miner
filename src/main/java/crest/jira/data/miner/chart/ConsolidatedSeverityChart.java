package crest.jira.data.miner.chart;


import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.util.Map;

public class ConsolidatedSeverityChart extends AbstractChart {

  private static final String FILE_LOCATION = 
      "C:/Users/cgavi/OneDrive/phd2/jira_data/Board_25_1447691416627.csv";
  private static final String PERIOD_IDENTIFIER = "Period Identifier";
  private static final String LOW_SEVERITY_IDENTIFIER = "Non-Severe (%)";
  private static final String MEDIUM_SEVERITY_IDENTIFIER = "Major (%)";
  private static final String HIGH_SEVERITY_IDENTIFIER = "Severe (%)";

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
    counterAxis.setLabel(RELATIVE_FREQUENCY_LABEL);

    StackedAreaChart<String, Number> stackAreaChart = new StackedAreaChart<String, Number>(
        periodAxis, counterAxis);

    Map<String, Series<String, Number>> chartSeries = getSeries(FILE_LOCATION, PERIOD_IDENTIFIER,
        LOW_SEVERITY_IDENTIFIER, MEDIUM_SEVERITY_IDENTIFIER, HIGH_SEVERITY_IDENTIFIER);
    stackAreaChart.getData().addAll(chartSeries.values());

    showChart(stackAreaChart, "Consolidated Severity", stage);

  }
}