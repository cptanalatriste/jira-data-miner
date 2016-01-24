package crest.jira.data.miner.chart.user;

import crest.jira.data.miner.chart.AbstractChart;
import crest.jira.data.miner.csv.JiraCsvConfiguration;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPriorityAssignmentsChart extends AbstractChart<Number, Number> {

  private static final String BAG_IDENTIFIER_REGEX = "^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+).*$";
  private static final String CHART_TITLE = "Priority assignments";
  private static final double MIN_PARTICIPATION = 10;
  // private static final double MIN_PARTICIPATION = Double.MIN_VALUE;

  public static void main(String... args) {
    launch(args);
  }

  @Override
  public String getFile() {
    return UserParticipationChart.INPUT_CSV_FILE + CSV_EXTENSION;
  }

  @Override
  public void start(Stage stage) throws Exception {
    buildChart(stage);
  }

  private void buildChart(Stage stage) throws IOException {

    List<Series<Number, Number>> chartSeries = getSeriesForScatterPlot(getCsvFileLocation(),
        JiraCsvConfiguration.NON_SEVERE_IDENTIFIER, JiraCsvConfiguration.SEVERE_IDENTIFIER);
    showAndSaveChart(CHART_TITLE, stage, chartSeries);

  }

  private List<Series<Number, Number>> getSeriesForScatterPlot(String fileName,
      String nonSevereIdentifier, String severeIdentifier) {

    List<Series<Number, Number>> seriesAsList = new ArrayList<>();
    List<CSVRecord> records = getCsvRecords(fileName);

    for (int index = 0; index < records.size(); index += 1) {
      CSVRecord record = records.get(index);
      String reporterName = record.get(JiraCsvConfiguration.USER_IDENTIFIER);
      double participation = Double
          .parseDouble(record.get(JiraCsvConfiguration.PARTICIPATIONS_IDENTIFIER));

      if (participation < MIN_PARTICIPATION) {
        continue;
      }

      Series<Number, Number> series = new Series<>();
      series.setName(reporterName);

      Map<String, Data<Number, Number>> periodInformation = new HashMap<>();

      for (String headerValue : getCsvHeaderMap(fileName).keySet()) {

        if (headerValue.matches(BAG_IDENTIFIER_REGEX)) {

          String period = headerValue.substring(0, 5);
          String metric = headerValue.substring(6);
          String value = record.get(headerValue);

          if (periodInformation.get(period) == null) {
            periodInformation.put(period, new Data<>());
          }

          double valueAsDouble = Double.isNaN(Double.parseDouble(value)) ? 0.0
              : Double.parseDouble(value);

          if (JiraCsvConfiguration.NON_SEVERE_IDENTIFIER.equals(metric)) {
            periodInformation.get(period).setXValue(valueAsDouble);
          } else if (JiraCsvConfiguration.SEVERE_IDENTIFIER.equals(metric)) {
            periodInformation.get(period).setYValue(valueAsDouble);
          }
        }
      }

      series.getData().addAll(periodInformation.values());
      seriesAsList.add(series);
    }

    return seriesAsList;
  }

  @Override
  public XYChart<Number, Number> getChart() {
    NumberAxis nonSevereAxis = new NumberAxis(-0.1, 1.2, 0.1);
    nonSevereAxis.setLabel(NON_SEVERE_ASSIGNMENTS_LABEL);

    NumberAxis severeAxis = new NumberAxis(-0.1, 1.2, 0.1);
    severeAxis.setLabel(SEVERE_ASSIGNMENTS_LABEL);

    ScatterChart<Number, Number> scatterChart = new ScatterChart<Number, Number>(nonSevereAxis,
        severeAxis);
    return scatterChart;
  }

}
