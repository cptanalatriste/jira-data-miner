package crest.jira.data.miner.report.model;

import crest.jira.data.retriever.model.User;

import org.apache.commons.math3.stat.Frequency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserJiraIssueBag implements CsvExportSupport {

  private User user;
  private List<String> periodKeys;
  private Map<String, JiraIssueBag> periodIssueBags;
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
    this.periodKeys = new ArrayList<>();
  }

  public void addBag(JiraIssueBag issueBag) {
    this.periodKeys.add(issueBag.getIdentifier());
    this.periodIssueBags.put(issueBag.getIdentifier(), issueBag.getIssuesPerReporter(this.user));
  }

  @Override
  public String[] getCsvHeader() {
    List<String> headerAsString = new ArrayList<String>();
    headerAsString.add(CsvConfiguration.USER_IDENTIFIER);

    for (String periodIdentifier : periodKeys) {
      headerAsString.add(CsvConfiguration.TOTAL_IDENTIFIER + " " + periodIdentifier);
    }

    headerAsString.add(CsvConfiguration.ABSTENTIONS_IDENTIFIER);
    headerAsString.add(CsvConfiguration.PARTICIPATIONS_IDENTIFIER);

    return headerAsString.toArray(new String[headerAsString.size()]);
  }

  @Override
  public List<Object> getCsvRecord() {
    List<Object> metrics = new ArrayList<>();
    metrics.add(this.user.getName());

    for (String periodIdentifier : periodKeys) {
      int issuesPerPeriod = this.periodIssueBags.get(periodIdentifier).getNumberOfIssues();
      issuesPerPeriodFrequency.addValue(issuesPerPeriod);

      metrics.add(issuesPerPeriod);
    }

    long abstentions = issuesPerPeriodFrequency.getCount(0);
    long participations = issuesPerPeriodFrequency.getSumFreq() - abstentions;
    metrics.add(abstentions);
    metrics.add(participations);

    return metrics;
  }

}
