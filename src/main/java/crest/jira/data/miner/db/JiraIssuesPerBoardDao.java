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
import crest.jira.data.retriever.model.History;
import crest.jira.data.retriever.model.Issue;

import org.apache.commons.collections4.map.MultiValueMap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class JiraIssuesPerBoardDao {

  private String boardId;
  private Dao<Issue, String> issueDao;
  private Dao<History, String> historyDao;
  private Dao<ChangeLogItem, String> changeLogItemDao;
  private List<ExtendedIssue> issueList;

  /**
   * Manages the database access for Issue Analysis.
   * 
   * @param boardId
   *          Board identifier.
   * @param connectionSource
   *          Connection source.
   * @throws SQLException
   *           In case of SQL errors.
   */
  public JiraIssuesPerBoardDao(String boardId, ConnectionSource connectionSource)
      throws SQLException {
    this.boardId = boardId;
    this.issueDao = DaoManager.createDao(connectionSource, Issue.class);
    this.historyDao = DaoManager.createDao(connectionSource, History.class);
    this.changeLogItemDao = DaoManager.createDao(connectionSource, ChangeLogItem.class);
  }

  /**
   * Loads the Issues that will be considered in analysis.
   * 
   * @throws SQLException
   *           In case of problems.
   */
  public void loadIssues(Object... reporters) throws SQLException {
    QueryBuilder<Issue, String> queryBuilder = issueDao.queryBuilder();
    Where<Issue, String> whereClause = queryBuilder.where();

    whereClause.eq("boardId", this.boardId);
    if (reporters != null && reporters.length > 0) {
      whereClause.and();
      whereClause.in("reporterId", reporters);
    }
    queryBuilder.orderBy("created", true);

    PreparedQuery<Issue> preparedQuery = queryBuilder.prepare();

    List<Issue> issuesFromDb = issueDao.query(preparedQuery);
    loadHistoryForIssueList(issuesFromDb);

    this.issueList = new ArrayList<ExtendedIssue>();

    for (Issue issue : issuesFromDb) {
      this.issueList.add(new ExtendedIssue(issue));
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
    }

  }

  /**
   * Organizes a list of issues according to a time frame.
   * 
   * @return A MultiValueMap, where the frame identified is the key.
   */
  public MultiValueMap<String, ExtendedIssue> organizeTimeFrames() {
    MultiValueMap<String, ExtendedIssue> issuesPerTimeFrame = 
        new MultiValueMap<String, ExtendedIssue>();

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
}
