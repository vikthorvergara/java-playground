package com.github.vikthorvergara.designpatterns.creational.abstractfactory;

public class GCPFactory implements CloudFactory {
    @Override
    public Storage createStorage() {
        return new GCPStorage();
    }

    @Override
    public Database createDatabase() {
        return new GCPDatabase();
    }
}
