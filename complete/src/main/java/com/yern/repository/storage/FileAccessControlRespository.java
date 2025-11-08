package com.yern.repository.storage;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yern.model.storage.FileAccessControl;
import com.yern.model.storage.FileImpl;

@Repository
public interface FileAccessControlRespository extends JpaRepository<FileAccessControl, Long> {
    List<FileAccessControl> findByFile(FileImpl file);
    List<FileAccessControl> findByUserIdAndFile(Long userId, FileImpl file);
    Page<FileAccessControl> findByUserId(Long userId, Pageable pageable);
}
