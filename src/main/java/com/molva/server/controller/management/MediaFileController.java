package com.molva.server.controller.management;

import com.molva.server.data.service.MediaFileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/files")
public class MediaFileController {
  MediaFileService mediaFileService;

  public MediaFileController(MediaFileService mediaFileService) {
    this.mediaFileService = mediaFileService;
  }

  @GetMapping(path = "/get", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
  public byte[] getMediaFileByteById(
      @RequestHeader("id") Long id
  ) {
    return mediaFileService.loadMediaFileBytesById(id);
  }
}
