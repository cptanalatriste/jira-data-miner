package crest.jira.data.miner.chart.priority;

import crest.jira.data.miner.report.model.CsvConfiguration;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class IssuesAndReportersChart extends AbstractChart {

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  private void buildChart(Stage stage) throws FileNotFoundException, IOException {

    List<Series<String, Number>> chartSeries = getSeries(getCsvFileLocation(),
        CsvConfiguration.TIME_PERIOD_IDENTIFIER, CsvConfiguration.TOTAL_IDENTIFIER,
        CsvConfiguration.NUMBER_REPORTERS_IDENTIFIER,
        CsvConfiguration.ISSUES_PER_REPORTER_IDENTIFIER);

    showAndSaveChart("Issues and Reporters", stage, chartSeries);
  }

  @Override
  public XYChart<String, Number> getChart() {
    CategoryAxis periodAxis = new CategoryAxis();
    periodAxis.setLabel(TIME_PERIOD_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(FREQUENCY_LABEL);

    LineChart<String, Number> lineChart = new LineChart<String, Number>(periodAxis, counterAxis);
    return lineChart;
  }

}
