package crest.jira.data.miner.report.model;

import crest.jira.data.retriever.model.User;
import crest.jira.data.retriever.model.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ExtendedUser {

  private User user;
  private long releaseParticipation;
  private HashMap<Version, Double> inflationRatios;

  /**
   * This instance produce user metrics through multiple games.
   * 
   * @param reporter
   *          Original User.
   */
  public ExtendedUser(User reporter) {
    this.user = reporter;
    this.inflationRatios = new HashMap<>();
    this.releaseParticipation = 0;
  }

  /**
   * Returns a regression instance for the inflation ratios for the tester.
   * 
   * @return Regression instance.
   */
  public SimpleRegression getRegressionForInflation() {
    SimpleRegression regression = new SimpleRegression();

    List<Version> versions = new ArrayList<>(inflationRatios.keySet());
    Collections.sort(versions, new ReleaseDateComparator());

    for (int index = 0; index < versions.size(); index += 1) {
      regression.addData(index, inflationRatios.get(versions.get(index)));
    }

    return regression;
  }

  public HashMap<Version, Double> getInflationRatios() {
    return inflationRatios;
  }

  public void reportParticipation() {
    this.releaseParticipation += 1;
  }

  public long getReleaseParticipation() {
    return releaseParticipation;
  }

  public User getUser() {
    return user;
  }

  // TODO(cgavidia): We're using the same mechanism as user to preserve some set
  // logic. Verify later if this is necessary.

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.user).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }

    ExtendedUser otherInstance = (ExtendedUser) obj;
    return new EqualsBuilder().append(this.user, otherInstance.user).isEquals();
  }

}
