package crest.jira.data.miner.report.model;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IssueListMetricGenerator {

  private static Logger logger = Logger.getLogger(IssueListMetricGenerator.class.getName());

  public static final String[] PRIORITIES = new String[] { "0", "1", "2", "3", "4", "5" };
  public static final String[] PRIORITY_DESCRIPTIONS = new String[] { "No Priority", "Blocker",
      "Critical", "Major", "Minor", "Trivial" };

  public static final String RESTIME_STD_SUFFIX = " Resolution Time (std)";
  public static final String RESTIME_MED_SUFFIX = " Resolution Time (med)";
  public static final String RESTIME_AVG_SUFFIX = " Resolution Time (avg)";
  public static final String UNRESOLVED_RELATIVE_SUFIX = " Unresolved (%)";
  public static final String UNRESOLVED_SUFIX = " Unresolved";
  public static final String RESOLVED_RELATIVE_SUFIX = " Resolved (%)";
  public static final String RESOLVED_SUFIX = " Resolved";
  public static final String RELATIVE_SUFIX = " (%)";
  public static final String FREQUENCIES_SUFIX = "";

  public static final String PERIOD_IDENTIFIER = "Period Identifier";
  public static final String LOW_SEVERITY_IDENTIFIER = "Non-Severe (%)";
  public static final String HIGH_SEVERITY_IDENTIFIER = "Severe (%)";
  public static final String NUMBER_REPORTERS_IDENTIFIER = "Number of Reporters";
  public static final String TOTAL_IDENTIFIER = "Total";
  public static final String ISSUES_PER_REPORTER_IDENTIFIER = "Average Issues per Reporter";
  public static final String CHANGERS_IDENTIFIER = "Number of Changers";
  public static final String PRIORITY_CHANGES_IDENTIFIER = "Priority Changes";
  public static final String RELATIVE_PRIORITY_CHANGES_IDENTIFIER = "Priority Changes (%)";

  private HashMap<String, Double> priorityCounter = new HashMap<String, Double>();
  private HashMap<String, Double> reporterCounter = new HashMap<String, Double>();
  private HashMap<String, Double> changerCounter = new HashMap<String, Double>();

  private int priorityChanges = 0;
  private HashMap<String, Double> resolvedCounter = new HashMap<String, Double>();
  private HashMap<String, Double> unresolvedCounter = new HashMap<String, Double>();
  private HashMap<String, DescriptiveStatistics> timePerPriorityCounter = new HashMap<>();
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
    initializePriorityStatistics(timePerPriorityCounter);

    calculateMetrics();
  }

  private void initializePriorityStatistics(HashMap<String, DescriptiveStatistics> counterMap) {
    for (String priority : PRIORITIES) {
      counterMap.put(priority, new DescriptiveStatistics());
    }
  }

  private void initializePriorityCounter(HashMap<String, Double> counterMap) {
    for (String priority : PRIORITIES) {
      counterMap.put(priority, 0.0);
    }
  }

  private void calculateMetrics() {
    for (ExtendedIssue extendedIssue : originalIssues) {
      try {
        this.calculatePriorityMetrics(extendedIssue);
        this.calculateResolutionTimeMetrics(extendedIssue);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error while processing issue: " + extendedIssue, e);
        throw new RuntimeException(e);
      }
    }
  }

  private void calculateResolutionTimeMetrics(ExtendedIssue extendedIssue) {
    String originalPriorityId = extendedIssue.getOriginalPriority().getId();

    if (extendedIssue.isResolved()) {
      updateCounterMap(originalPriorityId, resolvedCounter);

      DescriptiveStatistics counterPerPriority = timePerPriorityCounter.get(originalPriorityId);
      counterPerPriority.addValue(extendedIssue.getResolutionTime());
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
    headerAsString.add(PERIOD_IDENTIFIER);

    for (String priority : PRIORITIES) {
      String priorityDescription = PRIORITY_DESCRIPTIONS[Integer.parseInt(priority)];

      headerAsString.add(priorityDescription);
      headerAsString.add(priorityDescription + RELATIVE_SUFIX);
      headerAsString.add(priorityDescription + RESOLVED_SUFIX);
      headerAsString.add(priorityDescription + RESOLVED_RELATIVE_SUFIX);
      headerAsString.add(priorityDescription + UNRESOLVED_SUFIX);
      headerAsString.add(priorityDescription + UNRESOLVED_RELATIVE_SUFIX);
      headerAsString.add(priorityDescription + RESTIME_AVG_SUFFIX);
      headerAsString.add(priorityDescription + RESTIME_MED_SUFFIX);
      headerAsString.add(priorityDescription + RESTIME_STD_SUFFIX);

    }

    headerAsString.addAll(Arrays.asList(TOTAL_IDENTIFIER, LOW_SEVERITY_IDENTIFIER,
        HIGH_SEVERITY_IDENTIFIER, PRIORITY_CHANGES_IDENTIFIER, RELATIVE_PRIORITY_CHANGES_IDENTIFIER,
        NUMBER_REPORTERS_IDENTIFIER, ISSUES_PER_REPORTER_IDENTIFIER, CHANGERS_IDENTIFIER,
        "Top Reporter", "Top Changer"));

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
    for (String priority : PRIORITIES) {
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

      DescriptiveStatistics timeDescriptiveStats = timePerPriorityCounter.get(priority);
      double mean = 0.0;
      double median = 0.0;
      double standardDeviation = 0.0;

      if (timeDescriptiveStats.getValues().length > 0) {
        mean = timeDescriptiveStats.getMean();
        median = timeDescriptiveStats.getPercentile(50);
        standardDeviation = timeDescriptiveStats.getStandardDeviation();
      }

      metrics.add(mean);
      metrics.add(median);
      metrics.add(standardDeviation);
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
