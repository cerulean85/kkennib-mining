package kkennib.net.mining.textmining.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PipeTaskPaginationResponse<T> {
  List<T> list = new ArrayList<>();
  private int totalCount;
  private int itemCount;
}
