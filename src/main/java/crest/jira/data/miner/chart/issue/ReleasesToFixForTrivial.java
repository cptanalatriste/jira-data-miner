package crest.jira.data.miner.chart.issue;

public class ReleasesToFixForTrivial extends ReleasesToFixByPriorityChart {

  public ReleasesToFixForTrivial() {
    super(5);
  }

  public static void main(String... args) {
    launch(args);
  }
}
