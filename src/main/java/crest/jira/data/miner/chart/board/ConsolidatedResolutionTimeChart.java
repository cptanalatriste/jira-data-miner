package crest.jira.data.miner.chart.board;

import crest.jira.data.miner.chart.AbstractChart;
import crest.jira.data.miner.report.model.CsvConfiguration;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ConsolidatedResolutionTimeChart extends AbstractChart<String, Number> {

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
        CsvConfiguration.NON_SEVERE_IDENTIFIER + CsvConfiguration.RESTIME_MED_SUFFIX,
        CsvConfiguration.PRIORITY_DESCRIPTIONS[3] + CsvConfiguration.RESTIME_MED_SUFFIX,
        CsvConfiguration.SEVERE_IDENTIFIER + CsvConfiguration.RESTIME_MED_SUFFIX);

    showAndSaveChart("Consolidated Resolution Time", stage, chartSeries);
  }

  @Override
  public XYChart<String, Number> getChart() {
    CategoryAxis periodAxis = new CategoryAxis();
    periodAxis.setLabel(BOARD_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(TIME_LABEL);

    BarChart<String, Number> barChart = new BarChart<String, Number>(periodAxis, counterAxis);
    return barChart;
  }

}
