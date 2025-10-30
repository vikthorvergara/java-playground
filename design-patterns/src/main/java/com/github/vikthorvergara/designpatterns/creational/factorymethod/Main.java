package com.github.vikthorvergara.designpatterns.creational.factorymethod;

public class Main {
  public static void main(String[] args) {
    BlobStore local = new LocalSystem();
    local.save("Local data");
    System.out.println(local.get("local-id-123"));

    BlobStore s3 = new S3();
    s3.save("S3 data");
    System.out.println(s3.get("s3-id-456"));

    BlobStore gcs = new GCS();
    gcs.save("GCS data");
    System.out.println(gcs.get("gcs-id-789"));
  }
}
