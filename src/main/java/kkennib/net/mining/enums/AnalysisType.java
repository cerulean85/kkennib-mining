package kkennib.net.mining.enums;

public enum AnalysisType {
  FREQUENCY("frequency"),
  TFIDF("tfidf"),
  CONCOR("concor");

  private final String value;

  AnalysisType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
