package crest.jira.data.miner.report.model;

import java.util.List;

public interface CsvExportSupport {

  /**
   * Returns a representation of the collected metrics.
   * 
   * @return String representation of all the metric.
   */
  public List<Object> getCsvRecord();

  /**
   * The header for a CVS Report.
   * 
   * @return Header as String.
   */
  public String[] getCsvHeader();

}
