package com.example.demo.extension.registry;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 动态接口注册中心（内存）。
 */
@Component
public class DynamicApiRegistry {

    private final AtomicReference<RegistrySnapshot> snapshot = new AtomicReference<>(RegistrySnapshot.EMPTY);

    static String buildKey(String method, String path) {
        return normalizeMethod(method) + ':' + path;
    }

    static String normalizeMethod(String method) {
        return method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
    }

    static boolean isPatternPath(String path) {
        if (path == null) {
            return false;
        }
        return path.contains("{") || path.contains("*") || path.contains("?");
    }

    public DynamicApiMatch match(String method, String path) {
        if (method == null || path == null) {
            return null;
        }
        RegistrySnapshot current = snapshot.get();
        return current.match(normalizeMethod(method), path);
    }

    public void reload(Collection<DynamicApiMeta> metas) {
        snapshot.set(RegistrySnapshot.from(metas));
    }

    public void register(DynamicApiMeta meta) {
        if (meta == null || meta.getApi() == null) {
            return;
        }
        boolean isPattern = isPatternPath(meta.getApi().getPath());
        while (true) {
            RegistrySnapshot current = snapshot.get();
            RegistrySnapshot next = current.with(meta, isPattern);
            if (snapshot.compareAndSet(current, next)) {
                return;
            }
        }
    }

    public void remove(String method, String path) {
        if (method == null || path == null) {
            return;
        }
        while (true) {
            RegistrySnapshot current = snapshot.get();
            RegistrySnapshot next = current.without(method, path);
            if (snapshot.compareAndSet(current, next)) {
                return;
            }
        }
    }
}
