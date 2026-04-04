package kkennib.net.mining.textmining.dto;

import kkennib.net.mining.enums.PipeLineStatus;
import lombok.Data;

@Data
public class ItemDashboardSummary {
  private PipeLineStatus status;
  private int preparing;
  private  int pending;
  private int progress;
  private int completed;
  private int total;
}
