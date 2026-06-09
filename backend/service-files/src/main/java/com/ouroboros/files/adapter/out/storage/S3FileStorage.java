package com.ouroboros.files.adapter.out.storage;

import com.ouroboros.files.application.FileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/** Armazenamento de blobs em S3 (compativel com LocalStack via endpoint configuravel). */
@Component
public class S3FileStorage implements FileStorage {

  private final S3Client s3;
  private final String bucket;

  public S3FileStorage(S3Client s3, @Value("${ouroboros.s3.bucket}") String bucket) {
    this.s3 = s3;
    this.bucket = bucket;
  }

  @Override
  public void put(String key, byte[] content, String contentType) {
    PutObjectRequest.Builder request = PutObjectRequest.builder().bucket(bucket).key(key);
    if (contentType != null) {
      request.contentType(contentType);
    }
    s3.putObject(request.build(), RequestBody.fromBytes(content));
  }

  @Override
  public byte[] get(String key) {
    return s3.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build())
        .asByteArray();
  }

  @Override
  public void delete(String key) {
    s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
  }
}
