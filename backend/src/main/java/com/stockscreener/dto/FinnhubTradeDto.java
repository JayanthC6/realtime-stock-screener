package com.stockscreener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FinnhubTradeDto {

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private List<TradeData> data;

    @Data
    public static class TradeData {
        @JsonProperty("s")
        private String symbol;

        @JsonProperty("p")
        private Double price;

        @JsonProperty("v")
        private Double volume;

        @JsonProperty("t")
        private Long timestamp;

        @JsonProperty("c")
        private List<String> conditions;
    }
}