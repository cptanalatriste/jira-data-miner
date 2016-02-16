package crest.jira.data.miner.report.model;

import crest.jira.data.miner.csv.CsvExportSupport;
import crest.jira.data.miner.csv.JiraCsvConfiguration;
import crest.jira.data.retriever.model.User;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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
  private HashMap<String, DescriptiveStatistics> timePerPriorityCounters = new HashMap<>();
  private HashMap<String, DescriptiveStatistics> releasesPerPriorityCounters = new HashMap<>();
  private List<ExtendedIssue> originalIssues;
  private T identifier;

  private MultiValuedMap<String, ExtendedIssue> issuesPerReporter = MultiMapUtils
      .newSetValuedHashMap();

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
    initializePriorityStatistics(timePerPriorityCounters);
    initializePriorityStatistics(releasesPerPriorityCounters);
    calculateMetrics();
  }

  private void initializePriorityStatistics(HashMap<String, DescriptiveStatistics> counterMap) {
    for (String priority : JiraCsvConfiguration.PRIORITIES) {
      counterMap.put(priority, new DescriptiveStatistics());
    }
  }

  private void calculateMetrics() {
    if (originalIssues != null && originalIssues.size() > 0) {
      for (ExtendedIssue extendedIssue : originalIssues) {
        try {
          this.calculatePriorityMetrics(extendedIssue);
          this.calculateResolutionTimeMetrics(extendedIssue);
          this.calculateReleaseRelatedMetrics(extendedIssue);
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

  private void calculateReleaseRelatedMetrics(ExtendedIssue extendedIssue) {
    String originalPriorityId = extendedIssue.getOriginalPriority().getId();
    DescriptiveStatistics counterPerPriority = releasesPerPriorityCounters.get(originalPriorityId);
    Integer releasesToFix = extendedIssue.getReleasesToBeFixed();
    if (releasesToFix != null) {
      counterPerPriority.addValue(releasesToFix);
    }
  }

  private void calculateResolutionTimeMetrics(ExtendedIssue extendedIssue) {
    String originalPriorityId = extendedIssue.getOriginalPriority().getId();

    if (extendedIssue.isResolved()) {
      resolvedFrequency.addValue(originalPriorityId);

      DescriptiveStatistics counterPerPriority = timePerPriorityCounters.get(originalPriorityId);
      Double resolutionTime = extendedIssue.getResolutionTime();
      counterPerPriority.addValue(resolutionTime);

      if (extendedIssue.isReportedSevere()) {
        severeResolutionTime.addValue(resolutionTime);
      } else if (extendedIssue.isReportedNonSevere()) {
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
    headerAsString.add(JiraCsvConfiguration.TIME_PERIOD_IDENTIFIER);

    for (String priority : JiraCsvConfiguration.PRIORITIES) {
      String priorityDescription = JiraCsvConfiguration.PRIORITY_DESCRIPTIONS[Integer
          .parseInt(priority)];

      headerAsString.add(priorityDescription);
      headerAsString.add(priorityDescription + JiraCsvConfiguration.RELATIVE_SUFIX);
      headerAsString.add(priorityDescription + JiraCsvConfiguration.RESOLVED_SUFIX);
      headerAsString.add(priorityDescription + JiraCsvConfiguration.RESOLVED_RELATIVE_SUFIX);
      headerAsString.add(priorityDescription + JiraCsvConfiguration.UNRESOLVED_SUFIX);
      headerAsString.add(priorityDescription + JiraCsvConfiguration.UNRESOLVED_RELATIVE_SUFIX);
      headerAsString.add(priorityDescription + JiraCsvConfiguration.RESTIME_AVG_SUFFIX);
      headerAsString.add(priorityDescription + JiraCsvConfiguration.RESTIME_MED_SUFFIX);
      headerAsString.add(priorityDescription + JiraCsvConfiguration.RESTIME_STD_SUFFIX);
      headerAsString.add(priorityDescription + JiraCsvConfiguration.RELEASES_TO_FIX_SUFFIX);
      headerAsString.add(priorityDescription + JiraCsvConfiguration.RELEASES_TO_FIX_STD_SUFFIX);

    }

    headerAsString.addAll(Arrays.asList(JiraCsvConfiguration.TOTAL_IDENTIFIER,
        JiraCsvConfiguration.TOTAL_IDENTIFIER + JiraCsvConfiguration.RESTIME_MED_SUFFIX,
        JiraCsvConfiguration.NON_SEVERE_IDENTIFIER + JiraCsvConfiguration.RELATIVE_SUFIX,
        JiraCsvConfiguration.SEVERE_IDENTIFIER + JiraCsvConfiguration.RELATIVE_SUFIX,
        JiraCsvConfiguration.NON_SEVERE_IDENTIFIER + JiraCsvConfiguration.RESTIME_MED_SUFFIX,
        JiraCsvConfiguration.SEVERE_IDENTIFIER + JiraCsvConfiguration.RESTIME_MED_SUFFIX,
        JiraCsvConfiguration.NON_SEVERE_IDENTIFIER + JiraCsvConfiguration.UNRESOLVED_RELATIVE_SUFIX,
        JiraCsvConfiguration.SEVERE_IDENTIFIER + JiraCsvConfiguration.UNRESOLVED_RELATIVE_SUFIX,
        JiraCsvConfiguration.PRIORITY_CHANGES_IDENTIFIER,
        JiraCsvConfiguration.RELATIVE_PRIORITY_CHANGES_IDENTIFIER,
        JiraCsvConfiguration.NUMBER_REPORTERS_IDENTIFIER,
        JiraCsvConfiguration.ISSUES_PER_REPORTER_IDENTIFIER,
        JiraCsvConfiguration.CHANGERS_IDENTIFIER, "Top Reporter", "Top Changer"));

    return headerAsString.toArray(new String[headerAsString.size()]);

  }

  @Override
  public List<Object> getCsvRecord() {
    List<Object> recordAsList = new ArrayList<>();

    Double numberOfIssues = new Double(getNumberOfIssues());
    recordAsList.add(identifier.toString());

    for (String priority : JiraCsvConfiguration.PRIORITIES) {
      recordAsList.add(priorityFrequency.getCount(priority));
      recordAsList.add(getRelativeFrequencyByPriority(priority));
      recordAsList.add(resolvedFrequency.getCount(priority));
      recordAsList.add(resolvedFrequency.getPct(priority));
      recordAsList.add(unresolvedFrequency.getCount(priority));
      recordAsList.add(unresolvedFrequency.getPct(priority));

      DescriptiveStatistics timeDescriptiveStats = timePerPriorityCounters.get(priority);

      recordAsList.add(timeDescriptiveStats.getMean());
      recordAsList.add(timeDescriptiveStats.getPercentile(50));
      recordAsList.add(timeDescriptiveStats.getStandardDeviation());

      DescriptiveStatistics releasesToFixStats = releasesPerPriorityCounters.get(priority);
      recordAsList.add(releasesToFixStats.getPercentile(50));
      recordAsList.add(releasesToFixStats.getStandardDeviation());

    }

    recordAsList.add(numberOfIssues.intValue());
    recordAsList.add(generalResolutionTime.getPercentile(50));

    recordAsList.add(getNonSevereRelativeFrequency());
    recordAsList.add(getSevereRelativeFrequency());

    recordAsList.add(nonSevereResolutionTime.getPercentile(50));
    recordAsList.add(severeResolutionTime.getPercentile(50));

    recordAsList.add(unresolvedFrequency.getPct("4") + unresolvedFrequency.getPct("5"));
    recordAsList.add(unresolvedFrequency.getPct("1") + unresolvedFrequency.getPct("2"));

    recordAsList.add(this.priorityChanges);
    recordAsList.add(this.priorityChanges / numberOfIssues);

    int numberOfReporters = getNumberOfReporters();
    recordAsList.add(numberOfReporters);
    recordAsList.add(numberOfIssues / numberOfReporters);

    recordAsList.add(changerFrequency.getUniqueCount());

    recordAsList
        .add(reporterFrequency.getMode().isEmpty() ? null : reporterFrequency.getMode().get(0));
    recordAsList
        .add(changerFrequency.getMode().isEmpty() ? null : changerFrequency.getMode().get(0));
    return recordAsList;
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
  public JiraIssueBag<T> getIssuesPerReporter(User reporter) {
    Set<ExtendedIssue> thisReporterIssues = (Set<ExtendedIssue>) (issuesPerReporter
        .get(reporter.getName()));
    return new JiraIssueBag<T>(this.identifier, new ArrayList<>(thisReporterIssues));
  }

}
