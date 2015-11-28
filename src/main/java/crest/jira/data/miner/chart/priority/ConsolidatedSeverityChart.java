package crest.jira.data.miner.chart.priority;

import crest.jira.data.miner.report.model.IssueListMetricGenerator;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ConsolidatedSeverityChart extends AbstractChart {

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
        IssueListMetricGenerator.LOW_SEVERITY_IDENTIFIER + IssueListMetricGenerator.RELATIVE_SUFIX,
        IssueListMetricGenerator.PRIORITY_DESCRIPTIONS[3] + IssueListMetricGenerator.RELATIVE_SUFIX,
        IssueListMetricGenerator.HIGH_SEVERITY_IDENTIFIER
            + IssueListMetricGenerator.RELATIVE_SUFIX);

    showAndSaveChart("Consolidated Severity", stage, chartSeries);
  }

  @Override
  public XYChart<String, Number> getChart() {
    CategoryAxis periodAxis = new CategoryAxis();
    periodAxis.setLabel(TIME_PERIOD_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(RELATIVE_FREQUENCY_LABEL);

    StackedAreaChart<String, Number> stackAreaChart = new StackedAreaChart<String, Number>(
        periodAxis, counterAxis);
    return stackAreaChart;
  }
}
