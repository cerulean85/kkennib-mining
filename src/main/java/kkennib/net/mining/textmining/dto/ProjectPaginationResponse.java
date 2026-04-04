package kkennib.net.mining.textmining.dto;

import kkennib.net.mining.project.entity.Project;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ProjectPaginationResponse {
  List<Project> list = new ArrayList<>();
  private int totalCount;
  private int itemCount;
}
