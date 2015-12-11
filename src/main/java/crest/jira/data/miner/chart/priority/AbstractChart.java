package crest.jira.data.miner.chart.priority;

import crest.jira.data.miner.report.model.CsvConfiguration;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public abstract class AbstractChart extends Application {

  public static final String DIRECTORY = "C:/Users/cgavi/OneDrive/phd2/jira_data/";
  private static final String FILE_NAME = "Board_ALLBOARDS_1448807375714";
  public static final String CSV_EXTENSION = ".csv";
  private static final String PNG_EXTENSION = ".png";

  public static final String TIME_PERIOD_LABEL = "Time Period";
  public static final String BOARD_LABEL = "Board";
  public static final String FREQUENCY_LABEL = "Frequency";
  public static final String TIME_LABEL = "Time in Days";
  public static final String RANGE_LABEL = "Range";
  public static final String RELATIVE_FREQUENCY_LABEL = "Relative Frequency";

  public static void main(String... args) {
    launch(args);
  }

  public String getCsvFileLocation() {
    return DIRECTORY + getFile();
  }

  public String getFile() {
    return FILE_NAME + CSV_EXTENSION;
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
  public List<Series<String, Number>> getSeries(String fileName, String recordIdentifier,
      String... valueIdentifiers) {
    Map<String, Series<String, Number>> series = new HashMap<String, Series<String, Number>>();
    List<Series<String, Number>> seriesAsList = new ArrayList<>();

    for (String valueIdentifier : valueIdentifiers) {
      Series<String, Number> serie = new Series<String, Number>();
      serie.setName(valueIdentifier);
      series.put(valueIdentifier, serie);
    }

    CSVParser csvParser = null;

    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();

    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {

      csvParser = new CSVParser(bufferedReader, csvFormat);
      List<CSVRecord> records = csvParser.getRecords();

      for (int index = 0; index < records.size(); index += 1) {
        CSVRecord csvRecord = records.get(index);

        String identifierValue = csvRecord.get(recordIdentifier);

        for (String valueIdentifier : valueIdentifiers) {
          Double numericValue = Double.parseDouble(csvRecord.get(valueIdentifier));

          if (!numericValue.isNaN()) {
            series.get(valueIdentifier).getData().add(new Data<>(identifierValue, numericValue));
          }
        }
      }

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    for (String label : valueIdentifiers) {
      seriesAsList.add(series.get(label));
    }

    return seriesAsList;
  }

  public abstract XYChart<String, Number> getChart();

  /**
   * Assigns a title to a chart and shows it on the Screen.
   * 
   * @param title
   *          Title.
   * @param stage
   *          Stage.
   * @throws IOException
   *           Image writing might fail.
   */
  public void showAndSaveChart(String title, Stage stage, List<Series<String, Number>> chartSeries)
      throws IOException {
    XYChart<String, Number> chart = this.getChart();
    chart.getData().addAll(chartSeries);

    String chartTitle = title + " - " + getFile();
    chart.setTitle(chartTitle);

    Scene scene = new Scene(chart, 800, 600);
    stage.setScene(scene);
    stage.setMaximized(true);
    stage.show();

    WritableImage writableImage = chart.snapshot(new SnapshotParameters(), null);
    File file = new File(DIRECTORY + chartTitle + PNG_EXTENSION);
    ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
  }

  /**
   * Return a list of Priority-Related CSV keys.
   * 
   * @param suffix
   *          Suffix.
   * @return List of CSV keys.
   */
  public static String[] getPriorityLabelsBySuffix(String suffix) {
    List<String> labels = new ArrayList<>();

    for (String priority : CsvConfiguration.PRIORITIES) {
      labels.add(CsvConfiguration.PRIORITY_DESCRIPTIONS[Integer.parseInt(priority)] + suffix);
    }

    return labels.toArray(new String[labels.size()]);
  }

}
