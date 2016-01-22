package io.j99.idea.vue.cli;

import io.j99.idea.vue.settings.SettingStorage;
import org.jetbrains.annotations.NotNull;
public class VueSettings {
    public String node;
    public String vueExePath;
    public String config;
    public String cwd;

    public static VueSettings build(@NotNull String cwd, @NotNull String node, @NotNull String vue) {
        VueSettings settings = new VueSettings();
        settings.cwd = cwd;
        settings.vueExePath = vue;
        settings.node = node;
        return settings;
    }

    public static VueSettings build(SettingStorage settingStorage) {
        VueSettings s = new VueSettings();
        s.vueExePath = settingStorage.vueExePath;
        s.node = settingStorage.nodeInterpreter;
        return s;
    }

    public static VueSettings build(@NotNull SettingStorage settingStorage, @NotNull String cwd) {
        VueSettings s = build(settingStorage);
        s.cwd = cwd;
        return s;
    }
}
