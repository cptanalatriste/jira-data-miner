package crest.jira.data.miner.report.model;

import crest.jira.data.retriever.model.User;

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
  private HashMap<String, Double> inflationRatios;

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

    List<String> timeFrame = new ArrayList<>(inflationRatios.keySet());
    Collections.sort(timeFrame);

    for (int index = 0; index < timeFrame.size(); index += 1) {
      regression.addData(index, inflationRatios.get(timeFrame.get(index)));
    }

    return regression;
  }

  public HashMap<String, Double> getInflationRatios() {
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
