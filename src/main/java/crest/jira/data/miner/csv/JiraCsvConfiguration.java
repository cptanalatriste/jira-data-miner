package crest.jira.data.miner.csv;

public class JiraCsvConfiguration {

    public static final String[] PRIORITIES = new String[]{"0", "1", "2", "3", "4", "5"};
    public static final String[] PRIORITY_DESCRIPTIONS = new String[]{"No Priority", "Blocker",
            "Critical", "Major", "Minor", "Trivial"};

    public static final String RESTIME_STD_SUFFIX = " Resolution Time (std)";
    public static final String RESTIME_MED_SUFFIX = " Resolution Time (med)";
    public static final String RESTIME_AVG_SUFFIX = " Resolution Time (avg)";
    public static final String UNRESOLVED_RELATIVE_SUFIX = " Unresolved (%)";
    public static final String UNRESOLVED_SUFIX = " Unresolved";
    public static final String RESOLVED_RELATIVE_SUFIX = " Resolved (%)";
    public static final String RESOLVED_SUFIX = " Resolved";
    public static final String RELATIVE_SUFIX = " (%)";
    public static final String FREQUENCIES_SUFIX = "";
    // TODO(cgavidia): Change this later.
    public static final String RELEASES_TO_FIX_SUFFIX = " Releases to Fix (med)";
    public static final String RELEASES_TO_FIX_STD_SUFFIX = " Releases to Fix (std)";

    public static final String TIME_PERIOD_IDENTIFIER = "Period Identifier";
    public static final String NON_SEVERE_IDENTIFIER = "Non-Severe";
    public static final String SEVERE_IDENTIFIER = "Severe";
    public static final String NUMBER_REPORTERS_IDENTIFIER = "Number of Reporters";
    public static final String TOTAL_IDENTIFIER = "Total";
    public static final String ISSUES_PER_REPORTER_IDENTIFIER = "Average Issues per Reporter";
    public static final String CHANGERS_IDENTIFIER = "Number of Changers";
    public static final String PRIORITY_CHANGES_IDENTIFIER = "Priority Changes";
    public static final String RELATIVE_PRIORITY_CHANGES_IDENTIFIER = "Priority Changes (%)";

    public static final String USER_IDENTIFIER = "User";
    public static final String ABSTENTIONS_IDENTIFIER = "Abstentions";
    public static final String PARTICIPATIONS_IDENTIFIER = "Participations";

    public static final String ISSUE_KEY = "Issue Key";
    public static final String ISSUE_TYPE = "Issue Type";
    public static final String CLOSEST_RELEASE_NAME = "Closest Release";
    public static final String CLOSEST_RELEASE_INDEX = "Closest Release Index";
    public static final String CLOSEST_RELEASE_DATE = "Closest Release Date";
    public static final String CREATION_DATE = "Creation Date";
    public static final String REPORTER = "Reporter";
    public static final String EARLIEST_FIX_NAME = "Earliest Fix Version";
    public static final String EARLIEST_RELEASE_INDEX = "Earliest Fix Version Index";
    public static final String EARLIEST_FIX_DATE = "Earliest Fix Version Date";
    public static final String ORIGINAL_PRIORITY = "Original Priority";
    public static final String CURRENT_PRIORITY = "Current Priority";
    public static final String AFFECTED_VERSION = "Affected Version";
    public static final String IS_ACCEPTED_BY_DEV = "Accepted by Dev";
    public static final String FIX_VERSION = "Fix Version";
    public static final String AFFECTED_VERSION_INDEX = "Affected Version Index";
    public static final String TIME_FRAME_KEY = "Time Frame Key";
    public static final String RESOLUTION = "Resolution";
    public static final String STATUS = "Status";

    public static final String IS_REJECTED = "Is Rejected";
    public static final String IS_IGNORED = "Is Ignored";
}
