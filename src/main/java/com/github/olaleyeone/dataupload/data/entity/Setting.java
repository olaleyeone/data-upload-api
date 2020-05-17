package com.github.olaleyeone.dataupload.data.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String value;
}
