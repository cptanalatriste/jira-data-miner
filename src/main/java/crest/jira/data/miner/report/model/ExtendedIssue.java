package crest.jira.data.miner.report.model;

import crest.jira.data.retriever.model.ChangeLogItem;
import crest.jira.data.retriever.model.History;
import crest.jira.data.retriever.model.Issue;
import crest.jira.data.retriever.model.Priority;
import crest.jira.data.retriever.model.Version;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ExtendedIssue {

  public static final String NO_PRIORITY_ID = "0";
  public static final Version NO_RELEASE = new Version("NO RELEASE", new Date(0L));
  private static final long MILISECONDS_IN_A_DATE = 24 * 60 * 60 * 1000;

  private Issue issue;
  private Priority originalPriority;
  private boolean doesPriorityChanged;
  private double resolutionTime = 0.0;
  private boolean isResolved = false;

  private List<Version> projectVersions;

  /**
   * Calculates additional fields that are necessary for analysis.
   * 
   * @param issue
   *          Original issue, with all information loaded,
   */
  public ExtendedIssue(Issue issue) {
    this.issue = issue;

    loadPriorityProperties();
    loadResolutionProperties();
  }

  /**
   * Indicate if this a severe issue.
   * 
   * @return True if it is severe, false if it is not.
   */
  public boolean isSevere() {
    if ("1".equals(originalPriority.getId()) || "2".equals(originalPriority.getId())) {
      return true;
    }

    return false;
  }

  /**
   * Indicate if this a non-severe issue.
   * 
   * @return True if it is non-severe, false if it is not.
   */
  public boolean isNonSevere() {
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
    Priority noPriority = new Priority();
    noPriority.setId(NO_PRIORITY_ID);

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

  /**
   * Stores the list of versions for the Project related to this issue, and
   * sorts it.
   * 
   * @param projectVersions
   *          List of project versions.
   */
  public void setProjectVersions(List<Version> projectVersions) {
    this.projectVersions = projectVersions;

    Collections.sort(this.projectVersions, new VersionComparator());
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("issueId", issue.getId())
        .append("boardId", issue.getBoardId()).append("priority", issue.getPriority()).toString();
  }

}
