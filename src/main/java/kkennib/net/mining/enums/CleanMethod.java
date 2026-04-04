package kkennib.net.mining.enums;

public enum CleanMethod {
  EXTRACT_NOUN("noun-extraction"),
  EXTRACT_ADJECTIVE("adjective-extraction"),
  EXTRACT_VERB("verb-extraction");

  private final String value;

  CleanMethod(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
