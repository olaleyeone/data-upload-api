package com.github.olaleyeone.dataupload.service.api;

import java.util.Optional;
import java.util.function.Supplier;

public interface SettingService {

    String getString(String name, String value);

    String getString(String name, Supplier<? extends String> value);

    Optional<String> getString(String name);

    Integer getInteger(String name, int value);

    Optional<Integer> getInteger(String name);

    Long getLong(String name, long value);

    Optional<Long> getLong(String name);

}
