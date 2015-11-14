package crest.jira.data.miner;

import crest.jira.data.miner.config.ConfigurationProvider;
import crest.jira.data.miner.report.JiraIssueAnalyzer;
import crest.jira.data.miner.report.model.ExtendedIssue;
import crest.jira.data.miner.report.model.IssueListMetrics;

import org.apache.commons.collections4.map.MultiValueMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ExecuteAnalytics {

  private static final String FILENAME = "C:/Users/cgavi/OneDrive/phd2/jira_data/analyzer.csv";
  private static final String BOARD_ID = "25";
  // private static Logger logger =
  // Logger.getLogger(JiraDataExtractor.class.getName());

  /**
   * Analyzes the Testers moves per time frame. *
   * 
   * @param args
   *          Not used.
   * @throws SQLException
   *           In case of DB exceptions.
   * @throws IOException
   *           In case of logging issues.
   * @throws SecurityException
   *           In case of logging issues.
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws SQLException, SecurityException, IOException {

    ConfigurationProvider configProvider = new ConfigurationProvider();
    JiraIssueAnalyzer analyser = new JiraIssueAnalyzer(BOARD_ID,
        configProvider.getConnectionSource());
    analyser.loadIssues();

    MultiValueMap<String, ExtendedIssue> issuesPerTimeFrame = analyser.organizeTimeFrames();

    Object[] keysAsArray = issuesPerTimeFrame.keySet().toArray();
    Arrays.sort(keysAsArray);

    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(FILENAME));
    try (PrintWriter printWriter = new PrintWriter(bufferedWriter)) {

      printWriter.println(IssueListMetrics.getMetricHeader());

      for (Object oneKey : keysAsArray) {
        String key = (String) oneKey;

        List<ExtendedIssue> listOfIssues = (List<ExtendedIssue>) issuesPerTimeFrame.get(oneKey);
        IssueListMetrics metrics = new IssueListMetrics(key, listOfIssues);
        printWriter.println(metrics.getMetricsAsString());
      }

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
