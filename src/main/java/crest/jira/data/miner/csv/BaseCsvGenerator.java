package crest.jira.data.miner.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BaseCsvGenerator {

  private static final String NEW_LINE_SEPARATOR = "\n";

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

    if (rows.size() < 1) {
      throw new RuntimeException(
          "No rows sent while trying to create file with prefix " + filePrefix);
    }

    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
    BufferedWriter bufferedWriter = new BufferedWriter(
        new FileWriter(outputFolder + filePrefix + "_" + new Date().getTime() + ".csv"));

    try (CSVPrinter csvPrinter = new CSVPrinter(bufferedWriter, csvFileFormat)) {

      csvPrinter.printRecord(Arrays.copyOf(rows.get(0).getCsvHeader(),
          rows.get(0).getCsvHeader().length, Object[].class));

      for (CsvExportSupport row : rows) {
        csvPrinter.printRecord(row.getCsvRecord());
      }

    } catch (Exception ex) {
      ex.printStackTrace();
    }

  }
}
