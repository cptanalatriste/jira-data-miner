package crest.jira.data.miner.report.model;

import java.util.HashMap;
import java.util.List;

public class IssueListMetrics {

  private HashMap<String, Integer> priorityCounter = new HashMap<String, Integer>();
  private HashMap<String, Integer> reporterCounter = new HashMap<String, Integer>();
  private HashMap<String, Integer> changerCounter = new HashMap<String, Integer>();

  private int priorityChanges = 0;
  private List<ExtendedIssue> originalIssues;
  private String identifier;

  /**
   * Calculates metrics for a specific list of Issues.
   * 
   * @param originalIssues
   *          List of issues.
   */
  public IssueListMetrics(String identifier, List<ExtendedIssue> originalIssues) {
    this.identifier = identifier;
    this.originalIssues = originalIssues;

    priorityCounter.put("0", 0);
    priorityCounter.put("1", 0);
    priorityCounter.put("2", 0);
    priorityCounter.put("3", 0);
    priorityCounter.put("4", 0);
    priorityCounter.put("5", 0);

    calculateMetrics();
  }

  private void calculateMetrics() {
    for (ExtendedIssue extendedIssue : originalIssues) {
      String reporterName = extendedIssue.getIssue().getReporter().getName();

      if (!reporterCounter.containsKey(reporterName)) {
        reporterCounter.put(reporterName, 1);
      } else {
        reporterCounter.put(reporterName, reporterCounter.get(reporterName) + 1);
      }

      // Priorities
      String originalPriorityId = "0";

      if (extendedIssue.getOriginalPriority() != null
          && extendedIssue.getOriginalPriority().getId() != null) {
        originalPriorityId = extendedIssue.getOriginalPriority().getId();
      }

      if (!priorityCounter.containsKey(originalPriorityId)) {
        priorityCounter.put(originalPriorityId, 1);
      } else {
        priorityCounter.put(originalPriorityId, priorityCounter.get(originalPriorityId) + 1);
      }

      if (extendedIssue.isDoesPriorityChanged()) {
        priorityChanges += 1;

        if (!changerCounter.containsKey(reporterName)) {
          changerCounter.put(reporterName, 1);
        } else {
          changerCounter.put(reporterName, changerCounter.get(reporterName) + 1);
        }
      }
    }
  }

  /**
   * The header for a CVS Report.
   * 
   * @return Header as String.
   */
  public static String getMetricHeader() {
    String header = "";
    header += "Period Identifier, ";
    header += "No Priority, ";
    header += "Blocker, ";
    header += "Critical, ";
    header += "Major, ";
    header += "Minor, ";
    header += "Trivial, ";
    header += "Blocker (%), ";
    header += "Critical (%), ";
    header += "Major (%), ";
    header += "Minor (%), ";
    header += "Trivial (%), ";
    header += "Total, ";
    header += "Priority Changes, ";
    header += "Priority Changes (%), ";
    header += "Number of Reporters, ";
    header += "Average Issues per Reporter,";
    header += "Number of Changers, ";
    header += "Top Reporter, ";
    header += "Top Changer, ";

    return header;

  }

  /**
   * Returns a representation of the collected metrics.
   * 
   * @return String representation of all the metric.
   */
  public String getMetricsAsString() {
    String metrics = identifier + ", ";

    metrics += priorityCounter.get("0") + ", ";
    metrics += priorityCounter.get("1") + ", ";
    metrics += priorityCounter.get("2") + ", ";
    metrics += priorityCounter.get("3") + ", ";
    metrics += priorityCounter.get("4") + ", ";
    metrics += priorityCounter.get("5") + ", ";

    Double total = new Double(this.originalIssues.size());

    metrics += priorityCounter.get("1") / total + ", ";
    metrics += priorityCounter.get("2") / total + ", ";
    metrics += priorityCounter.get("3") / total + ", ";
    metrics += priorityCounter.get("4") / total + ", ";
    metrics += priorityCounter.get("5") / total + ", ";
    metrics += total.intValue() + ", ";
    metrics += this.priorityChanges + ", ";
    metrics += this.priorityChanges / total + ", ";
    int numberOfReporters = reporterCounter.keySet().size();
    metrics += numberOfReporters + ",";
    metrics += total / numberOfReporters + ",";
    metrics += changerCounter.keySet().size() + ",";
    metrics += getMaximumKey(reporterCounter) + ", ";
    metrics += getMaximumKey(changerCounter) + ", ";

    return metrics;
  }

  private String getMaximumKey(HashMap<String, Integer> counterMap) {
    String maxKey = "";
    Integer maxValue = -1;

    for (String key : counterMap.keySet()) {
      Integer counter = counterMap.get(key);

      if (counter > maxValue) {
        maxValue = counter;
        maxKey = key;
      }
    }

    return maxKey;
  }

  public HashMap<String, Integer> getPriorityCounter() {
    return priorityCounter;
  }

  public int getPriorityChanges() {
    return priorityChanges;
  }

}
