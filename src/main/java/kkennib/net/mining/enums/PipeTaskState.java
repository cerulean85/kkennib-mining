package kkennib.net.mining.enums;

public enum PipeTaskState {
  PREPARING("preparing"),
  PENDING("pending"),
  IN_PROGRESS("progress"),
  COMPLETED("completed");
  private final String value;

  PipeTaskState(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
