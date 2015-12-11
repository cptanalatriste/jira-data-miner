package crest.jira.data.miner;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import crest.jira.data.miner.config.ConfigurationProvider;
import crest.jira.data.miner.db.JiraIssueListDao;
import crest.jira.data.miner.report.model.CsvExportSupport;
import crest.jira.data.miner.report.model.ExtendedIssue;
import crest.jira.data.miner.report.model.JiraIssueBag;
import crest.jira.data.miner.report.model.UserJiraIssueBag;
import crest.jira.data.retriever.model.Board;
import crest.jira.data.retriever.model.User;

import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GenerateConsolidatedFiles {

  private static final String ALLBOARDS_KEY = "ALLBOARDS";
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

    processAllBoards(connectionSource);

    boardDao = DaoManager.createDao(connectionSource, Board.class);
    List<Board> allBoards = boardDao.queryForAll();
    for (Board board : allBoards) {
      processBoard(connectionSource, board.getId(), true);
    }

    //processBoard(connectionSource, "2", true);
  }

  private static void processAllBoards(ConnectionSource connectionSource)
      throws SQLException, IOException {
    JiraIssueListDao analyser = new JiraIssueListDao(connectionSource);
    analyser.loadAllBugs();

    MultiValueMap<String, ExtendedIssue> issuesPerBoard = analyser.organizeInBoards();
    getBagsPerTimePeriod(ALLBOARDS_KEY, issuesPerBoard);

  }

  private static void processBoard(ConnectionSource connectionSource, String boardId,
      boolean onlyBugs) throws SQLException, IOException {
    JiraIssueListDao issueListDao = new JiraIssueListDao(connectionSource);
    issueListDao.loadBoardIssues(boardId, onlyBugs);

    MultiValueMap<String, ExtendedIssue> issuesPerTimeFrame = issueListDao.organizeInTimeFrames();
    List<CsvExportSupport> issueBags = getBagsPerTimePeriod(boardId, issuesPerTimeFrame);
    generateCsvFile("Board_" + boardId, issueBags);

    Object[] periodKeys = issuesPerTimeFrame.keySet().toArray();
    Arrays.sort(periodKeys);

    Object[] reporters = issueListDao.getReporterCatalogPerBoard(boardId).toArray();

    List<CsvExportSupport> userIssueBags = getBagsPerUser(reporters, issueBags);
    generateCsvFile("Reporters_Board_" + boardId, userIssueBags);

  }

  private static List<CsvExportSupport> getBagsPerUser(Object[] reporters,
      List<CsvExportSupport> issueBags) {

    if (reporters.length < 1) {
      throw new RuntimeException("The user catalog is empty");
    }

    List<CsvExportSupport> userIssueBags = new ArrayList<>();

    for (Object reporterAsObject : reporters) {
      User reporter = (User) reporterAsObject;
      UserJiraIssueBag userJiraIssueBag = new UserJiraIssueBag(reporter);
      for (CsvExportSupport issueBag : issueBags) {
        userJiraIssueBag.addBag((JiraIssueBag) issueBag);
      }

      userIssueBags.add(userJiraIssueBag);
    }

    return userIssueBags;
  }

  @SuppressWarnings("unchecked")
  private static List<CsvExportSupport> getBagsPerTimePeriod(String groupId,
      MultiValueMap<String, ExtendedIssue> issuesPerKey) throws IOException {

    List<CsvExportSupport> issueBags = new ArrayList<>();

    Object[] keysAsArray = issuesPerKey.keySet().toArray();
    Arrays.sort(keysAsArray);

    for (Object oneKey : keysAsArray) {
      String key = (String) oneKey;

      List<ExtendedIssue> listOfIssues = (List<ExtendedIssue>) issuesPerKey.get(oneKey);
      JiraIssueBag issueBag = new JiraIssueBag(key, listOfIssues);
      issueBags.add(issueBag);
    }

    return issueBags;
  }

  private static void generateCsvFile(String filePrefix, List<CsvExportSupport> rows)
      throws IOException {

    if (rows.size() < 1) {
      throw new RuntimeException(
          "No rows sent while trying to create file with prefix " + filePrefix);
    }

    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
    BufferedWriter bufferedWriter = new BufferedWriter(
        new FileWriter(FOLDER_NAME + filePrefix + "_" + new Date().getTime() + ".csv"));

    try (CSVPrinter csvPrinter = new CSVPrinter(bufferedWriter, csvFileFormat)) {

      csvPrinter.printRecord(Arrays.copyOf(rows.get(0).getCsvHeader(),
          rows.get(0).getCsvHeader().length, Object[].class));

      for (CsvExportSupport row : rows) {
        csvPrinter.printRecord(row.getCsvRecord());
      }

    } catch (Exception ex) {
      ex.printStackTrace();
    }

  }

}
