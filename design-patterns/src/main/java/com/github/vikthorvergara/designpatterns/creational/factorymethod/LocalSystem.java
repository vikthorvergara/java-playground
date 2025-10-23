package com.github.vikthorvergara.designpatterns.creational.factorymethod;

public class LocalSystem implements BlobStore {
    @Override
    public void save(String data) {
        System.out.println("Saving data to Local System: " + data);
    }

    @Override
    public String get(String id) {
        System.out.println("Getting data from Local System with id: " + id);
        return "Data from Local System with id: " + id;
    }
}
