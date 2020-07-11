package com.github.olaleyeone.dataupload.data.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Data
@Entity
public class DataUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String contentType;

    private Long size;

    private String description;

    @Column(
            updatable = false,
            nullable = false
    )
    private OffsetDateTime createdOn;

    private OffsetDateTime completedOn;
    private OffsetDateTime completionPublishedOn;

    @Column(updatable = false)
    private String userId;

    @PrePersist
    public void prePersist() {
        if (this.createdOn == null) {
            this.createdOn = OffsetDateTime.now();
        }
    }
}
