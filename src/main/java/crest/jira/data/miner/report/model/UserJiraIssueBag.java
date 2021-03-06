package crest.jira.data.miner.report.model;

import crest.jira.data.miner.csv.CsvExportSupport;
import crest.jira.data.miner.csv.JiraCsvConfiguration;
import crest.jira.data.retriever.model.User;

import org.apache.commons.math3.stat.Frequency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserJiraIssueBag<T> implements CsvExportSupport {

  private User user;
  private List<T> groupKeys;
  private Map<T, JiraIssueBag<T>> periodIssueBags;
  private Frequency issuesPerPeriodFrequency = new Frequency();

  public UserJiraIssueBag() {
  }

  /**
   * Creates an Issue Bag related to a User.
   * 
   * @param user
   *          The owner of the bag.
   */
  public UserJiraIssueBag(User user) {
    this.user = user;
    this.periodIssueBags = new HashMap<>();
    this.groupKeys = new ArrayList<>();
  }

  public void addBag(JiraIssueBag<T> issueBag) {
    this.groupKeys.add(issueBag.getIdentifier());
    this.periodIssueBags.put(issueBag.getIdentifier(), issueBag.getIssuesPerReporter(this.user));
  }

  @Override
  public String[] getCsvHeader() {
    List<String> headerAsList = new ArrayList<String>();
    headerAsList.add(JiraCsvConfiguration.USER_IDENTIFIER);

    for (T groupIdentifier : groupKeys) {
      headerAsList.add(groupIdentifier + " " + JiraCsvConfiguration.TOTAL_IDENTIFIER);
      headerAsList.add(groupIdentifier + " " + JiraCsvConfiguration.NON_SEVERE_IDENTIFIER);
      headerAsList.add(groupIdentifier + " " + JiraCsvConfiguration.SEVERE_IDENTIFIER);
      headerAsList.add(groupIdentifier + " " + JiraCsvConfiguration.PRIORITY_DESCRIPTIONS[3]);
    }

    headerAsList.add(JiraCsvConfiguration.ABSTENTIONS_IDENTIFIER);
    headerAsList.add(JiraCsvConfiguration.PARTICIPATIONS_IDENTIFIER);

    return headerAsList.toArray(new String[headerAsList.size()]);
  }

  @Override
  public List<Object> getCsvRecord() {
    List<Object> metrics = new ArrayList<>();
    metrics.add(this.user.getName());

    for (T groupIdentifier : groupKeys) {
      JiraIssueBag<T> periodBag = this.periodIssueBags.get(groupIdentifier);

      int issuesPerPeriod = periodBag.getNumberOfIssues();
      issuesPerPeriodFrequency.addValue(issuesPerPeriod);

      metrics.add(issuesPerPeriod);
      metrics.add(periodBag.getNonSevereRelativeFrequency());
      metrics.add(periodBag.getSevereRelativeFrequency());
      metrics.add(periodBag.getRelativeFrequencyByPriority("3"));
    }

    long abstentions = issuesPerPeriodFrequency.getCount(0);
    long participations = issuesPerPeriodFrequency.getSumFreq() - abstentions;
    metrics.add(abstentions);
    metrics.add(participations);

    return metrics;
  }

}
