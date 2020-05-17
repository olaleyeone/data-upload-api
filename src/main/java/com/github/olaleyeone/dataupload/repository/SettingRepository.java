package com.github.olaleyeone.dataupload.repository;

import com.github.olaleyeone.dataupload.data.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettingRepository extends JpaRepository<Setting, Long> {

    Setting findByName(String name);

    List<Setting> findByNameIn(List<String> string);
}
