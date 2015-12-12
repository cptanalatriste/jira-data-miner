package crest.jira.data.miner.report.model;

import crest.jira.data.retriever.model.User;

import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JiraIssueBag<T> implements CsvExportSupport {

  private static Logger logger = Logger.getLogger(JiraIssueBag.class.getName());

  private Frequency priorityFrequency = new Frequency();
  private Frequency reporterFrequency = new Frequency();
  private Frequency changerFrequency = new Frequency();
  private Frequency resolvedFrequency = new Frequency();
  private Frequency unresolvedFrequency = new Frequency();
  private DescriptiveStatistics generalResolutionTime = new DescriptiveStatistics();
  private DescriptiveStatistics severeResolutionTime = new DescriptiveStatistics();
  private DescriptiveStatistics nonSevereResolutionTime = new DescriptiveStatistics();

  private int priorityChanges = 0;
  private HashMap<String, DescriptiveStatistics> timePerPriorityCounter = new HashMap<>();
  private List<ExtendedIssue> originalIssues;
  private T identifier;

  private MultiValueMap<String, ExtendedIssue> issuesPerReporter = new MultiValueMap<>();

  public JiraIssueBag() {
  }

  /**
   * Calculates metrics for a specific list of Issues.
   * 
   * @param originalIssues
   *          List of issues.
   */
  public JiraIssueBag(T identifier, List<ExtendedIssue> originalIssues) {
    this.identifier = identifier;
    this.originalIssues = originalIssues;
    initializePriorityStatistics(timePerPriorityCounter);
    calculateMetrics();
  }

  private void initializePriorityStatistics(HashMap<String, DescriptiveStatistics> counterMap) {
    for (String priority : CsvConfiguration.PRIORITIES) {
      counterMap.put(priority, new DescriptiveStatistics());
    }
  }

  private void calculateMetrics() {
    if (originalIssues != null && originalIssues.size() > 0) {
      for (ExtendedIssue extendedIssue : originalIssues) {
        try {
          this.calculatePriorityMetrics(extendedIssue);
          this.calculateResolutionTimeMetrics(extendedIssue);
          this.calculateReporterMetrics(extendedIssue);
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Error while processing issue: " + extendedIssue, e);
          throw new RuntimeException(e);
        }
      }
    }
  }

  private void calculateReporterMetrics(ExtendedIssue extendedIssue) {

    String reporterName = extendedIssue.getIssue().getReporter().getName();
    reporterFrequency.addValue(reporterName);
    issuesPerReporter.put(reporterName, extendedIssue);

    if (extendedIssue.isDoesPriorityChanged()) {
      priorityChanges += 1;
      changerFrequency.addValue(reporterName);
    }
  }

  private void calculateResolutionTimeMetrics(ExtendedIssue extendedIssue) {
    String originalPriorityId = extendedIssue.getOriginalPriority().getId();

    if (extendedIssue.isResolved()) {
      resolvedFrequency.addValue(originalPriorityId);

      DescriptiveStatistics counterPerPriority = timePerPriorityCounter.get(originalPriorityId);
      Double resolutionTime = extendedIssue.getResolutionTime();
      counterPerPriority.addValue(resolutionTime);

      if (extendedIssue.isSevere()) {
        severeResolutionTime.addValue(resolutionTime);
      } else if (extendedIssue.isNonSevere()) {
        nonSevereResolutionTime.addValue(resolutionTime);
      }

      generalResolutionTime.addValue(resolutionTime);
    } else {
      unresolvedFrequency.addValue(originalPriorityId);
    }
  }

  private void calculatePriorityMetrics(ExtendedIssue extendedIssue) {

    String originalPriorityId = extendedIssue.getOriginalPriority().getId();
    priorityFrequency.addValue(originalPriorityId);

  }

  @Override
  public String[] getCsvHeader() {
    List<String> headerAsString = new ArrayList<String>();
    headerAsString.add(CsvConfiguration.TIME_PERIOD_IDENTIFIER);

    for (String priority : CsvConfiguration.PRIORITIES) {
      String priorityDescription = CsvConfiguration.PRIORITY_DESCRIPTIONS[Integer
          .parseInt(priority)];

      headerAsString.add(priorityDescription);
      headerAsString.add(priorityDescription + CsvConfiguration.RELATIVE_SUFIX);
      headerAsString.add(priorityDescription + CsvConfiguration.RESOLVED_SUFIX);
      headerAsString.add(priorityDescription + CsvConfiguration.RESOLVED_RELATIVE_SUFIX);
      headerAsString.add(priorityDescription + CsvConfiguration.UNRESOLVED_SUFIX);
      headerAsString.add(priorityDescription + CsvConfiguration.UNRESOLVED_RELATIVE_SUFIX);
      headerAsString.add(priorityDescription + CsvConfiguration.RESTIME_AVG_SUFFIX);
      headerAsString.add(priorityDescription + CsvConfiguration.RESTIME_MED_SUFFIX);
      headerAsString.add(priorityDescription + CsvConfiguration.RESTIME_STD_SUFFIX);

    }

    headerAsString.addAll(Arrays.asList(CsvConfiguration.TOTAL_IDENTIFIER,
        CsvConfiguration.TOTAL_IDENTIFIER + CsvConfiguration.RESTIME_MED_SUFFIX,
        CsvConfiguration.NON_SEVERE_IDENTIFIER + CsvConfiguration.RELATIVE_SUFIX,
        CsvConfiguration.SEVERE_IDENTIFIER + CsvConfiguration.RELATIVE_SUFIX,
        CsvConfiguration.NON_SEVERE_IDENTIFIER + CsvConfiguration.RESTIME_MED_SUFFIX,
        CsvConfiguration.SEVERE_IDENTIFIER + CsvConfiguration.RESTIME_MED_SUFFIX,
        CsvConfiguration.NON_SEVERE_IDENTIFIER + CsvConfiguration.UNRESOLVED_RELATIVE_SUFIX,
        CsvConfiguration.SEVERE_IDENTIFIER + CsvConfiguration.UNRESOLVED_RELATIVE_SUFIX,
        CsvConfiguration.PRIORITY_CHANGES_IDENTIFIER,
        CsvConfiguration.RELATIVE_PRIORITY_CHANGES_IDENTIFIER,
        CsvConfiguration.NUMBER_REPORTERS_IDENTIFIER,
        CsvConfiguration.ISSUES_PER_REPORTER_IDENTIFIER, CsvConfiguration.CHANGERS_IDENTIFIER,
        "Top Reporter", "Top Changer"));

    return headerAsString.toArray(new String[headerAsString.size()]);

  }

  @Override
  public List<Object> getCsvRecord() {
    List<Object> metrics = new ArrayList<>();

    Double numberOfIssues = new Double(getNumberOfIssues());

    metrics.add(identifier.toString());
    for (String priority : CsvConfiguration.PRIORITIES) {
      metrics.add(priorityFrequency.getCount(priority));
      metrics.add(getRelativeFrequencyByPriority(priority));
      metrics.add(resolvedFrequency.getCount(priority));
      metrics.add(resolvedFrequency.getPct(priority));
      metrics.add(unresolvedFrequency.getCount(priority));
      metrics.add(unresolvedFrequency.getPct(priority));

      DescriptiveStatistics timeDescriptiveStats = timePerPriorityCounter.get(priority);

      metrics.add(timeDescriptiveStats.getMean());
      metrics.add(timeDescriptiveStats.getPercentile(50));
      metrics.add(timeDescriptiveStats.getStandardDeviation());
    }

    metrics.add(numberOfIssues.intValue());
    metrics.add(generalResolutionTime.getPercentile(50));

    metrics.add(getNonSevereRelativeFrequency());
    metrics.add(getSevereRelativeFrequency());

    metrics.add(nonSevereResolutionTime.getPercentile(50));
    metrics.add(severeResolutionTime.getPercentile(50));

    metrics.add(unresolvedFrequency.getPct("4") + unresolvedFrequency.getPct("5"));
    metrics.add(unresolvedFrequency.getPct("1") + unresolvedFrequency.getPct("2"));

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

  public double getRelativeFrequencyByPriority(String priority) {
    return priorityFrequency.getPct(priority);
  }

  public double getSevereRelativeFrequency() {
    return priorityFrequency.getPct("1") + priorityFrequency.getPct("2");
  }

  public double getNonSevereRelativeFrequency() {
    return priorityFrequency.getPct("4") + priorityFrequency.getPct("5");
  }

  public int getNumberOfReporters() {
    return reporterFrequency.getUniqueCount();
  }

  public int getNumberOfIssues() {
    return this.originalIssues != null ? this.originalIssues.size() : 0;
  }

  public int getPriorityChanges() {
    return priorityChanges;
  }

  public T getIdentifier() {
    return identifier;
  }

  /**
   * Returns a Bag of issues corresponding to an specific reporter.
   * 
   * @param reporter
   *          Reporter.
   * @return An issue bag for this reporter.
   */
  @SuppressWarnings("unchecked")
  public JiraIssueBag<T> getIssuesPerReporter(User reporter) {
    List<ExtendedIssue> thisReporterIssues = (List<ExtendedIssue>) issuesPerReporter
        .get(reporter.getName());
    return new JiraIssueBag<T>(this.identifier, thisReporterIssues);
  }

}
