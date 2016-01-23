package io.j99.idea.vue.module;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by apple on 16/1/22.
 */
public class WebpackTemplate extends VueProjectTemplate{
    public WebpackTemplate(String name, String description) {
        super(name, description);
    }
    @Override
    public Collection<VirtualFile> generateProject(@NotNull VueProjectWizardData data, @NotNull Module module, @NotNull VirtualFile baseDir) throws IOException {
        VirtualFile packagejson = baseDir.findChild("package.json");
        if(packagejson!=null){
            JsonParser jsonParser = new JsonParser();
            JsonElement je= jsonParser.parse(new FileReader(packagejson.getName()));
            try {
                JSONObject json = new JSONObject(je.toString());
                json.put("name",module.getName());
                json.put("private",false);
                je= jsonParser.parse(json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            packagejson.setBinaryContent(new GsonBuilder().setPrettyPrinting().create().toJson(je).getBytes());;
        }
        return Collections.EMPTY_LIST;
    }
}
