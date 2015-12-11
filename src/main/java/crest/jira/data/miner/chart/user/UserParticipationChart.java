package crest.jira.data.miner.chart.user;

import crest.jira.data.miner.chart.AbstractChart;
import crest.jira.data.miner.report.model.CsvConfiguration;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserParticipationChart extends AbstractChart<String, Number> {

  private static final String INPUT_CSV_FILE = "Reporters_Board_2_1449825659783";
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
    List<Series<String, Number>> chartSeries = getSeriesForHistogram(getCsvFileLocation(),
        CsvConfiguration.PARTICIPATIONS_IDENTIFIER);
    showAndSaveChart(CHART_TITLE, stage, chartSeries);
  }

  @Override
  public XYChart<String, Number> getChart() {
    CategoryAxis rangeAxis = new CategoryAxis();
    rangeAxis.setLabel(RANGE_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(FREQUENCY_LABEL);

    BarChart<String, Number> barChart = new BarChart<>(rangeAxis, counterAxis);
    return barChart;
  }

  @Override
  public String getCsvFileLocation() {
    return DIRECTORY + INPUT_CSV_FILE + CSV_EXTENSION;
  }

  /**
   * Return chart series for a bin-based histogram.
   * 
   * @param fileName
   *          Input file.
   * @param valueIdentifier
   *          Column with the data.
   * @return Series for the histogram.
   */
  public List<Series<String, Number>> getSeriesForHistogram(String fileName,
      String valueIdentifier) {

    EmpiricalDistribution empiricalDistributon = new EmpiricalDistribution(BIN_COUNT);
    double[] dataPoints = null;

    List<CSVRecord> records = getCsvRecords(fileName);
    dataPoints = new double[records.size()];

    for (int index = 0; index < records.size(); index += 1) {
      CSVRecord csvRecord = records.get(index);
      // TODO(cgavidia): Enable muliple support later, if required.
      // TODO(cgavidia): And change the String with Enums :S
      Double dataPoint = Double.parseDouble(csvRecord.get(valueIdentifier));
      if (!dataPoint.isNaN()) {
        dataPoints[index] = dataPoint;
      }
    }

    empiricalDistributon.load(dataPoints);
    Series<String, Number> histogramSeries = new Series<String, Number>();
    // TODO(cgavidia): Enable muliple support later if required.
    histogramSeries.setName(valueIdentifier);

    List<SummaryStatistics> binStats = empiricalDistributon.getBinStats();
    double[] upperBounds = empiricalDistributon.getUpperBounds();

    for (int index = 0; index < binStats.size(); index += 1) {
      SummaryStatistics binStatistics = binStats.get(index);

      long frequency = binStatistics.getN();
      double lowerBound = index > 0 ? upperBounds[index - 1] : 0;
      String binLabel = String.format("%.2f", lowerBound) + " - "
          + String.format("%.2f", upperBounds[index]);

      Data<String, Number> data = new Data<>(binLabel, frequency);
      histogramSeries.getData().add(data);
    }

    List<Series<String, Number>> seriesAsList = new ArrayList<>();
    // TODO(cgavidia): Enable muliple support later.
    seriesAsList.add(histogramSeries);
    return seriesAsList;
  }

}
