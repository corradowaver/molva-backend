package com.molva.server.helpers;

import com.molva.server.data.model.MediaFile;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Configuration
public class MediaFileFactory {
  public MediaFile createMediaFile() {
    MediaFile mediaFile = new MediaFile(new Date(), new Date(), "thisismd5stringlolactuallyno", "image/png", 42069);
    mediaFile.setId(1L);
    return mediaFile;
  }

  public MultipartFile createMultipartFile() {
    return new MultipartFile() {
      @Override
      public String getName() {
        return "belle.jpg";
      }

      @Override
      public String getOriginalFilename() {
        return "belle.jpg";
      }

      @Override
      public String getContentType() {
        return "image/png";
      }

      @Override
      public boolean isEmpty() {
        return false;
      }

      @Override
      public long getSize() {
        return 42069;
      }

      @Override
      public byte[] getBytes() {
        return new byte[0];
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return null;
      }

      @Override
      public void transferTo(File dest) throws IOException, IllegalStateException {

      }
    };
  }

  public MultipartFile createInvalidMultipartFile() {
    return new MultipartFile() {
      @Override
      public String getName() {
        return "belle";
      }

      @Override
      public String getOriginalFilename() {
        return "belle";
      }

      @Override
      public String getContentType() {
        return "image/png";
      }

      @Override
      public boolean isEmpty() {
        return false;
      }

      @Override
      public long getSize() {
        return 42069;
      }

      @Override
      public byte[] getBytes() throws IOException {
        return new byte[0];
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return null;
      }

      @Override
      public void transferTo(File dest) throws IOException, IllegalStateException {

      }
    };
  }


  public MultipartFile createLargeMultipartFile() {
    return new MultipartFile() {
      @Override
      public String getName() {
        return "belle.jpg";
      }

      @Override
      public String getOriginalFilename() {
        return "belle.jpg";
      }

      @Override
      public String getContentType() {
        return "image/png";
      }

      @Override
      public boolean isEmpty() {
        return false;
      }

      @Override
      public long getSize() {
        return 42069420613131313L;
      }

      @Override
      public byte[] getBytes() throws IOException {
        return new byte[0];
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return null;
      }

      @Override
      public void transferTo(File dest) throws IOException, IllegalStateException {

      }
    };
  }

}
