package crest.jira.data.miner.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CsvUtils {

  private static final String NEW_LINE_SEPARATOR = "\n";

  /**
   * Returns a list of record instances from a CSV file with Header.
   * 
   * @param fileName
   *          File location.
   * @return List of CSVRecord Instances.
   */
  public static List<CSVRecord> getCsvRecords(String fileName) {
    List<CSVRecord> csvRecords = null;
    CSVParser csvParser = null;
    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();

    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
      csvParser = new CSVParser(bufferedReader, csvFormat);
      csvRecords = csvParser.getRecords();
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return csvRecords;
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
  public static void generateCsvFile(String outputFolder, String filePrefix,
      List<? extends CsvExportSupport> rows) throws IOException {

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
