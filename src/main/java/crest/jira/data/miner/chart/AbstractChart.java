package crest.jira.data.miner.chart;

import crest.jira.data.miner.report.model.IssueListMetricGenerator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractChart extends Application {

  private static final String FILE_LOCATION = 
      "C:/Users/cgavi/OneDrive/phd2/jira_data/Board_25_1447791806508.csv";
  public static final String TIME_PERIOD_LABEL = "Time Period";
  public static final String FREQUENCY_LABEL = "Frequency";
  public static final String RELATIVE_FREQUENCY_LABEL = "Relative Frequency";

  public static void main(String... args) {
    launch(args);
  }

  public static String getCsvFileLocation() {
    return FILE_LOCATION;
  }

  /**
   * Produces series of values from a CSV file, to be used in Charts.
   * 
   * @param fileName
   *          Name and location of the CSV files.
   * @param recordIdentifier
   *          Identifier of each record.
   * @param valueIdentifiers
   *          Identifier of the values.
   * @return Map containing the series.
   */
  public static Map<String, Series<String, Number>> getSeries(String fileName,
      String recordIdentifier, String... valueIdentifiers) {
    Map<String, Series<String, Number>> series = new HashMap<String, Series<String, Number>>();

    for (String valueIdentifier : valueIdentifiers) {
      Series<String, Number> serie = new Series<String, Number>();
      serie.setName(valueIdentifier);
      series.put(valueIdentifier, serie);
    }

    CSVParser csvParser = null;
    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(IssueListMetricGenerator.getMetricHeader());

    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {

      csvParser = new CSVParser(bufferedReader, csvFormat);
      List<CSVRecord> records = csvParser.getRecords();

      for (int index = 1; index < records.size(); index += 1) {
        CSVRecord csvRecord = records.get(index);

        String identifierValue = csvRecord.get(recordIdentifier);
        for (String valueIdentifier : valueIdentifiers) {
          Double numericValue = Double.parseDouble(csvRecord.get(valueIdentifier));
          series.get(valueIdentifier).getData().add(new Data<>(identifierValue, numericValue));
        }
      }

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return series;
  }

  /**
   * Assigns a title to a chart and shows it on the Screen.
   * 
   * @param scatterChart
   *          Chart.
   * @param title
   *          Title.
   * @param stage
   *          Stage.
   */
  public static void showChart(XYChart<String, Number> scatterChart, String title, Stage stage) {
    scatterChart.setTitle(title);
    Scene scene = new Scene(scatterChart, 800, 600);
    stage.setScene(scene);
    stage.show();
  }
}
