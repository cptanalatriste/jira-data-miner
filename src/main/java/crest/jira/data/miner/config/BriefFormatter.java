package crest.jira.data.miner.config;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class BriefFormatter extends Formatter {
  public BriefFormatter() {
    super();
  }

  @Override
  public String format(final LogRecord record) {
    return record.getMessage() + "\n";
  }
}