package com.molva.server.data.repository;

import com.molva.server.data.model.MediaFile;
import org.springframework.data.repository.CrudRepository;

public interface MediaFileRepository extends CrudRepository<MediaFile, Long> {
}
