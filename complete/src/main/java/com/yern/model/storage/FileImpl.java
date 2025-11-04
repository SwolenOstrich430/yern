package com.yern.model.storage;

import java.io.File;
import java.io.Serializable;

import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import com.yern.model.common.AuditTimestamp;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Entity(name="files")
@Table(name="files")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileImpl implements Serializable {

    public static FileImpl from(String rawPath, String originalPath) {
        FileImpl file = new FileImpl();

        file.setRawPath(rawPath);
        file.setOriginalName(originalPath);
        file.setStorageProvider(
            StorageProviderType.defaultOr("")
        );

        return file;
    }

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "storage_provider", columnDefinition = "storage_provider")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private StorageProviderType storageProvider;
    @Column 
    private String rawPath;
    @Column 
    private String formattedPath;
    @Column 
    private String publicUrl;
    @Column 
    private String etag;
    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error", columnDefinition = "jsonb")
    private ErrorLog error;
    @Column 
    private String originalName;
    @Column
    private AuditTimestamp auditTimestamps;

    private static final String RAW_DIR = "raw";
    private static final String FORMATTED_DIR = "formatted";

    public boolean hasError() {
        return (
            getError() instanceof ErrorLog
        );
    }

    public String getBasename() {
        File file = new File(rawPath);
        return file.getName();
    }

    public String getFormattedPath() {
        if (formattedPath == null || formattedPath.isEmpty()) {
            setFormattedPath(getDefaultFormattedFileName());
        }

        return formattedPath;
    }

    public String getDefaultFormattedFileName() {
        assert(rawPath != null);
        assert(!rawPath.isEmpty());

        File rawPathAsFile = new File(rawPath);
        String formattedFileName = rawPathAsFile.getPath().replace(
            "/" + RAW_DIR + "/", 
            "/" + FORMATTED_DIR + "/"
        );
        assert(formattedFileName.contains("/" + FORMATTED_DIR + "/"));
        return formattedFileName;
    }

    public void setEtag(String etag) {
        assert(etag != null && !etag.isEmpty());
        this.etag = etag;
    }

    public void setOriginalName(String originalFileName) {
        File file = new File(originalFileName);
        this.originalName = file.getName();
    }
}
