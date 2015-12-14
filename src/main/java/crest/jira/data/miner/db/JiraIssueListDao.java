package crest.jira.data.miner.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import crest.jira.data.miner.report.model.ExtendedIssue;
import crest.jira.data.retriever.map.ResponseList;
import crest.jira.data.retriever.model.ChangeLogItem;
import crest.jira.data.retriever.model.FixVersionPerIssue;
import crest.jira.data.retriever.model.History;
import crest.jira.data.retriever.model.Issue;
import crest.jira.data.retriever.model.User;
import crest.jira.data.retriever.model.Version;
import crest.jira.data.retriever.model.VersionPerIssue;

import org.apache.commons.collections4.map.MultiValueMap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JiraIssueListDao {

  private static final String BOARD_KEY_PREFFIX = "BOARD-";
  private static final String BUG_ISSUE_TYPE = "1";

  private Dao<Issue, String> issueDao;
  private Dao<History, String> historyDao;
  private Dao<ChangeLogItem, String> changeLogItemDao;
  private Dao<Version, String> versionDao;
  private Dao<FixVersionPerIssue, String> fixVersionDao;
  private Dao<VersionPerIssue, String> affectedVersionDao;

  private List<ExtendedIssue> issueList = new ArrayList<ExtendedIssue>();

  /**
   * Manages the database access for Issue Analysis.
   * 
   * @param connectionSource
   *          Connection source.
   * @throws SQLException
   *           In case of SQL errors.
   */
  public JiraIssueListDao(ConnectionSource connectionSource) throws SQLException {
    this.issueDao = DaoManager.createDao(connectionSource, Issue.class);
    this.historyDao = DaoManager.createDao(connectionSource, History.class);
    this.changeLogItemDao = DaoManager.createDao(connectionSource, ChangeLogItem.class);
    this.versionDao = DaoManager.createDao(connectionSource, Version.class);
    this.fixVersionDao = DaoManager.createDao(connectionSource, FixVersionPerIssue.class);
    this.affectedVersionDao = DaoManager.createDao(connectionSource, VersionPerIssue.class);
  }

  /**
   * Returns all the Bugs present in the JIRA Issue Database.
   * 
   * @throws SQLException
   *           Constructing SQL can produce errors.
   */
  public void loadAllBugs() throws SQLException {
    QueryBuilder<Issue, String> queryBuilder = issueDao.queryBuilder();
    Where<Issue, String> whereClause = queryBuilder.where();
    whereClause.eq("issueTypeId", BUG_ISSUE_TYPE);
    PreparedQuery<Issue> preparedQuery = queryBuilder.prepare();

    List<Issue> issuesFromDb = issueDao.query(preparedQuery);
    loadHistoryForIssueList(issuesFromDb);
  }

  /**
   * Loads the Issues that will be considered in analysis.
   * 
   * @throws SQLException
   *           In case of problems.
   */
  public void loadBoardIssues(String boardId, boolean onlyBugs, Object... reporters)
      throws SQLException {
    QueryBuilder<Issue, String> queryBuilder = issueDao.queryBuilder();
    Where<Issue, String> whereClause = queryBuilder.where();

    whereClause.eq("boardId", boardId);

    boolean filterByReporters = reporters != null && reporters.length > 0;
    if (onlyBugs || filterByReporters) {
      whereClause.and();
    }

    if (filterByReporters) {
      whereClause.in("reporterId", reporters);
    }
    if (onlyBugs) {
      whereClause.eq("issueTypeId", BUG_ISSUE_TYPE);
    }

    queryBuilder.orderBy("created", true);
    PreparedQuery<Issue> preparedQuery = queryBuilder.prepare();

    List<Issue> issuesFromDb = issueDao.query(preparedQuery);
    loadHistoryForIssueList(issuesFromDb);
    loadVersionsForIssueList();
  }

  private void loadVersionsForIssueList() throws SQLException {
    for (ExtendedIssue extendedIssue : this.issueList) {
      String issueId = extendedIssue.getIssue().getId();

      List<Version> versionList = new ArrayList<>();
      List<FixVersionPerIssue> fixVersions = fixVersionDao.queryForEq("issueId", issueId);
      for (FixVersionPerIssue fixVersion : fixVersions) {
        versionList.add(versionDao.queryForId(fixVersion.getVersion().getId()));
      }
      extendedIssue.getIssue().setFixVersions(versionList.toArray(new Version[versionList.size()]));

      versionList.clear();
      List<VersionPerIssue> affectedVersions = affectedVersionDao.queryForEq("issueId", issueId);
      for (VersionPerIssue affectedVersion : affectedVersions) {
        versionList.add(versionDao.queryForId(affectedVersion.getVersion().getId()));
      }

      extendedIssue.getIssue().setVersions(versionList.toArray(new Version[versionList.size()]));

      String projectId = extendedIssue.getIssue().getProject().getId();
      List<Version> projectVersions = versionDao.queryForEq("projectId", projectId);
      extendedIssue.setProjectVersions(projectVersions);
    }
  }

  private void loadHistoryForIssueList(List<Issue> issuesFromDb) throws SQLException {

    for (Issue issue : issuesFromDb) {
      issue.setChangeLog(new ResponseList<History>());

      List<History> historyList = historyDao.queryForEq("issueId", issue.getId());

      for (History history : historyList) {
        List<ChangeLogItem> changeList = changeLogItemDao.queryForEq("historyId", history.getId());
        history.setItems(changeList.toArray(new ChangeLogItem[changeList.size()]));
      }

      issue.getChangeLog().setValues(historyList.toArray(new History[historyList.size()]));
      this.issueList.add(new ExtendedIssue(issue));
    }
  }

  /**
   * Organizes a list of Issues in corresponding boards.
   * 
   * @return Map of per-board lists of issues.
   */
  public MultiValueMap<String, ExtendedIssue> organizeInBoards() {
    MultiValueMap<String, ExtendedIssue> issuesPerBoard = new MultiValueMap<>();
    for (ExtendedIssue extendedIssue : issueList) {
      String boardKey = extendedIssue.getIssue().getBoardId();
      if (boardKey.length() == 1) {
        boardKey = "0" + boardKey;
      }
      issuesPerBoard.put(BOARD_KEY_PREFFIX + boardKey, extendedIssue);
    }

    return issuesPerBoard;
  }

  /**
   * Given a list of issue, it organizes it in buckets based on the closest
   * release.
   * 
   * @return MultiValueMap, containing the buckets.
   */
  public MultiValueMap<Version, ExtendedIssue> organizeInReleases() {
    MultiValueMap<Version, ExtendedIssue> issuesPerRelease = new MultiValueMap<>();

    for (ExtendedIssue extendedIssue : issueList) {
      Version closestRelease = extendedIssue.getClosestRelease();
      issuesPerRelease.put(closestRelease, extendedIssue);
    }
    return issuesPerRelease;
  }

  /**
   * Returns the total list of reporters for a given board.
   * 
   * @param boardId
   *          Board identifier.
   * @return List of reporters.
   */
  public Set<User> getReporterCatalogPerBoard(String boardId) {
    Set<User> reporterCatalog = new HashSet<>();
    for (ExtendedIssue extendedIssue : issueList) {

      if (boardId.equals(extendedIssue.getIssue().getBoardId())) {
        reporterCatalog.add(extendedIssue.getIssue().getReporter());
      }
    }

    return reporterCatalog;
  }

  /**
   * Organizes a list of issues according to a time frame.
   * 
   * @return A MultiValueMap, where the frame identified is the key.
   */
  public MultiValueMap<String, ExtendedIssue> organizeInTimeFrames() {
    MultiValueMap<String, ExtendedIssue> issuesPerTimeFrame = new MultiValueMap<>();

    for (ExtendedIssue issue : issueList) {
      String timeFrameKey = getTimeFrameKey(issue);
      issuesPerTimeFrame.put(timeFrameKey, issue);
    }

    return issuesPerTimeFrame;
  }

  private String getTimeFrameKey(ExtendedIssue extendedIssue) {
    Date reportedDate = extendedIssue.getIssue().getCreated();
    Calendar reportedDateAsCalendar = Calendar.getInstance();
    reportedDateAsCalendar.setTime(reportedDate);

    String monthAsString = "" + (reportedDateAsCalendar.get(Calendar.MONTH) + 1);
    if ((reportedDateAsCalendar.get(Calendar.MONTH) + 1) < 10) {
      monthAsString = "0" + (reportedDateAsCalendar.get(Calendar.MONTH) + 1);
    }

    return reportedDateAsCalendar.get(Calendar.YEAR) + "-" + monthAsString;
  }

  public List<ExtendedIssue> getIssueList() {
    return issueList;
  }

}
