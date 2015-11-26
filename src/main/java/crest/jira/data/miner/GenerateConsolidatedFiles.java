package crest.jira.data.miner;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import crest.jira.data.miner.config.ConfigurationProvider;
import crest.jira.data.miner.db.JiraIssuesPerBoardDao;
import crest.jira.data.miner.report.model.ExtendedIssue;
import crest.jira.data.miner.report.model.IssueListMetricGenerator;
import crest.jira.data.retriever.model.Board;

import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GenerateConsolidatedFiles {

  private static final String FOLDER_NAME = "C:/Users/cgavi/OneDrive/phd2/jira_data/";
  private static final String NEW_LINE_SEPARATOR = "\n";

  private static Dao<Board, String> boardDao;

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
  public static void main(String[] args) throws SQLException, SecurityException, IOException {

    ConfigurationProvider configProvider = new ConfigurationProvider();
    ConnectionSource connectionSource = configProvider.getConnectionSource();

    boardDao = DaoManager.createDao(connectionSource, Board.class);
    List<Board> allBoards = boardDao.queryForAll();
    for (Board board : allBoards) {
      String boardId = board.getId();

      // TODO(cgavidia): Only for testing
      if ("2".equals(boardId)) {
        JiraIssuesPerBoardDao analyser = new JiraIssuesPerBoardDao(boardId, connectionSource);
        analyser.loadIssues();

        MultiValueMap<String, ExtendedIssue> issuesPerTimeFrame = analyser.organizeTimeFrames();

        Object[] keysAsArray = issuesPerTimeFrame.keySet().toArray();
        Arrays.sort(keysAsArray);

        generateCsvFile(boardId, keysAsArray, issuesPerTimeFrame);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void generateCsvFile(String boardId, Object[] keysAsArray,
      MultiValueMap<String, ExtendedIssue> issuesPerTimeFrame) throws IOException {

    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
    BufferedWriter bufferedWriter = new BufferedWriter(
        new FileWriter(FOLDER_NAME + "Board_" + boardId + "_" + new Date().getTime() + ".csv"));
    try (CSVPrinter csvPrinter = new CSVPrinter(bufferedWriter, csvFileFormat)) {

      csvPrinter.printRecord(Arrays.copyOf(IssueListMetricGenerator.getMetricHeader(),
          IssueListMetricGenerator.getMetricHeader().length, Object[].class));

      for (Object oneKey : keysAsArray) {
        String key = (String) oneKey;

        List<ExtendedIssue> listOfIssues = (List<ExtendedIssue>) issuesPerTimeFrame.get(oneKey);
        IssueListMetricGenerator metrics = new IssueListMetricGenerator(key, listOfIssues);

        csvPrinter.printRecord(metrics.getMetricsAsList());
      }

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
