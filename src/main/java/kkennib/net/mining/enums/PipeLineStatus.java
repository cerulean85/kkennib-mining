package kkennib.net.mining.enums;

public enum PipeLineStatus {
  SEARCH("search"),
  CLEAN("clean"),
  FREQUENCY("frequency"),
  TFIDF("tfidf"),
  CONCOR("concor");

  private final String value;

  PipeLineStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
