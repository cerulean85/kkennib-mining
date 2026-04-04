package kkennib.net.mining.textmining.dto;

import lombok.Data;

@Data
public class ItemConcor {
    private String word1;
    private String pos1;
    private Long word1Frequency;
    private String word2;
    private String pos2;
    private Long word2Frequency;
    private Long count;
}

