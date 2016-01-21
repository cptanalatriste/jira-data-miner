package crest.jira.data.miner.chart;

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

import org.apache.commons.collections4.Predicate;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public abstract class AbstractChart<X, Y> extends Application {

  private static Logger logger = Logger.getLogger(AbstractChart.class.getName());

  public static final String DIRECTORY = "C:/Users/cgavi/OneDrive/phd2/jira_data/";

  private static final String FILE_NAME = "Board_2_1450137002550";
  public static final String CSV_EXTENSION = ".csv";
  private static final String PNG_EXTENSION = ".png";

  public static final String PERIOD = "Period";
  public static final String BOARD_LABEL = "Board";
  public static final String COUNT_LABEL = "Count";
  public static final String TIME_LABEL = "Time in Days";
  public static final String RANGE_LABEL = "Range";
  public static final String RELATIVE_FREQUENCY_LABEL = "Relative Frequency";
  public static final String NON_SEVERE_ASSIGNMENTS_LABEL = "Non Severe (%)";
  public static final String SEVERE_ASSIGNMENTS_LABEL = "Severe (%)";

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

    List<CSVRecord> records = getCsvRecords(fileName);

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

    for (String label : valueIdentifiers) {
      seriesAsList.add(series.get(label));
    }

    return seriesAsList;
  }

  protected List<CSVRecord> getCsvRecords(String fileName) {
    List<CSVRecord> csvRecords = null;
    CSVParser csvParser = null;
    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();

    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
      csvParser = new CSVParser(bufferedReader, csvFormat);
      csvRecords = csvParser.getRecords();
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return csvRecords;
  }

  protected Map<String, Integer> getCsvHeaderMap(String fileName) {
    Map<String, Integer> headerMap = null;

    CSVParser csvParser = null;
    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();

    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
      csvParser = new CSVParser(bufferedReader, csvFormat);
      headerMap = csvParser.getHeaderMap();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return headerMap;
  }

  public abstract XYChart<X, Y> getChart();

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
  public void showAndSaveChart(String title, Stage stage, List<Series<X, Y>> chartSeries)
      throws IOException {
    XYChart<X, Y> chart = this.getChart();

    for (Series<X, Y> oneSerie : chartSeries) {
      chart.getData().add(oneSerie);
    }

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
   * Return chart series for a bin-based histogram.
   * 
   * @param fileName
   *          Input file.
   * @param valueIdentifier
   *          Column with the data.
   * @return Series for the histogram.
   */
  public List<Series<String, Number>> getSeriesForHistogramUsingBins(String fileName,
      String valueIdentifier, int binCount, Predicate<CSVRecord> isValid) {

    EmpiricalDistribution empiricalDistributon = new EmpiricalDistribution(binCount);
    List<Double> dataPoints = new ArrayList<>();

    List<CSVRecord> records = getCsvRecords(fileName);

    for (int index = 0; index < records.size(); index += 1) {
      CSVRecord csvRecord = records.get(index);

      if (isValid == null || isValid.evaluate(csvRecord)) {
        Double dataPoint = Double.parseDouble(csvRecord.get(valueIdentifier));
        dataPoints.add(dataPoint);
      }
    }

    logger.info("dataPoints.size() " + dataPoints.size());

    empiricalDistributon
        .load(ArrayUtils.toPrimitive(dataPoints.toArray(new Double[dataPoints.size()])));
    Series<String, Number> histogramSeries = new Series<String, Number>();
    histogramSeries.setName(valueIdentifier);

    List<SummaryStatistics> binStats = empiricalDistributon.getBinStats();
    double[] upperBounds = empiricalDistributon.getUpperBounds();

    for (int index = 0; index < binStats.size(); index += 1) {
      SummaryStatistics binStatistics = binStats.get(index);

      long frequency = binStatistics.getN();
      double lowerBound = index > 0 ? upperBounds[index - 1] : binStatistics.getMin();
      String binLabel = String.format("%.2f", lowerBound) + " - "
          + String.format("%.2f", upperBounds[index]);

      Data<String, Number> data = new Data<>(binLabel, frequency);
      histogramSeries.getData().add(data);
    }

    List<Series<String, Number>> seriesAsList = new ArrayList<>();
    seriesAsList.add(histogramSeries);
    return seriesAsList;
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
