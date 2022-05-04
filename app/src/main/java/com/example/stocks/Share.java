package com.example.stocks;

public class Share {
    public final String ticker;
    public Integer sharesNum;
    public Float totalCost;

    public Share(String ticker, Integer sharesNum, Float totalCost) {
        this.ticker = ticker;
        this.sharesNum = sharesNum;
        this.totalCost = totalCost;
    }
}
