package crest.jira.data.miner.report.model;

public class CsvConfiguration {

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

  public static final String TIME_PERIOD_IDENTIFIER = "Period Identifier";
  public static final String LOW_SEVERITY_IDENTIFIER = "Non-Severe";
  public static final String HIGH_SEVERITY_IDENTIFIER = "Severe";
  public static final String NUMBER_REPORTERS_IDENTIFIER = "Number of Reporters";
  public static final String TOTAL_IDENTIFIER = "Total";
  public static final String ISSUES_PER_REPORTER_IDENTIFIER = "Average Issues per Reporter";
  public static final String CHANGERS_IDENTIFIER = "Number of Changers";
  public static final String PRIORITY_CHANGES_IDENTIFIER = "Priority Changes";
  public static final String RELATIVE_PRIORITY_CHANGES_IDENTIFIER = "Priority Changes (%)";

  public static final String USER_IDENTIFIER = "User";
  public static final String ABSTENTIONS_IDENTIFIER = "Abstentions";
  public static final String PARTICIPATIONS_IDENTIFIER = "Participations";

}
