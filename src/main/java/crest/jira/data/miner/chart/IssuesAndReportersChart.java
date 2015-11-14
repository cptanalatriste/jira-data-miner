package crest.jira.data.miner.chart;

import crest.jira.data.miner.report.model.IssueListMetricGenerator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;



public class IssuesAndReportersChart extends Application {

  private static final String FILE_LOCATION = 
      "C:/Users/cgavi/OneDrive/phd2/jira_data/Board_25_1447519453706.csv";
  private static final String FREQUENCY_LABEL = "Frequency";
  private static final String TIME_PERIOD_LABEL = "Time Period";

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  @SuppressWarnings("unchecked")
  private void buildChart(Stage stage) throws FileNotFoundException, IOException {

    Series<String, Number> numberOfIssuesSeries = new Series<String, Number>();
    numberOfIssuesSeries.setName("Number of Issues");

    Series<String, Number> numberOfReportersSeries = new Series<String, Number>();
    numberOfReportersSeries.setName("Number of Reporters");

    CSVParser csvParser = null;
    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(IssueListMetricGenerator.getMetricHeader());

    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_LOCATION))) {
      csvParser = new CSVParser(bufferedReader, csvFormat);
      List<CSVRecord> records = csvParser.getRecords();

      for (int index = 1; index < records.size(); index += 1) {
        CSVRecord csvRecord = records.get(index);

        String periodIdentifier = csvRecord.get("Period Identifier");
        int numberOfIssues = Integer.parseInt(csvRecord.get("Total"));
        int numberOfReporters = Integer.parseInt(csvRecord.get("Number of Reporters"));

        numberOfIssuesSeries.getData()
            .add(new Data<String, Number>(periodIdentifier, numberOfIssues));
        numberOfReportersSeries.getData()
            .add(new Data<String, Number>(periodIdentifier, numberOfReporters));

      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    CategoryAxis periodAxis = new CategoryAxis();
    periodAxis.setLabel(TIME_PERIOD_LABEL);

    NumberAxis counterAxis = new NumberAxis();
    counterAxis.setLabel(FREQUENCY_LABEL);

    ScatterChart<String, Number> scatterChart = new ScatterChart<String, Number>(periodAxis,
        counterAxis);
    scatterChart.setTitle("Issues and Reporters");
    scatterChart.getData().addAll(numberOfIssuesSeries, numberOfReportersSeries);

    Scene scene = new Scene(scatterChart, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

}
