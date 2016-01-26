package crest.jira.data.miner.chart.issue;

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
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

public abstract class ReleasesToFixByPriorityChart extends AbstractChart<String, Number> {

  private static final String CHART_TITLE = "Releases to Fix for Priority ";
  private static final int BIN_COUNT = 10;
  private static final String INPUT_CSV_FILE = "Issues_for_Board_2_1450137002091";
  private String priorityValue;
  private String priorityName;

  public ReleasesToFixByPriorityChart(int priorityIndex) {
    priorityValue = JiraCsvConfiguration.PRIORITIES[priorityIndex];
    priorityName = JiraCsvConfiguration.PRIORITY_DESCRIPTIONS[priorityIndex];
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  private void buildChart(Stage stage) throws IOException {
    String valueIdentifier = JiraCsvConfiguration.RELEASES_TO_FIX_SUFFIX;
    Predicate<CSVRecord> validRecordPredicate = new Predicate<CSVRecord>() {
      @Override
      public boolean evaluate(CSVRecord csvRecord) {
        String recordPriority = csvRecord.get(JiraCsvConfiguration.ORIGINAL_PRIORITY);
        String dataPoint = csvRecord.get(valueIdentifier);
        return recordPriority.equals(priorityValue) && !StringUtils.isEmpty(dataPoint);
      }
    };

    /*
     * List<Series<String, Number>> chartSeries =
     * getSeriesForHistogramUsingBins(getCsvFileLocation(), valueIdentifier,
     * BIN_COUNT, validRecordPredicate);
     */
    List<Series<String, Number>> chartSeries = getSeriesForHistogram(getCsvFileLocation(),
        valueIdentifier, BIN_COUNT, validRecordPredicate);

    showAndSaveChart(CHART_TITLE + priorityName, stage, chartSeries);

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
