package crest.jira.data.miner.chart.issue;

public class ReleasesToFixForMajor extends ReleasesToFixByPriorityChart {
  
  public ReleasesToFixForMajor() {
    super(3);
  }

  public static void main(String... args) {
    launch(args);
  }
}
