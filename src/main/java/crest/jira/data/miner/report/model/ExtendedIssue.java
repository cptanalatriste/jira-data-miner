package crest.jira.data.miner.report.model;

import crest.jira.data.retriever.model.ChangeLogItem;
import crest.jira.data.retriever.model.History;
import crest.jira.data.retriever.model.Issue;
import crest.jira.data.retriever.model.Priority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ExtendedIssue {

  private static final long MILISECONDS_IN_A_DATE = 24 * 60 * 60 * 1000;

  private Issue issue;
  private Priority originalPriority;
  private boolean doesPriorityChanged;
  private double resolutionTime = 0.0;

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

  private void loadResolutionProperties() {
    Date reportedDate = this.issue.getCreated();
    Date resolutionDate = this.issue.getResolutiondate();

    if (reportedDate != null && resolutionDate != null) {
      this.resolutionTime = Math.abs(resolutionDate.getTime() - reportedDate.getTime())
          / MILISECONDS_IN_A_DATE;
    }

  }

  private void loadPriorityProperties() {
    List<TimedChangeLogItem> priorityChanges = new ArrayList<TimedChangeLogItem>();

    this.doesPriorityChanged = false;
    this.originalPriority = issue.getPriority();

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

      this.originalPriority = new Priority();
      this.originalPriority.setId(firstChange.getChangeLogItem().getFrom());
      this.originalPriority.setName(firstChange.getChangeLogItem().getFromString());
    }

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

}
