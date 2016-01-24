package crest.jira.data.miner.chart.user;

import crest.jira.data.miner.chart.AbstractChart;
import crest.jira.data.miner.csv.JiraCsvConfiguration;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.List;

public class UserParticipationChart extends AbstractChart<String, Number> {

  static final String INPUT_CSV_FILE = "Reporters_Board_2_1449954679787";
  private static final String CHART_TITLE = "Reporter participation";
  private static final int BIN_COUNT = 10;

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  private void buildChart(Stage stage) throws IOException {
    String valueIdentifier = JiraCsvConfiguration.PARTICIPATIONS_IDENTIFIER;
    Predicate<CSVRecord> notNanPredicate = new Predicate<CSVRecord>() {
      @Override
      public boolean evaluate(CSVRecord csvRecord) {
        Double dataPoint = Double.parseDouble(csvRecord.get(valueIdentifier));
        return !dataPoint.isNaN();
      }
    };
    List<Series<String, Number>> chartSeries = getSeriesForHistogramUsingBins(getCsvFileLocation(),
        valueIdentifier, BIN_COUNT, notNanPredicate);
    showAndSaveChart(CHART_TITLE, stage, chartSeries);
  }

  @Override
  public XYChart<String, Number> getChart() {
    CategoryAxis rangeAxis = new CategoryAxis();
    rangeAxis.setLabel(RANGE_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(COUNT_LABEL);

    BarChart<String, Number> barChart = new BarChart<>(rangeAxis, counterAxis);
    return barChart;
  }

  @Override
  public String getFile() {
    return INPUT_CSV_FILE + CSV_EXTENSION;
  }



}
