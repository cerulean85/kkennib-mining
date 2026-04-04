package kkennib.net.mining.textmining.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ItemClean {

    @JsonProperty("sentence_index")
    private int sentenceIndex;

    private List<Token> tokens;

    @Getter
    @Setter
    @ToString
    public static class Token {
        private String word;
        private String pos;
    }
}

