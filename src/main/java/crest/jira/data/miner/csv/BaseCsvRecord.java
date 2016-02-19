package crest.jira.data.miner.csv;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCsvRecord implements CsvExportSupport {

  private List<Object> csvRecord = new ArrayList<>();
  private List<String> csvHeader = new ArrayList<>();

  public void addDataItem(String key, Object value) {
    csvHeader.add(key);
    csvRecord.add(value);
  }

  public abstract void configureCsvRecord();

  @Override
  public List<Object> getCsvRecord() {
    if (csvHeader.isEmpty()) {
      configureCsvRecord();
    }
    return csvRecord;
  }

  @Override
  public String[] getCsvHeader() {
    if (csvRecord.isEmpty()) {
      configureCsvRecord();
    }

    return csvHeader.toArray(new String[0]);
  }

}
