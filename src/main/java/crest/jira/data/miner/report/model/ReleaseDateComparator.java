package crest.jira.data.miner.report.model;

import crest.jira.data.retriever.model.Version;

import java.util.Comparator;
import java.util.Date;

public class ReleaseDateComparator implements Comparator<Version> {

  public static final String MAXIMUM_VERSION = "Future";
  public static final String MINIMUM_VERSION = "pre-4.0.0";

  @Override
  public int compare(Version oneVersion, Version anotherVersion) {

    if (oneVersion.getName().equals(anotherVersion.getName())) {
      return 0;
    }

    if (MINIMUM_VERSION.equals(oneVersion.getName())
        || MAXIMUM_VERSION.equals(anotherVersion.getName())) {
      return -1;
    }

    if (MAXIMUM_VERSION.equals(oneVersion.getName())
        || MINIMUM_VERSION.equals(anotherVersion.getName())) {
      return 1;
    }

    Date oneReleaseDate = oneVersion.getReleaseDate();
    Date anotherReleaseDate = anotherVersion.getReleaseDate();

    if (oneReleaseDate == null || anotherReleaseDate == null) {
      return oneVersion.getName().compareTo(anotherVersion.getName());
    }

    return oneReleaseDate.compareTo(anotherReleaseDate);
  }

}
