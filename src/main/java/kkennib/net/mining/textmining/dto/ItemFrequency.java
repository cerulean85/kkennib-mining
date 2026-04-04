package kkennib.net.mining.textmining.dto;

import lombok.Data;

@Data
public class ItemFrequency {
  private String word;
  private String pos;
  private Long count;
}
