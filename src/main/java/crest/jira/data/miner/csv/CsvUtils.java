package crest.jira.data.miner.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class CsvUtils {

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
}
