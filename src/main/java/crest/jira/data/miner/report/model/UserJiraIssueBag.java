package crest.jira.data.miner.report.model;

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
    List<String> headerAsString = new ArrayList<String>();
    headerAsString.add(CsvConfiguration.USER_IDENTIFIER);

    for (T groupIdentifier : groupKeys) {
      headerAsString.add(groupIdentifier + " " + CsvConfiguration.TOTAL_IDENTIFIER);
      headerAsString.add(groupIdentifier + " " + CsvConfiguration.NON_SEVERE_IDENTIFIER);
      headerAsString.add(groupIdentifier + " " + CsvConfiguration.SEVERE_IDENTIFIER);
      headerAsString.add(groupIdentifier + " " + CsvConfiguration.PRIORITY_DESCRIPTIONS[3]);
    }

    headerAsString.add(CsvConfiguration.ABSTENTIONS_IDENTIFIER);
    headerAsString.add(CsvConfiguration.PARTICIPATIONS_IDENTIFIER);

    return headerAsString.toArray(new String[headerAsString.size()]);
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
