package com.molva.server.controller.management;

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

  @GetMapping(path = "/get", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
  public byte[] getMediaFileBytesById(
      @RequestHeader("id") Long id
  ) {
    return mediaFileService.loadMediaFileBytesById(id);
  }
}
