package crest.jira.data.miner.report.model;

import crest.jira.data.retriever.model.Version;

import java.util.Comparator;
import java.util.Date;

public class VersionComparator implements Comparator<Version> {

  @Override
  public int compare(Version oneVersion, Version anotherVersion) {
    Date oneReleaseDate = oneVersion.getReleaseDate();
    Date anotherReleaseDate = anotherVersion.getReleaseDate();

    if (oneReleaseDate == null && anotherReleaseDate == null) {
      return 0;
    }

    if (oneReleaseDate == null && anotherReleaseDate != null) {
      return -1;
    }

    if (oneReleaseDate != null && anotherReleaseDate == null) {
      return 1;
    }

    return oneReleaseDate.compareTo(anotherReleaseDate);
  }

}
