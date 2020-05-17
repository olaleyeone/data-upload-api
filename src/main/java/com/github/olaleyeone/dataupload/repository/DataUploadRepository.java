package com.github.olaleyeone.dataupload.repository;

import com.github.olaleyeone.dataupload.data.entity.DataUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataUploadRepository extends JpaRepository<DataUpload, Long> {
}
