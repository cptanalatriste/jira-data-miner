package crest.jira.data.miner.report.model;

import crest.jira.data.retriever.model.ChangeLogItem;

import java.util.Date;

public class TimedChangeLogItem {

  private ChangeLogItem changeLogItem;
  private Date changeLogDate;

  /**
   * Creates a Change Items with a Date.
   * 
   * @param changeLogItem
   *          Change Item.
   * @param changeLogDate
   *          Date of change.
   */
  public TimedChangeLogItem(ChangeLogItem changeLogItem, Date changeLogDate) {
    super();
    this.changeLogItem = changeLogItem;
    this.changeLogDate = changeLogDate;
  }

  public ChangeLogItem getChangeLogItem() {
    return changeLogItem;
  }

  public void setChangeLogItem(ChangeLogItem changeLogItem) {
    this.changeLogItem = changeLogItem;
  }

  public Date getChangeLogDate() {
    return changeLogDate;
  }

  public void setChangeLogDate(Date changeLogDate) {
    this.changeLogDate = changeLogDate;
  }

}
