package crest.jira.data.miner.chart.issue;

public class ReleasesToFixForCritical extends ReleasesToFixByPriorityChart {

  public ReleasesToFixForCritical() {
    super(2);
  }

  public static void main(String... args) {
    launch(args);
  }
}
