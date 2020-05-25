package com.github.olaleyeone.dataupload.data.entity;

import com.olaleyeone.audittrail.api.IgnoreData;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class DataUploadChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private DataUpload dataUpload;

    @Column(nullable = false, updatable = false)
    private Long start;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false, updatable = false)
    private Integer size;

    @IgnoreData
    @Lob
    private byte[] data;

    @Column(
            updatable = false,
            nullable = false
    )
    private LocalDateTime createdOn;

    @PrePersist
    public void prePersist() {
        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }
        this.size = data.length;
    }
}
