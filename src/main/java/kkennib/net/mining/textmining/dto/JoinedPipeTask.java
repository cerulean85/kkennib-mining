package kkennib.net.mining.textmining.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinedPipeTask {
  // Search Task fields
  private Long searchId;
  private String searchKeyword;
  private String site;
  private String channel;
  private LocalDateTime searchStartDate;
  private LocalDateTime searchEndDate;
  private String searchState;

  // Clean Task fields
  private Long cleanId;
  private String cleanState;
  private boolean extractNoun;
  private boolean extractAdjective;
  private boolean extractVerb;

  // Frequency Task fields
  private Long frequencyId;
  private String frequencyState;
  private String frequencyS3Url;

  // TF-IDF Task fields
  private Long tfidfId;
  private String tfidfState;
  private String tfidfS3Url;

  // Concor Task fields
  private Long concorId;
  private String concorState;
  private String concorS3Url;
}