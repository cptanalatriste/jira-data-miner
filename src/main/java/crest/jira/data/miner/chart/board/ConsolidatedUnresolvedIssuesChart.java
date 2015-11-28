package crest.jira.data.miner.chart.board;

import crest.jira.data.miner.chart.priority.AbstractChart;
import crest.jira.data.miner.report.model.IssueListMetricGenerator;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ConsolidatedUnresolvedIssuesChart extends AbstractChart {

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
        IssueListMetricGenerator.LOW_SEVERITY_IDENTIFIER
            + IssueListMetricGenerator.UNRESOLVED_RELATIVE_SUFIX,
        IssueListMetricGenerator.PRIORITY_DESCRIPTIONS[3]
            + IssueListMetricGenerator.UNRESOLVED_RELATIVE_SUFIX,
        IssueListMetricGenerator.HIGH_SEVERITY_IDENTIFIER
            + IssueListMetricGenerator.UNRESOLVED_RELATIVE_SUFIX);

    showAndSaveChart("Consolidated Unresolved Percentage", stage, chartSeries);

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
