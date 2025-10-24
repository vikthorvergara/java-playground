package com.github.vikthorvergara.designpatterns.creational.abstractfactory;

public class AWSFactory implements CloudFactory {
    @Override
    public Storage createStorage() {
        return new AWSStorage();
    }

    @Override
    public Database createDatabase() {
        return new AWSDatabase();
    }
}
