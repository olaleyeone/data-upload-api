package com.github.olaleyeone.dataupload.service.impl;

import com.github.olaleyeone.dataupload.data.entity.Setting;
import com.github.olaleyeone.dataupload.repository.SettingRepository;
import com.github.olaleyeone.dataupload.service.api.SettingService;
import com.olaleyeone.audittrail.api.Activity;
import com.olaleyeone.audittrail.context.TaskContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
class SettingServiceImpl implements SettingService {

    private final Provider<TaskContext> taskContextProvider;
    private final SettingRepository settingRepository;

    @Activity("FETCH SETTING")
    @Transactional
    @Override
    public String getString(String name, String value) {
        taskContextProvider.get().setDescription(String.format("Fetch setting %s", name));
        return getString(name).orElseGet(() -> {
            initializeSetting(name, value);
            return value;
        });
    }

    @Activity("FETCH SETTING")
    @Transactional
    @Override
    public String getString(String name, Supplier<? extends String> value) {
        taskContextProvider.get().setDescription(String.format("Fetch setting %s", name));
        return getString(name).orElseGet(() -> {
            initializeSetting(name, value.get());
            return value.get();
        });
    }

    @Override
    public Optional<String> getString(String name) {
        Setting setting = settingRepository.findByName(name);
        if (setting == null) {
            return Optional.empty();
        }
        return Optional.of(setting.getValue());
    }

    @Activity("FETCH SETTING")
    @Transactional
    @Override
    public Integer getInteger(String name, int value) {
        taskContextProvider.get().setDescription(String.format("Fetch setting %s", name));
        return getInteger(name).orElseGet(() -> {
            initializeSetting(name, String.valueOf(value));
            return value;
        });
    }

    @Override
    public Optional<Integer> getInteger(String name) {
        Setting setting = settingRepository.findByName(name);
        if (setting == null) {
            return Optional.empty();
        }
        return Optional.of(Integer.valueOf(setting.getValue()));
    }

    @Activity("FETCH SETTING")
    @Transactional
    @Override
    public Long getLong(String name, long value) {
        taskContextProvider.get().setDescription(String.format("Fetch setting %s", name));
        return getLong(name).orElseGet(() -> {
            initializeSetting(name, String.valueOf(value));
            return value;
        });
    }

    @Override
    public Optional<Long> getLong(String name) {
        Setting setting = settingRepository.findByName(name);
        if (setting == null) {
            return Optional.empty();
        }
        return Optional.of(Long.valueOf(setting.getValue()));
    }

    private Setting initializeSetting(String name, String value) {
        taskContextProvider.get().setDescription(String.format("Initialize setting %s", name));
        Setting setting = new Setting();
        setting.setName(name);
        setting.setValue(value);
        settingRepository.save(setting);
        return setting;
    }
}
