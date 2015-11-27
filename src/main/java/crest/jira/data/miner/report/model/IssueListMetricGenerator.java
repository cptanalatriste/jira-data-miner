package crest.jira.data.miner.report.model;

import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

  private Frequency priorityFrequency = new Frequency();
  private Frequency reporterFrequency = new Frequency();
  private Frequency changerFrequency = new Frequency();
  private Frequency resolvedFrequency = new Frequency();
  private Frequency unresolvedFrequency = new Frequency();

  private int priorityChanges = 0;
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
    initializePriorityStatistics(timePerPriorityCounter);
    calculateMetrics();
  }

  private void initializePriorityStatistics(HashMap<String, DescriptiveStatistics> counterMap) {
    for (String priority : PRIORITIES) {
      counterMap.put(priority, new DescriptiveStatistics());
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
      resolvedFrequency.addValue(originalPriorityId);

      DescriptiveStatistics counterPerPriority = timePerPriorityCounter.get(originalPriorityId);
      counterPerPriority.addValue(extendedIssue.getResolutionTime());
    } else {
      unresolvedFrequency.addValue(originalPriorityId);
    }
  }

  private void calculatePriorityMetrics(ExtendedIssue extendedIssue) {

    String reporterName = extendedIssue.getIssue().getReporter().getName();
    reporterFrequency.addValue(reporterName);

    String originalPriorityId = extendedIssue.getOriginalPriority().getId();
    priorityFrequency.addValue(originalPriorityId);

    if (extendedIssue.isDoesPriorityChanged()) {
      priorityChanges += 1;
      changerFrequency.addValue(reporterName);
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
      metrics.add(priorityFrequency.getCount(priority));
      metrics.add(Double.isNaN(priorityFrequency.getPct(priority)) ? 0.0
          : priorityFrequency.getPct(priority));
      metrics.add(resolvedFrequency.getCount(priority));
      metrics.add(Double.isNaN(resolvedFrequency.getPct(priority)) ? 0.0
          : resolvedFrequency.getPct(priority));
      metrics.add(unresolvedFrequency.getCount(priority));
      metrics.add(Double.isNaN(unresolvedFrequency.getPct(priority)) ? 0.0
          : unresolvedFrequency.getPct(priority));

      DescriptiveStatistics timeDescriptiveStats = timePerPriorityCounter.get(priority);
      metrics
          .add(Double.isNaN(timeDescriptiveStats.getMean()) ? 0.0 : timeDescriptiveStats.getMean());
      metrics.add(Double.isNaN(timeDescriptiveStats.getPercentile(50)) ? 0.0
          : timeDescriptiveStats.getPercentile(50));
      metrics.add(Double.isNaN(timeDescriptiveStats.getStandardDeviation()) ? 0.0
          : timeDescriptiveStats.getStandardDeviation());
    }

    metrics.add(numberOfIssues.intValue());

    metrics.add(priorityFrequency.getPct(new Comparable<String>() {
      @Override
      public int compareTo(String object) {
        if (object.equals("4") || object.equals("5")) {
          return 0;
        }
        return -1;
      }
    }));

    metrics.add(priorityFrequency.getPct(new Comparable<String>() {
      @Override
      public int compareTo(String object) {
        if (object.equals("1") || object.equals("2")) {
          return 0;
        }
        return 1;
      }
    }));

    metrics.add(this.priorityChanges);
    metrics.add(this.priorityChanges / numberOfIssues);

    int numberOfReporters = getNumberOfReporters();
    metrics.add(numberOfReporters);
    metrics.add(numberOfIssues / numberOfReporters);

    metrics.add(changerFrequency.getUniqueCount());

    metrics.add(reporterFrequency.getMode().isEmpty() ? null : reporterFrequency.getMode().get(0));
    metrics.add(changerFrequency.getMode().isEmpty() ? null : changerFrequency.getMode().get(0));
    return metrics;
  }

  public int getNumberOfReporters() {
    return reporterFrequency.getUniqueCount();
  }

  public int getNumberOfIssues() {
    return this.originalIssues.size();
  }

  public int getPriorityChanges() {
    return priorityChanges;
  }

}
