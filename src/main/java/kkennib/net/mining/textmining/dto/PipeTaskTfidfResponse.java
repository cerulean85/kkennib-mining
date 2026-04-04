package kkennib.net.mining.textmining.dto;

import kkennib.net.mining.enums.PipeTaskState;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PipeTaskTfidfResponse {
  private Long id;
  private Long pipeLineId = 0L;
  private String currentState = PipeTaskState.PREPARING.getValue();
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime createDate;
  private String s3Url;
  private float fileSize;
  private Long memId = 0L;
  private Long searchTaskId;
  private String searchKeyword;
  private String site = "";
  private String channel = "";
  private LocalDateTime searchStartDate;
  private LocalDateTime searchEndDate;
}
