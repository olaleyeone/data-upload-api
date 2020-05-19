package com.github.olaleyeone.dataupload.data.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    private LocalDateTime createdOn;

    private LocalDateTime completionPublishedOn;

    @Column(updatable = false)
    private String userId;

    @PrePersist
    public void prePersist() {
        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }
    }
}
