package crest.jira.data.miner;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import crest.jira.data.miner.config.ConfigurationProvider;
import crest.jira.data.miner.csv.BaseCsvGenerator;
import crest.jira.data.miner.csv.CsvExportSupport;
import crest.jira.data.miner.db.JiraIssueListDao;
import crest.jira.data.miner.report.model.ExtendedIssue;
import crest.jira.data.miner.report.model.JiraIssueBag;
import crest.jira.data.miner.report.model.ReleaseDateComparator;
import crest.jira.data.miner.report.model.UserJiraIssueBag;
import crest.jira.data.retriever.model.Board;
import crest.jira.data.retriever.model.User;
import crest.jira.data.retriever.model.Version;

import org.apache.commons.collections4.MultiValuedMap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GenerateConsolidatedCsvFiles extends BaseCsvGenerator {

  private static final String REPORTERS_FILE_PREFIX = "Reporters_Board_";
  private static final String ISSUES_FILE_PREFIX = "Issues_for_Board_";
  private static final String RELEASE_FILE_PREFIX = "Board_";
  private static final String ALLBOARDS_KEY = "ALLBOARDS";
  public static final String FOLDER_NAME = "C:/Users/cgavi/OneDrive/phd2/jira_data/";

  private static Dao<Board, String> boardDao;

  public GenerateConsolidatedCsvFiles() {
    super(FOLDER_NAME);
  }

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

    GenerateConsolidatedCsvFiles generator = new GenerateConsolidatedCsvFiles();

    generator.processAllBoards(connectionSource);

    // boardDao = DaoManager.createDao(connectionSource, Board.class);
    // List<Board> allBoards = boardDao.queryForAll();
    // for (Board board : allBoards) {
    // generator.processBoard(connectionSource, board.getId(), true);
    // }

    generator.processBoard(connectionSource, "2", true);
  }

  private void processAllBoards(ConnectionSource connectionSource)
      throws SQLException, IOException {
    JiraIssueListDao analyser = new JiraIssueListDao(connectionSource);
    analyser.loadAllBugs();

    MultiValuedMap<String, ExtendedIssue> issuesPerBoard = analyser.organizeInBoards();
    List<JiraIssueBag<String>> issueBags = getBagsPerTimePeriod(issuesPerBoard,
        new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            return o1.compareTo(o2);
          }
        });

    generateCsvFile(ALLBOARDS_KEY, issueBags);
  }

  private void processBoard(ConnectionSource connectionSource, String boardId, boolean onlyBugs)
      throws SQLException, IOException {
    JiraIssueListDao issueListDao = new JiraIssueListDao(connectionSource);
    issueListDao.loadBoardIssues(boardId, onlyBugs);
    generateCsvFile(ISSUES_FILE_PREFIX + boardId, issueListDao.getIssueList());

    MultiValuedMap<Version, ExtendedIssue> issuesInGroups = issueListDao.organizeInReleases();
    List<JiraIssueBag<Version>> issueBags = getBagsPerTimePeriod(issuesInGroups,
        new ReleaseDateComparator());

    generateCsvFile(RELEASE_FILE_PREFIX + boardId, issueBags);

    Object[] reporters = issueListDao.getReporterCatalogPerBoard(boardId).toArray();
    List<CsvExportSupport> userIssueBags = getBagsPerUser(reporters, issueBags);
    generateCsvFile(REPORTERS_FILE_PREFIX + boardId, userIssueBags);

  }

  private static <T> List<CsvExportSupport> getBagsPerUser(Object[] reporters,
      List<JiraIssueBag<T>> issueBags) {

    if (reporters.length < 1) {
      throw new RuntimeException("The user catalog is empty");
    }

    List<CsvExportSupport> userIssueBags = new ArrayList<>();

    for (Object reporterAsObject : reporters) {
      User reporter = (User) reporterAsObject;
      UserJiraIssueBag<T> userJiraIssueBag = new UserJiraIssueBag<T>(reporter);
      for (JiraIssueBag<T> issueBag : issueBags) {
        userJiraIssueBag.addBag(issueBag);
      }

      userIssueBags.add(userJiraIssueBag);
    }

    return userIssueBags;
  }

  private static <T> List<JiraIssueBag<T>> getBagsPerTimePeriod(
      MultiValuedMap<T, ExtendedIssue> issuesPerKey, Comparator<T> comparator) throws IOException {

    List<JiraIssueBag<T>> issueBags = new ArrayList<>();

    List<T> keysAsCollection = new ArrayList<T>(issuesPerKey.keySet());
    Collections.sort(keysAsCollection, comparator);

    for (T oneKey : keysAsCollection) {
      T key = oneKey;

      List<ExtendedIssue> listOfIssues = (List<ExtendedIssue>) issuesPerKey.get(oneKey);
      JiraIssueBag<T> issueBag = new JiraIssueBag<T>(key, listOfIssues);
      issueBags.add(issueBag);
    }

    return issueBags;
  }

}
