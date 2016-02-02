package crest.jira.data.miner.csv;

import java.io.IOException;
import java.util.List;

public class BaseCsvGenerator {

  private String outputFolder;

  public BaseCsvGenerator(String outputFolder) {
    super();
    this.outputFolder = outputFolder;
  }

  /**
   * Generates a CSV file.
   * 
   * @param filePrefix
   *          Prefix for the generated file.
   * @param rows
   *          Rows to include in the file.
   * @throws IOException
   *           Problems might arise.
   */
  public void generateCsvFile(String filePrefix, List<? extends CsvExportSupport> rows)
      throws IOException {
    CsvUtils.generateCsvFile(outputFolder, filePrefix, rows);
  }
}
