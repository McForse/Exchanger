package com.shotball.project.models;

public class ExchangeModel {
    public int status;
    public String whom;
    public String what_exchange;
    public String exchange_for;

    public ExchangeModel() {

    }

    public ExchangeModel(int status, String whom, String what_exchange, String exchange_for) {
        this.status = status;
        this.whom = whom;
        this.what_exchange = what_exchange;
        this.exchange_for = exchange_for;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getWhom() {
        return whom;
    }

    public void setWhom(String whom) {
        this.whom = whom;
    }

    public String getWhat_exchange() {
        return what_exchange;
    }

    public void setWhat_exchange(String what_exchange) {
        this.what_exchange = what_exchange;
    }

    public String getExchange_for() {
        return exchange_for;
    }

    public void setExchange_for(String exchange_for) {
        this.exchange_for = exchange_for;
    }
}
