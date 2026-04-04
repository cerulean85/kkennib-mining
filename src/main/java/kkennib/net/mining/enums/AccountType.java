package kkennib.net.mining.enums;

public enum AccountType {
  EMAIL("EMAIL"),
  GOOGLE("GOOGLE"),
  FACEBOOK("FACEBOOK");

  private final String value;

  AccountType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}