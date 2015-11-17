package crest.jira.data.miner.report.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueListMetricGenerator {

  private static String[] supportedPriorities = new String[] { "0", "1", "2", "3", "4", "5" };
  private static String[] prioritiesDescriptions = new String[] { "No Priority", "Blocker",
      "Critical", "Major", "Minor", "Trivial" };

  private HashMap<String, Double> priorityCounter = new HashMap<String, Double>();
  private HashMap<String, Double> reporterCounter = new HashMap<String, Double>();
  private HashMap<String, Double> changerCounter = new HashMap<String, Double>();

  private int priorityChanges = 0;
  private HashMap<String, Double> resolvedCounter = new HashMap<String, Double>();
  private HashMap<String, Double> unresolvedCounter = new HashMap<String, Double>();
  private HashMap<String, Double> timePerPriorityCounter = new HashMap<String, Double>();
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

    initializePriorityCounter(priorityCounter);
    initializePriorityCounter(resolvedCounter);
    initializePriorityCounter(unresolvedCounter);
    initializePriorityCounter(timePerPriorityCounter);

    calculateMetrics();
  }

  private void initializePriorityCounter(HashMap<String, Double> counterMap) {
    for (String priority : supportedPriorities) {
      counterMap.put(priority, 0.0);
    }
  }

  private void calculateMetrics() {
    for (ExtendedIssue extendedIssue : originalIssues) {
      this.calculatePriorityMetrics(extendedIssue);
      this.calculateResolutionTimeMetrics(extendedIssue);
    }
  }

  private void calculateResolutionTimeMetrics(ExtendedIssue extendedIssue) {
    String originalPriorityId = extendedIssue.getOriginalPriority().getId();

    if (extendedIssue.isResolved()) {
      updateCounterMap(originalPriorityId, resolvedCounter);
      updateCounterMap(originalPriorityId, timePerPriorityCounter,
          extendedIssue.getResolutionTime());
    } else {
      updateCounterMap(originalPriorityId, unresolvedCounter);
    }
  }

  private void calculatePriorityMetrics(ExtendedIssue extendedIssue) {

    String reporterName = extendedIssue.getIssue().getReporter().getName();
    updateCounterMap(reporterName, reporterCounter);

    String originalPriorityId = extendedIssue.getOriginalPriority().getId();
    updateCounterMap(originalPriorityId, priorityCounter);

    if (extendedIssue.isDoesPriorityChanged()) {
      priorityChanges += 1;
      updateCounterMap(reporterName, changerCounter);
    }
  }

  /**
   * The header for a CVS Report.
   * 
   * @return Header as String.
   */
  public static String[] getMetricHeader() {
    List<String> headerAsString = new ArrayList<String>();
    headerAsString.add("Period Identifier");

    for (String priority : supportedPriorities) {
      String priorityDescription = prioritiesDescriptions[Integer.parseInt(priority)];

      headerAsString.add(priorityDescription);
      headerAsString.add(priorityDescription + " (%)");
      headerAsString.add(priorityDescription + " Resolved");
      headerAsString.add(priorityDescription + " Resolved (%)");
      headerAsString.add(priorityDescription + " Unresolved");
      headerAsString.add(priorityDescription + " Unresolved (%)");
      headerAsString.add(priorityDescription + " Resolution Time (avg)");
    }

    headerAsString.addAll(Arrays.asList("Total", "Non-Severe (%)", "Severe (%)", "Priority Changes",
        "Priority Changes (%)", "Number of Reporters", "Average Issues per Reporter",
        "Number of Changers", "Top Reporter", "Top Changer"));

    return headerAsString.toArray(new String[headerAsString.size()]);

  }

  /**
   * Returns a representation of the collected metrics.
   * 
   * @return String representation of all the metric.
   */
  public List<Object> getMetricsAsList() {
    List<Object> metrics = new ArrayList<>();

    Double numberOfIssues = new Double(getNumberOfIssues());

    metrics.add(identifier);
    for (String priority : supportedPriorities) {
      int frequencyPerPriority = priorityCounter.get(priority).intValue();
      metrics.add(frequencyPerPriority);
      Double relativeFrequency = numberOfIssues != 0 ? frequencyPerPriority / numberOfIssues : 0;
      metrics.add(relativeFrequency);

      int resolvedPerPriority = resolvedCounter.get(priority).intValue();
      metrics.add(resolvedPerPriority);
      double relativeResolved = frequencyPerPriority != 0
          ? resolvedPerPriority / new Double(frequencyPerPriority) : 0;
      metrics.add(relativeResolved);

      int unresolvedPerPriority = unresolvedCounter.get(priority).intValue();
      metrics.add(unresolvedPerPriority);
      double relativeUnresolved = frequencyPerPriority != 0
          ? unresolvedPerPriority / new Double(frequencyPerPriority) : 0;
      metrics.add(relativeUnresolved);

      double averageTime = frequencyPerPriority != 0
          ? timePerPriorityCounter.get(priority) / frequencyPerPriority : 0;
      metrics.add(averageTime);
    }

    metrics.add(numberOfIssues.intValue());
    metrics.add((priorityCounter.get("4") + priorityCounter.get("5")) / numberOfIssues);
    metrics.add((priorityCounter.get("1") + priorityCounter.get("2")) / numberOfIssues);

    metrics.add(this.priorityChanges);
    metrics.add(this.priorityChanges / numberOfIssues);

    int numberOfReporters = getNumberOfReporters();
    metrics.add(numberOfReporters);
    metrics.add(numberOfIssues / numberOfReporters);

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

  private String getMaximumKey(HashMap<String, Double> counterMap) {
    String maxKey = "";
    Double maxValue = -1.0;

    for (String key : counterMap.keySet()) {
      Double counter = counterMap.get(key);

      if (counter > maxValue) {
        maxValue = counter;
        maxKey = key;
      }
    }

    return maxKey;
  }

  private void updateCounterMap(String key, HashMap<String, Double> counterMap) {
    this.updateCounterMap(key, counterMap, 1.0);
  }

  private void updateCounterMap(String key, Map<String, Double> counterMap, Double amount) {
    if (!counterMap.containsKey(key)) {
      counterMap.put(key, amount);
    } else {
      counterMap.put(key, counterMap.get(key) + amount);
    }
  }

  public HashMap<String, Double> getPriorityCounter() {
    return priorityCounter;
  }

  public int getPriorityChanges() {
    return priorityChanges;
  }

}
