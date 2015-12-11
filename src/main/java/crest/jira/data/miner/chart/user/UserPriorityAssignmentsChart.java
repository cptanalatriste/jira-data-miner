package crest.jira.data.miner.chart.user;

import crest.jira.data.miner.chart.AbstractChart;
import crest.jira.data.miner.report.model.CsvConfiguration;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class UserPriorityAssignmentsChart extends AbstractChart<Number, Number> {

  private static final String CHART_TITLE = "Priority assignments";

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  private void buildChart(Stage stage) throws IOException {
    List<Series<Number, Number>> chartSeries = getSeriesForScatterPlot(getCsvFileLocation(),
        CsvConfiguration.NON_SEVERE_IDENTIFIER, CsvConfiguration.SEVERE_IDENTIFIER);
    showAndSaveChart(CHART_TITLE, stage, chartSeries);
  }

  private List<Series<Number, Number>> getSeriesForScatterPlot(String fileName,
      String nonSevereIdentifier, String severeIdentifier) {

    List<Series<Number, Number>> seriesAsList = new ArrayList<>();
    List<CSVRecord> records = getCsvRecords(fileName);

    for (int index = 0; index < records.size(); index += 1) {
    }

    return seriesAsList;
  }

  @Override
  public XYChart<Number, Number> getChart() {
    NumberAxis nonSevereAxis = new NumberAxis();
    nonSevereAxis.setLabel(NON_SEVERE_ASSIGNMENTS_LABEL);

    NumberAxis severeAxis = new NumberAxis();
    severeAxis.setLabel(SEVERE_ASSIGNMENTS_LABEL);

    ScatterChart<Number, Number> scatterChart = new ScatterChart<Number, Number>(nonSevereAxis,
        severeAxis);
    return scatterChart;
  }

}
