package com.yern.repository.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yern.model.storage.FileImpl;

/**
 * TODO: figure out what specific queries you'll need: 
 *  * processing (unprocessed, errored)
 *  * by original source 
 *  * by a set of users?
 *  TODO: what kind of access it has? 
 *      * one user
 *      * 2+ users 
 *      * or any
 *  TODO: currently patterns are the only thing connecting these to users.
 *      * is that realistic?
 *      * for now, files are only images and images only exist on sections 
 *      
 */
@Repository
public interface FileRepository extends JpaRepository<FileImpl,Long> {
    FileImpl getFileById(Long fildId);
}