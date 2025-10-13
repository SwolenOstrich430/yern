package com.yern.repository.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yern.model.storage.FileAccessControl;

public interface FileAccessControlRespository extends JpaRepository<Long, FileAccessControl> {

}
