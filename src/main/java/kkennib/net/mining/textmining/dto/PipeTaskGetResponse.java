package kkennib.net.mining.textmining.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class PipeTaskGetResponse<T> {
  private Map<String, Integer> count = new HashMap<>();
  List<T> list = new ArrayList<>();
}
