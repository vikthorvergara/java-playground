package com.github.vikthorvergara.designpatterns.creational.factorymethod;

public interface BlobStore {
    void save(String data);
    String get(String id);
}
