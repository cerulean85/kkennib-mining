package kkennib.net.mining.textmining.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PipeTaskDashboardResponse {
  List<ItemDashboardSummary> summaryList = new ArrayList<>();
  List<JoinedPipeTask> joinedTaskList = new ArrayList<>();
}
