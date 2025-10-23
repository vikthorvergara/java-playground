package com.github.vikthorvergara.designpatterns.creational.factorymethod;

public class GCS implements BlobStore {
    @Override
    public void save(String data) {
        System.out.println("Saving data to GCS: " + data);
    }

    @Override
    public String get(String id) {
        System.out.println("Getting data from GCS with id: " + id);
        return "Data from GCS with id: " + id;
    }
}
