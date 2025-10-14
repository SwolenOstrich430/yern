package com.yern.repository.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yern.model.storage.FileAccessControl;

@Repository
public interface FileAccessControlRespository extends JpaRepository<FileAccessControl, Long> {
    List<FileAccessControl> findByFileId(Long fileId);
}
