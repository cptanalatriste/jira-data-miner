package crest.jira.data.miner;

import com.j256.ormlite.support.ConnectionSource;

import crest.jira.data.miner.config.ConfigurationProvider;
import crest.jira.data.retriever.BoardRetriever;
import crest.jira.data.retriever.EpicRetriever;
import crest.jira.data.retriever.FieldRetriever;
import crest.jira.data.retriever.JiraApiConfiguration;
import crest.jira.data.retriever.SprintRetriever;
import crest.jira.data.retriever.db.JiraEntityMiner;
import crest.jira.data.retriever.map.IssueWithCustomFields;
import crest.jira.data.retriever.map.ResponseList;
import crest.jira.data.retriever.model.Board;
import crest.jira.data.retriever.model.ChangeLogItem;
import crest.jira.data.retriever.model.ClosedSprintPerIssue;
import crest.jira.data.retriever.model.Comment;
import crest.jira.data.retriever.model.Component;
import crest.jira.data.retriever.model.ComponentPerIssue;
import crest.jira.data.retriever.model.CustomFieldsCatalog;
import crest.jira.data.retriever.model.Epic;
import crest.jira.data.retriever.model.Field;
import crest.jira.data.retriever.model.FixVersionPerIssue;
import crest.jira.data.retriever.model.History;
import crest.jira.data.retriever.model.Issue;
import crest.jira.data.retriever.model.IssueType;
import crest.jira.data.retriever.model.Priority;
import crest.jira.data.retriever.model.Project;
import crest.jira.data.retriever.model.ProjectCategory;
import crest.jira.data.retriever.model.Resolution;
import crest.jira.data.retriever.model.Sprint;
import crest.jira.data.retriever.model.Status;
import crest.jira.data.retriever.model.StatusCategory;
import crest.jira.data.retriever.model.SubtaskPerIssue;
import crest.jira.data.retriever.model.User;
import crest.jira.data.retriever.model.Version;
import crest.jira.data.retriever.model.VersionPerIssue;

import org.glassfish.jersey.jackson.JacksonFeature;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class JiraDataExtractor {

  private static Logger logger = Logger.getLogger(JiraDataExtractor.class.getName());

  private static final String CHANGELOG_EXPAND = "changelog";
  private static final Integer MAX_ISSUES = null;

  private static ConfigurationProvider configurationProvider = new ConfigurationProvider();

  private static JiraEntityMiner<Board, String> boardMiner;
  private static JiraEntityMiner<Epic, String> epicMiner;
  private static JiraEntityMiner<Sprint, String> sprintMiner;
  private static JiraEntityMiner<Issue, String> issueMiner;
  private static JiraEntityMiner<CustomFieldsCatalog, String> customCatalogMiner;
  private static JiraEntityMiner<Resolution, String> resolutionMiner;
  private static JiraEntityMiner<Priority, String> priorityMiner;
  private static JiraEntityMiner<IssueType, String> issueTypeMiner;
  private static JiraEntityMiner<User, String> userMiner;
  private static JiraEntityMiner<Comment, String> commentMiner;
  private static JiraEntityMiner<History, String> historyMiner;
  private static JiraEntityMiner<ChangeLogItem, String> changeItemMiner;

  private static JiraEntityMiner<Project, String> projectMiner;
  private static JiraEntityMiner<ProjectCategory, String> projectCategoryMiner;
  private static JiraEntityMiner<Status, String> statusMiner;
  private static JiraEntityMiner<StatusCategory, String> statusCategoryMiner;

  private static JiraEntityMiner<Component, String> componentMiner;
  private static JiraEntityMiner<ComponentPerIssue, String> componentPerIssuetMiner;
  private static JiraEntityMiner<SubtaskPerIssue, String> subtaskPerIssuetMiner;
  private static JiraEntityMiner<Version, String> versionMiner;
  private static JiraEntityMiner<FixVersionPerIssue, String> fixVersionPerIssueMiner;
  private static JiraEntityMiner<VersionPerIssue, String> versionPerIssueMiner;
  private static JiraEntityMiner<ClosedSprintPerIssue, String> closedSprintPerIssueMiner;

  private static JiraApiConfiguration jiraConfiguration;
  private static Client restClient;
  private static Field[] fields;

  /**
   * Retrieves the information from all boards from a Jira Repository.
   * 
   * @param args
   *          Not used.
   * @throws Exception
   *           Many things can go wrong ...
   */
  public static void main(String... args) throws Exception {
    ConnectionSource connectionSource = configurationProvider.getConnectionSource();

    setupHttpConnection();
    setupDatabase(connectionSource);
    readAndWriteBoardData();
  }

  private static void setupHttpConnection() {
    jiraConfiguration = configurationProvider.getJiraApiConfiguration();

    restClient = ClientBuilder.newClient();
    restClient.register(JacksonFeature.class);

    FieldRetriever fieldRetriever = new FieldRetriever(restClient, jiraConfiguration);
    fields = fieldRetriever.getFields();
  }

  private static void setupDatabase(ConnectionSource connectionSource) throws SQLException {
    boardMiner = new JiraEntityMiner<Board, String>(Board.class, connectionSource);
    epicMiner = new JiraEntityMiner<Epic, String>(Epic.class, connectionSource);
    sprintMiner = new JiraEntityMiner<Sprint, String>(Sprint.class, connectionSource);
    issueMiner = new JiraEntityMiner<Issue, String>(Issue.class, connectionSource);
    customCatalogMiner = new JiraEntityMiner<CustomFieldsCatalog, String>(CustomFieldsCatalog.class,
        connectionSource);
    resolutionMiner = new JiraEntityMiner<Resolution, String>(Resolution.class, connectionSource);
    priorityMiner = new JiraEntityMiner<Priority, String>(Priority.class, connectionSource);
    issueTypeMiner = new JiraEntityMiner<IssueType, String>(IssueType.class, connectionSource);
    userMiner = new JiraEntityMiner<User, String>(User.class, connectionSource);
    commentMiner = new JiraEntityMiner<Comment, String>(Comment.class, connectionSource);

    historyMiner = new JiraEntityMiner<History, String>(History.class, connectionSource);
    changeItemMiner = new JiraEntityMiner<ChangeLogItem, String>(ChangeLogItem.class,
        connectionSource);

    projectMiner = new JiraEntityMiner<Project, String>(Project.class, connectionSource);
    projectCategoryMiner = new JiraEntityMiner<ProjectCategory, String>(ProjectCategory.class,
        connectionSource);
    statusMiner = new JiraEntityMiner<Status, String>(Status.class, connectionSource);
    statusCategoryMiner = new JiraEntityMiner<StatusCategory, String>(StatusCategory.class,
        connectionSource);

    componentMiner = new JiraEntityMiner<Component, String>(Component.class, connectionSource);
    componentPerIssuetMiner = new JiraEntityMiner<ComponentPerIssue, String>(
        ComponentPerIssue.class, connectionSource);
    subtaskPerIssuetMiner = new JiraEntityMiner<SubtaskPerIssue, String>(SubtaskPerIssue.class,
        connectionSource);
    versionMiner = new JiraEntityMiner<Version, String>(Version.class, connectionSource);
    fixVersionPerIssueMiner = new JiraEntityMiner<FixVersionPerIssue, String>(
        FixVersionPerIssue.class, connectionSource);
    versionPerIssueMiner = new JiraEntityMiner<VersionPerIssue, String>(VersionPerIssue.class,
        connectionSource);
    closedSprintPerIssueMiner = new JiraEntityMiner<ClosedSprintPerIssue, String>(
        ClosedSprintPerIssue.class, connectionSource);
  }

  private static void readAndWriteBoardData() throws Exception {

    BoardRetriever boardRetriever = new BoardRetriever(restClient, jiraConfiguration, fields);
    ResponseList<Board> allBoards = boardRetriever.getAllBoards();
    boardMiner.writeToDatabase(allBoards);

    int issuesWritten = 0;

    Comparator<Board> comparator = new Comparator<Board>() {

      public int compare(Board oneBoard, Board anotherBoard) {
        return oneBoard.getId().compareTo(anotherBoard.getId());
      }
    };
    Arrays.sort(allBoards.getValues(), comparator);

    for (Board board : allBoards.getValues()) {
      // TODO(cgavidia): This is temporary. Remove later.
      if ("45".compareTo(board.getId()) > 0) {
        continue;
      }

      readAndWriteEpicData(board.getId());
      clearBeforeLoading(board.getId());
      ResponseList<Sprint> sprints = readAndWriteSprintData(board.getId());
      issuesWritten += readAndWriteIssueData(board.getId(),
          sprints != null ? sprints.getValues() : null);

      if (MAX_ISSUES != null && issuesWritten >= MAX_ISSUES) {
        logger.info(issuesWritten + " were written on the Database.");
        return;
      }
    }
  }

  private static int readAndWriteIssueData(String boardId, Sprint[] sprints) throws Exception {
    BoardRetriever boardRetriever = new BoardRetriever(restClient, jiraConfiguration, fields);

    int issueCounter = 0;
    List<IssueWithCustomFields> issuesPerBoard = boardRetriever.getIssuesForBoard(boardId,
        CHANGELOG_EXPAND);

    issueCounter += processIssueList(boardId, issuesPerBoard);
    return issueCounter;
  }

  private static void clearBeforeLoading(String boardId) throws SQLException {
    changeItemMiner.deleteAccordingValue("boardId", boardId);
    componentPerIssuetMiner.deleteAccordingValue("boardId", boardId);
    subtaskPerIssuetMiner.deleteAccordingValue("boardId", boardId);
    fixVersionPerIssueMiner.deleteAccordingValue("boardId", boardId);
    versionPerIssueMiner.deleteAccordingValue("boardId", boardId);
    closedSprintPerIssueMiner.deleteAccordingValue("boardId", boardId);
  }

  private static void readAndWriteEpicData(String boardId) throws Exception {
    EpicRetriever epicRetriever = new EpicRetriever(restClient, jiraConfiguration, fields);

    List<Epic> epics = epicRetriever.getEpics(boardId);
    for (Epic epic : epics) {
      epic.setBoardId(boardId);
    }

    epicMiner.writeToDatabase(epics);
  }

  private static ResponseList<Sprint> readAndWriteSprintData(String boardId) throws Exception {
    SprintRetriever sprintRetriever = new SprintRetriever(restClient, jiraConfiguration, fields);

    ResponseList<Sprint> sprints = sprintRetriever.getAllSprints(boardId);

    // TODO(cgavidia): BIG TODO!! Many boards doesn't support sprints! We need
    // to retrieves Issues directly from the Board.
    if (sprints != null) {
      sprintMiner.writeToDatabase(sprints);
    }

    return sprints;
  }

  private static int processIssueList(String boardId, List<IssueWithCustomFields> issueList)
      throws Exception {
    ArrayList<Issue> issues = new ArrayList<Issue>();
    ArrayList<CustomFieldsCatalog> customFields = new ArrayList<CustomFieldsCatalog>();
    ArrayList<Resolution> resolutions = new ArrayList<Resolution>();
    ArrayList<Priority> priorities = new ArrayList<Priority>();
    ArrayList<IssueType> issueTypes = new ArrayList<IssueType>();
    ArrayList<User> users = new ArrayList<User>();
    ArrayList<ChangeLogItem> changeLogItems = new ArrayList<ChangeLogItem>();
    ArrayList<Comment> comments = new ArrayList<Comment>();
    ArrayList<History> histories = new ArrayList<History>();
    ArrayList<Project> projects = new ArrayList<Project>();
    ArrayList<ProjectCategory> projectCategories = new ArrayList<ProjectCategory>();
    ArrayList<Status> status = new ArrayList<Status>();
    ArrayList<StatusCategory> statusCategory = new ArrayList<StatusCategory>();
    ArrayList<Component> components = new ArrayList<Component>();
    ArrayList<ComponentPerIssue> componentsPerIssue = new ArrayList<ComponentPerIssue>();
    ArrayList<SubtaskPerIssue> subtasksPerIssue = new ArrayList<SubtaskPerIssue>();
    ArrayList<Version> versions = new ArrayList<Version>();
    ArrayList<FixVersionPerIssue> fixVersionsPerIssue = new ArrayList<FixVersionPerIssue>();
    ArrayList<VersionPerIssue> versionsPerIssue = new ArrayList<VersionPerIssue>();
    ArrayList<ClosedSprintPerIssue> closedSprintIssue = new ArrayList<ClosedSprintPerIssue>();

    for (IssueWithCustomFields issueWithCustomFields : issueList) {

      Issue issue = issueWithCustomFields.getIssue();
      // TODO(cgavidia): This has to be moved
      issue.setBoardId(boardId);

      issues.add(issue);
      customFields.add(issueWithCustomFields.getCustomFields());

      // Epics are not added because they were recorded previously.
      resolutions.add(issue.getResolution());
      priorities.add(issue.getPriority());
      issueTypes.add(issue.getIssueType());
      users.add(issue.getAssignee());
      users.add(issue.getCreator());
      users.add(issue.getReporter());
      projects.add(issue.getProject());
      projectCategories.add(issue.getProject().getProjectCategory());
      status.add(issue.getStatus());
      statusCategory.add(issue.getStatus().getStatusCategory());

      if (issue.getComponents() != null) {
        components.addAll(Arrays.asList(issue.getComponents()));
      }

      componentsPerIssue.addAll(issue.getComponentsPerIssue());
      subtasksPerIssue.addAll(issue.getSubstasksPerIssue());

      if (issue.getFixVersions() != null) {
        versions.addAll(Arrays.asList(issue.getFixVersions()));
      }

      if (issue.getVersions() != null) {
        versions.addAll(Arrays.asList(issue.getVersions()));
      }

      fixVersionsPerIssue.addAll(issue.getFixVersionsPerIssue());
      versionsPerIssue.addAll(issue.getVersionsPerIssue());
      closedSprintIssue.addAll(issue.getClosedSprintsPerIssue());

      commentMiner.enforcePaginationSupport(issue.getComment());
      historyMiner.enforcePaginationSupport(issue.getChangeLog());

      if (issue.getComment() != null && issue.getComment().getValues() != null) {
        comments.addAll(Arrays.asList(issue.getComment().getValues()));
      }

      if (issue.getChangeLog() != null && issue.getChangeLog().getValues() != null) {
        histories.addAll(Arrays.asList(issue.getChangeLog().getValues()));
      }

      for (History changeLogItem : issue.getChangeLog().getValues()) {
        if (changeLogItem.getItems() != null) {
          changeLogItems.addAll(Arrays.asList(changeLogItem.getItems()));
        }
      }
    }

    componentMiner.writeToDatabase(components);
    componentPerIssuetMiner.writeToDatabase(componentsPerIssue);
    versionMiner.writeToDatabase(versions);

    statusCategoryMiner.writeToDatabase(statusCategory);
    statusMiner.writeToDatabase(status);

    projectCategoryMiner.writeToDatabase(projectCategories);
    projectMiner.writeToDatabase(projects);
    commentMiner.writeToDatabase(comments);
    historyMiner.writeToDatabase(histories);

    userMiner.writeToDatabase(users);
    priorityMiner.writeToDatabase(priorities);
    changeItemMiner.writeToDatabase(changeLogItems);
    issueTypeMiner.writeToDatabase(issueTypes);
    subtaskPerIssuetMiner.writeToDatabase(subtasksPerIssue);
    fixVersionPerIssueMiner.writeToDatabase(fixVersionsPerIssue);
    versionPerIssueMiner.writeToDatabase(versionsPerIssue);
    closedSprintPerIssueMiner.writeToDatabase(closedSprintIssue);

    resolutionMiner.writeToDatabase(resolutions);

    // CreateOrUpdateStatus createOrUpdateStatus =
    // issueMiner.writeToDatabase(issues);
    issueMiner.writeToDatabase(issues);
    customCatalogMiner.writeToDatabase(customFields);

    // if (createOrUpdateStatus != null) {
    // issueCounter += createOrUpdateStatus.getNumLinesChanged();
    // }

    return issues.size();
  }

}
