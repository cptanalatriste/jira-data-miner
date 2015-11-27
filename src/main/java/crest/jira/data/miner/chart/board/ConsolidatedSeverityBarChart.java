package crest.jira.data.miner.chart.board;

import crest.jira.data.miner.chart.priority.ConsolidatedSeverityChart;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;

public class ConsolidatedSeverityBarChart extends ConsolidatedSeverityChart {

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public XYChart<String, Number> getChart() {
    CategoryAxis boardAxis = new CategoryAxis();
    boardAxis.setLabel(BOARD_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(RELATIVE_FREQUENCY_LABEL);

    StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<String, Number>(boardAxis,
        counterAxis);
    return stackedBarChart;
  }
}
