package com.github.vikthorvergara.designpatterns.creational.abstractfactory;

public class GCPDatabase implements Database {
    @Override
    public void query(String sql) {
        System.out.println("Querying GCP Cloud SQL: " + sql);
    }
}
