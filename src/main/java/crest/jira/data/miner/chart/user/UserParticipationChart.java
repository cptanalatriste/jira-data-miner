package crest.jira.data.miner.chart.user;

import crest.jira.data.miner.chart.priority.AbstractChart;
import crest.jira.data.miner.report.model.CsvConfiguration;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserParticipationChart extends AbstractChart {

  private static final String INPUT_CSV_FILE = "Reporters_Board_2_1449692755822";
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
    List<Series<String, Number>> chartSeries = getSeries(getCsvFileLocation(),
        CsvConfiguration.TIME_PERIOD_IDENTIFIER, CsvConfiguration.PARTICIPATIONS_IDENTIFIER);
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
  public String getFile() {
    return INPUT_CSV_FILE + CSV_EXTENSION;
  }

  @Override
  public List<Series<String, Number>> getSeries(String fileName, String recordIdentifier,
      String... valueIdentifiers) {

    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
    CSVParser csvParser = null;

    EmpiricalDistribution empiricalDistributon = new EmpiricalDistribution(BIN_COUNT);
    double[] dataPoints = null;

    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(getCsvFileLocation()))) {

      csvParser = new CSVParser(bufferedReader, csvFormat);
      List<CSVRecord> records = csvParser.getRecords();
      dataPoints = new double[records.size()];

      for (int index = 0; index < records.size(); index += 1) {
        CSVRecord csvRecord = records.get(index);
        // TODO(cgavidia): Enable muliple support later.
        // TODO(cgavidia): And change the String with Enums :S
        Double dataPoint = Double.parseDouble(csvRecord.get(valueIdentifiers[0]));
        if (!dataPoint.isNaN()) {
          dataPoints[index] = dataPoint;
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    empiricalDistributon.load(dataPoints);
    Series<String, Number> histogramSeries = new Series<String, Number>();
    // TODO(cgavidia): Enable muliple support later.
    histogramSeries.setName(valueIdentifiers[0]);

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
