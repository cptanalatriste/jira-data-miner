package crest.jira.data.miner.report.model;

import crest.jira.data.retriever.model.User;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ExtendedUser {

  private User user;
  private long releaseParticipation;

  public ExtendedUser(User reporter) {
    this.user = reporter;
    this.releaseParticipation = 0;
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
