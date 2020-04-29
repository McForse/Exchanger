package com.shotball.project.models;

import com.google.firebase.database.Exclude;

public class ExchangeModel {
    @Exclude
    public String key;
    public int status;
    public String who;
    public String whom;
    public String what_exchange;
    public String exchange_for;

    public ExchangeModel() {

    }

    public ExchangeModel(int status, String who, String whom, String what_exchange, String exchange_for) {
        this.status = status;
        this.who = who;
        this.whom = whom;
        this.what_exchange = what_exchange;
        this.exchange_for = exchange_for;
    }

    public ExchangeModel(String key, int status, String who, String whom, String what_exchange, String exchange_for) {
        this.key = key;
        this.status = status;
        this.who = who;
        this.whom = whom;
        this.what_exchange = what_exchange;
        this.exchange_for = exchange_for;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
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
