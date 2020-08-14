package com.molva.server.controller.management;

import com.molva.server.data.model.ProjectFile;
import com.molva.server.data.model.ProjectPreview;
import com.molva.server.data.service.MediaFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/files")
public class MediaFileController {
  MediaFileService mediaFileService;

  @Autowired
  public MediaFileController(MediaFileService mediaFileService) {
    this.mediaFileService = mediaFileService;
  }

  @GetMapping(path = "/get/preview", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
  public byte[] getProjectPreviewBytesById(
      @RequestHeader("id") Long id
  ) {
    return mediaFileService.loadProjectPreviewBytesById(id);
  }

  @GetMapping(path = "/get/file", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
  public byte[] getProjectFileBytesById(
      @RequestHeader("id") Long id
  ) {
    return mediaFileService.loadProjectFileBytesById(id);
  }
}
