package crest.jira.data.miner.report.model;

import crest.jira.data.miner.csv.CsvExportSupport;
import crest.jira.data.miner.csv.JiraCsvConfiguration;
import crest.jira.data.retriever.model.ChangeLogItem;
import crest.jira.data.retriever.model.History;
import crest.jira.data.retriever.model.Issue;
import crest.jira.data.retriever.model.Priority;
import crest.jira.data.retriever.model.Version;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

public class ExtendedIssue implements CsvExportSupport {

  private static final String DATE_PATTERN = "yyyy-MM-dd";

  private static Logger logger = Logger.getLogger(ExtendedIssue.class.getName());

  public static final Priority NO_PRIORITY = new Priority("0", "No Priority");
  private static final Version NO_RELEASE = new Version("-1", "0.0.0", new Date(0L));
  private static final long MILISECONDS_IN_A_DATE = 24 * 60 * 60 * 1000;

  private Issue issue;
  private Priority originalPriority;
  private boolean doesPriorityChanged;
  private double resolutionTime = 0.0;
  private boolean isResolved = false;

  private Set<Version> projectVersions;
  private ExtendedUser reporterMetrics;

  /**
   * Calculates additional fields that are necessary for analysis.
   * 
   * @param issue
   *          Original issue, with all information loaded,
   */
  public ExtendedIssue(Issue issue) {
    this.issue = issue;
    this.reporterMetrics = new ExtendedUser(this.issue.getReporter());

    loadPriorityProperties();
    loadResolutionProperties();
  }

  /**
   * Identifies an issue as a possible priority inflation.
   * 
   * @return True if it is highly possible an inflation, false if it is not.
   */
  public boolean isProbablyAnInflation() {
    // TODO(cgavidia): This rule needs to be improved!
    int maximumReleasesForSevere = 0;
    return this.isReportedSevere() && (this.getReleasesToBeFixed() != null
        && this.getReleasesToBeFixed() > maximumReleasesForSevere);
  }

  /**
   * Indicate if this a severe issue.
   * 
   * @return True if it is severe, false if it is not.
   */
  public boolean isReportedSevere() {
    if ("1".equals(originalPriority.getId()) || "2".equals(originalPriority.getId())) {
      return true;
    }

    return false;
  }

  /**
   * Indicates if this issue has the Default priority assgined.
   * 
   * @return True if it is a default, false if it is not.
   */
  public boolean isReportedDefault() {
    if ("3".equals(originalPriority.getId())) {
      return true;
    }

    return false;
  }

  /**
   * Indicate if this a non-severe issue.
   * 
   * @return True if it is non-severe, false if it is not.
   */
  public boolean isReportedNonSevere() {
    if ("4".equals(originalPriority.getId()) || "5".equals(originalPriority.getId())) {
      return true;
    }

    return false;
  }

  private void loadResolutionProperties() {
    Date reportedDate = this.issue.getCreated();
    Date resolutionDate = this.issue.getResolutiondate();

    if (reportedDate != null && resolutionDate != null) {
      this.resolutionTime = Math.abs(resolutionDate.getTime() - reportedDate.getTime())
          / new Double(MILISECONDS_IN_A_DATE);
      this.isResolved = true;
    }

  }

  private void loadPriorityProperties() {
    List<TimedChangeLogItem> priorityChanges = new ArrayList<TimedChangeLogItem>();
    Priority noPriority = NO_PRIORITY;

    this.doesPriorityChanged = false;
    this.originalPriority = noPriority;

    if (issue.getPriority() != null && issue.getPriority().getId() != null) {
      this.originalPriority = issue.getPriority();
    }

    for (History history : issue.getChangeLog().getValues()) {
      for (ChangeLogItem changeItem : history.getItems()) {
        if ("priority".equals(changeItem.getField())) {
          priorityChanges.add(new TimedChangeLogItem(changeItem, history.getCreated()));
        }
      }
    }

    if (priorityChanges.size() > 0) {

      doesPriorityChanged = true;

      Collections.sort(priorityChanges, new Comparator<TimedChangeLogItem>() {

        public int compare(TimedChangeLogItem thisObject, TimedChangeLogItem otherObject) {
          return thisObject.getChangeLogDate().compareTo(otherObject.getChangeLogDate());
        }
      });

      TimedChangeLogItem firstChange = priorityChanges.get(0);

      String fromPriority = firstChange.getChangeLogItem().getFrom();

      if (fromPriority != null) {
        this.originalPriority = new Priority();
        this.originalPriority.setId(fromPriority);
        this.originalPriority.setName(firstChange.getChangeLogItem().getFromString());
      }
    }
  }

  public boolean isResolved() {
    return isResolved;
  }

  public Double getResolutionTime() {
    return resolutionTime;
  }

  public Priority getOriginalPriority() {
    return originalPriority;
  }

  public boolean isDoesPriorityChanged() {
    return doesPriorityChanged;
  }

  public Issue getIssue() {
    return issue;
  }

  /**
   * Returns the version that's the closest to the report of the Issue.
   * 
   * @return Closest version.
   */
  public Version getClosestRelease() {

    for (Version version : projectVersions) {
      Date releaseDate = version.getReleaseDate();
      if (releaseDate != null && releaseDate.compareTo(this.getIssue().getCreated()) > 0) {
        return version;
      }
    }

    return NO_RELEASE;
  }

  private Version getLatestFixVersion() {
    Version latestFixVersion = NO_RELEASE;

    Version[] fixVersionsArray = issue.getFixVersions();
    if (fixVersionsArray.length > 0) {
      List<Version> fixVersions = new ArrayList<Version>(Arrays.asList(fixVersionsArray));
      Collections.sort(fixVersions, new ReleaseDateComparator());

      latestFixVersion = fixVersions.get(fixVersions.size() - 1);
    }

    return latestFixVersion;
  }

  private Integer getVersionIndex(Version version) {
    int index = 0;

    for (Version currentVersion : this.projectVersions) {
      if (currentVersion.equals(version)) {
        return index;
      }

      index += 1;
    }

    return null;
  }

  /**
   * Returns the number of releases needed to get a fix for this issue.
   * 
   * @return Number of releases, and -1 if no fix version was included.
   */
  public Integer getReleasesToBeFixed() {

    Version closestRelease = this.getClosestRelease();
    Version latestFixVersion = this.getLatestFixVersion();

    if (!NO_RELEASE.equals(closestRelease) && !NO_RELEASE.equals(latestFixVersion)) {
      int closestReleaseIndex = getVersionIndex(closestRelease);
      int fixVersionIndex = getVersionIndex(latestFixVersion);

      if (fixVersionIndex < closestReleaseIndex) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        Date latestReleaseDate = latestFixVersion.getReleaseDate();
        String latestReleaseAsString = latestReleaseDate != null
            ? dateFormat.format(latestReleaseDate) : "NO DATE";

        logger.fine("On issue " + this.getIssue().getKey() + " (Reported "
            + dateFormat.format(this.getIssue().getCreated()) + ") was fixed on Release "
            + latestFixVersion.getName() + " (Index " + fixVersionIndex + " "
            + latestReleaseAsString + " ). However, the closest release was "
            + closestRelease.getName() + " (Index " + closestReleaseIndex + " "
            + dateFormat.format(closestRelease.getReleaseDate()) + " )");
      }

      return fixVersionIndex - closestReleaseIndex;
    }

    return null;
  }

  /**
   * Stores the list of versions for the Project related to this issue, and
   * sorts it.
   * 
   * @param projectVersions
   *          List of project versions.
   */
  public void setProjectVersions(List<Version> projectVersions) {

    // It is possible that the Versions associated to the issue are not
    // retrieved from the Database query.
    Set<Version> joinedSet = new TreeSet<>(new ReleaseDateComparator());
    joinedSet.addAll(projectVersions);
    joinedSet.addAll(Arrays.asList(issue.getVersions()));
    joinedSet.addAll(Arrays.asList(issue.getFixVersions()));

    this.projectVersions = joinedSet;
  }

  public ExtendedUser getReporterMetrics() {
    return reporterMetrics;
  }

  @Override
  public String[] getCsvHeader() {
    List<String> headerAsList = new ArrayList<String>();

    headerAsList.add(JiraCsvConfiguration.ISSUE_KEY);
    headerAsList.add(JiraCsvConfiguration.ISSUE_TYPE);
    headerAsList.add(JiraCsvConfiguration.ORIGINAL_PRIORITY);
    headerAsList.add(JiraCsvConfiguration.CURRENT_PRIORITY);
    headerAsList.add(JiraCsvConfiguration.CREATION_DATE);
    headerAsList.add(JiraCsvConfiguration.REPORTER);
    headerAsList.add(JiraCsvConfiguration.CLOSEST_RELEASE_NAME);
    headerAsList.add(JiraCsvConfiguration.CLOSEST_RELEASE_INDEX);
    headerAsList.add(JiraCsvConfiguration.CLOSEST_RELEASE_DATE);
    headerAsList.add(JiraCsvConfiguration.LATEST_FIX_NAME);
    headerAsList.add(JiraCsvConfiguration.LATEST_RELEASE_INDEX);
    headerAsList.add(JiraCsvConfiguration.LATEST_FIX_DATE);
    headerAsList.add(JiraCsvConfiguration.RELEASES_TO_FIX_SUFFIX);

    return headerAsList.toArray(new String[headerAsList.size()]);
  }

  @Override
  public List<Object> getCsvRecord() {
    List<Object> recordAsList = new ArrayList<>();

    recordAsList.add(this.issue.getKey());
    recordAsList.add(this.issue.getIssueType().getId());
    recordAsList.add(this.getOriginalPriority().getId());

    Priority currentPriority = (Priority) ObjectUtils.defaultIfNull(this.issue.getPriority(),
        NO_PRIORITY);
    recordAsList.add(currentPriority.getId());
    recordAsList.add(this.issue.getCreated());
    recordAsList.add(this.issue.getReporter().getName());

    Version closestRelease = this.getClosestRelease();
    recordAsList.add(closestRelease.getName());
    recordAsList.add(getVersionIndex(closestRelease));
    recordAsList.add(closestRelease.getReleaseDate());

    Version latestFixVersion = (Version) ObjectUtils.defaultIfNull(this.getLatestFixVersion(),
        NO_RELEASE);
    recordAsList.add(latestFixVersion.getName());
    recordAsList.add(getVersionIndex(latestFixVersion));
    recordAsList.add(latestFixVersion.getReleaseDate());

    recordAsList.add(this.getReleasesToBeFixed());
    return recordAsList;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("issueId", issue.getId()).append("key", issue.getKey())
        .append("boardId", issue.getBoardId()).append("priority", issue.getPriority()).toString();
  }

}
