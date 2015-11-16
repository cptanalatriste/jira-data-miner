package crest.jira.data.miner.report.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IssueListMetricGenerator {

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
  public IssueListMetricGenerator(String identifier, List<ExtendedIssue> originalIssues) {
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
  public static String[] getMetricHeader() {
    String[] header = new String[] { "Period Identifier", "No Priority", "Blocker", "Critical",
        "Major", "Minor", "Trivial", "No Priority (%)", "Blocker (%)", "Critical (%)", "Major (%)",
        "Minor (%)", "Trivial (%)", "Total", "Non-Severe (%)", "Severe (%)", "Priority Changes",
        "Priority Changes (%)", "Number of Reporters", "Average Issues per Reporter",
        "Number of Changers", "Top Reporter", "Top Changer" };

    return header;

  }

  /**
   * Returns a representation of the collected metrics.
   * 
   * @return String representation of all the metric.
   */
  public List<Object> getMetricsAsList() {
    List<Object> metrics = new ArrayList<>();

    metrics.add(identifier);
    metrics.add(priorityCounter.get("0"));
    metrics.add(priorityCounter.get("1"));
    metrics.add(priorityCounter.get("2"));
    metrics.add(priorityCounter.get("3"));
    metrics.add(priorityCounter.get("4"));
    metrics.add(priorityCounter.get("5"));

    Double total = new Double(getNumberOfIssues());
    metrics.add(priorityCounter.get("0") / total);
    metrics.add(priorityCounter.get("1") / total);
    metrics.add(priorityCounter.get("2") / total);
    metrics.add(priorityCounter.get("3") / total);
    metrics.add(priorityCounter.get("4") / total);
    metrics.add(priorityCounter.get("5") / total);
    metrics.add(total.intValue());
    metrics.add((priorityCounter.get("4") + priorityCounter.get("5")) / total);
    metrics.add((priorityCounter.get("1") + priorityCounter.get("2")) / total);

    metrics.add(this.priorityChanges);
    metrics.add(this.priorityChanges / total);

    int numberOfReporters = getNumberOfReporters();
    metrics.add(numberOfReporters);
    metrics.add(total / numberOfReporters);

    metrics.add(changerCounter.keySet().size());
    metrics.add(getMaximumKey(reporterCounter));
    metrics.add(getMaximumKey(changerCounter));
    return metrics;
  }

  public int getNumberOfReporters() {
    return reporterCounter.keySet().size();
  }

  public int getNumberOfIssues() {
    return this.originalIssues.size();
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
