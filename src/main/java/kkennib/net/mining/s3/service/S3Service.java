package kkennib.net.mining.s3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket-name}")
  private String bucketName;


  public String uploadFile(String key, MultipartFile file) throws IOException {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(file.getContentType())
            .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

    return s3Client.utilities().getUrl(GetUrlRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()).toString();
  }

  public byte[] downloadFile(String key) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

    ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
    return objectBytes.asByteArray();
  }

}
