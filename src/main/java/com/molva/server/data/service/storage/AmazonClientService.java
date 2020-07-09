package com.molva.server.data.service.storage;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.molva.server.data.service.storage.helpers.FileConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.Objects;

@Service
public class AmazonClientService {

  private AmazonS3 s3client;
  private Logger logger = LoggerFactory.getLogger(AmazonClientService.class);

  @Value("${aws.endpointUrl}")
  private String endpointUrl;
  @Value("${aws.s3bucket.name}")
  private String bucketName;
  @Value("${aws.accessKey}")
  private String accessKey;
  @Value("${aws.secretKey}")
  private String secretKey;

  private String BUCKET_NAME = "nzrv-example-bucket";
  private String PATH_TO_PROFILE_PHOTOS = "/molva-profile-photo/";

  @PostConstruct
  private void initializeAmazon() {
    AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
    this.s3client = AmazonS3ClientBuilder
        .standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withRegion("eu-north-1")
        .build();
  }

  public URL uploadNewProfilePhotoFile(MultipartFile multipartFile) throws IOException {
    File file = FileConverters.convertMultiPartToImageFile(multipartFile);
    String fileName = generateFileName(multipartFile);
    URL url = s3client.getUrl(BUCKET_NAME, PATH_TO_PROFILE_PHOTOS + fileName);
    uploadFileTos3bucket(fileName, file);
    file.delete();
    return url;
  }

  private String generateFileName(MultipartFile multiPart) {
    return new Date().getTime() + "-" + Objects.requireNonNull(multiPart.getOriginalFilename())
        .replace(" ", "_");
  }

  private void uploadFileTos3bucket(String fileName, File file) {
    s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
        .withCannedAcl(CannedAccessControlList.PublicRead));
  }

  public String deleteFileFromS3Bucket(String fileUrl) {
    String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
    return "Successfully deleted";
  }

  public ByteArrayOutputStream downloadFile(String keyName) {
    try {
      S3Object s3object = s3client.getObject(new GetObjectRequest(bucketName, keyName));

      InputStream is = s3object.getObjectContent();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int len;
      byte[] buffer = new byte[4096];
      while ((len = is.read(buffer, 0, buffer.length)) != -1) {
        baos.write(buffer, 0, len);
      }
      return baos;
    } catch (IOException ioe) {
      logger.error("IOException: " + ioe.getMessage());
    } catch (AmazonServiceException ase) {
      logger.info("sCaught an AmazonServiceException from GET requests, rejected reasons:");
      logger.info("Error Message:    " + ase.getMessage());
      logger.info("HTTP Status Code: " + ase.getStatusCode());
      logger.info("AWS Error Code:   " + ase.getErrorCode());
      logger.info("Error Type:       " + ase.getErrorType());
      logger.info("Request ID:       " + ase.getRequestId());
      throw ase;
    } catch (AmazonClientException ace) {
      logger.info("Caught an AmazonClientException: ");
      logger.info("Error Message: " + ace.getMessage());
      throw ace;
    }

    return null;
  }
}
