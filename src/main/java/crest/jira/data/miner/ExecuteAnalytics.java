package crest.jira.data.miner;

import crest.jira.data.miner.config.BriefFormatter;
import crest.jira.data.miner.config.ConfigurationProvider;
import crest.jira.data.miner.report.JiraIssueAnalyzer;
import crest.jira.data.miner.report.model.ExtendedIssue;
import crest.jira.data.miner.report.model.IssueListMetrics;

import org.apache.commons.collections4.map.MultiValueMap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class ExecuteAnalytics {

  private static final String BOARD_ID = "25";
  private static Logger logger = Logger.getLogger(JiraDataExtractor.class.getName());

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

    configureLogger();

    ConfigurationProvider configProvider = new ConfigurationProvider();
    JiraIssueAnalyzer analyser = new JiraIssueAnalyzer(BOARD_ID,
        configProvider.getConnectionSource());
    analyser.loadIssues();

    MultiValueMap<String, ExtendedIssue> issuesPerTimeFrame = analyser.organizeTimeFrames();

    Object[] keysAsArray = issuesPerTimeFrame.keySet().toArray();
    Arrays.sort(keysAsArray);

    logger.info(IssueListMetrics.getMetricHeader());

    for (Object oneKey : keysAsArray) {
      String key = (String) oneKey;

      List<ExtendedIssue> listOfIssues = (List<ExtendedIssue>) issuesPerTimeFrame.get(oneKey);
      IssueListMetrics metrics = new IssueListMetrics(key, listOfIssues);
      logger.info(metrics.getMetricsAsString());
    }
  }

  private static void configureLogger() throws SecurityException, IOException {
    FileHandler fileHandler = new FileHandler(
        "C:/Users/cgavi/OneDrive/phd2/jira_data/analyzer.csv");
    logger.addHandler(fileHandler);
    BriefFormatter simpleFormatter = new BriefFormatter();
    fileHandler.setFormatter(simpleFormatter);
  }

}
