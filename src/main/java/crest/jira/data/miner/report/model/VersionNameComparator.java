package crest.jira.data.miner.report.model;

import crest.jira.data.retriever.model.Version;

import java.util.Comparator;

public class VersionNameComparator implements Comparator<Version> {

  @Override
  public int compare(Version oneVersion, Version anotherVersion) {
    if (oneVersion.getName().equals(anotherVersion.getName())) {
      return 0;
    }

    if (ReleaseDateComparator.MINIMUM_VERSION.equals(oneVersion.getName())
        || ReleaseDateComparator.MAXIMUM_VERSION.equals(anotherVersion.getName())) {
      return -1;
    }

    if (ReleaseDateComparator.MAXIMUM_VERSION.equals(oneVersion.getName())
        || ReleaseDateComparator.MINIMUM_VERSION.equals(anotherVersion.getName())) {
      return 1;
    }
    return oneVersion.getName().compareTo(anotherVersion.getName());
  }

}
