package io.j99.idea.vue.settings;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.components.ServiceManager.getService;

@State(name = "Vue",
        storages = @Storage(id = "dir", file = StoragePathMacros.APP_CONFIG + "/vue_for_idea.xml"))
public class SettingStorage implements PersistentStateComponent<SettingStorage> {
    public String builtinRulesPath = "";
    public String vueExePath = "";
    public String npmExePath = "";
    public String nodeExePath = "";
    public boolean treatAllIssuesAsWarnings;


    public static SettingStorage getInstance() {
        return getService(SettingStorage.class);
    }

    @Nullable
    @Override
    public SettingStorage getState() {
        return this;
    }

    @Override
    public void loadState(SettingStorage state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getVersion() {
        return nodeExePath + vueExePath + npmExePath + builtinRulesPath;
    }
}
