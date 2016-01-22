package io.j99.idea.vue.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
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
        //loader template
        FileTemplate template_package = getFile("package.json");
        FileTemplate template_readme = getFile("README.md");
        FileTemplate template_balelrc = getFile(".babelrc");
        FileTemplate template_eslintrc = getFile(".eslintrc");
        FileTemplate template_src_app = getFile("src_App.vue");
        FileTemplate template_src_components_hello = getFile("src_components_Hello.vue");
        FileTemplate template_src_index = getFile("src_index.html");
        FileTemplate template_src_main = getFile("src_main.js");
        FileTemplate template_test_unit_hello_spec = getFile("test_unit_Hello.spec.js");
        FileTemplate template_test_unit_index = getFile("test_unit_index.js");

        //create dir
        VirtualFile srcDir = baseDir.createChildDirectory(this, "src");
        VirtualFile assetsDir = srcDir.createChildDirectory(this, "assets");
        VirtualFile componentDir = srcDir.createChildDirectory(this, "components");
        VirtualFile testDir = baseDir.createChildDirectory(this, "test");
        VirtualFile unitDir = testDir.createChildDirectory(this, "unit");

        VirtualFile pkg = baseDir.createChildData(this, "package.json");
        VirtualFile readme = baseDir.createChildData(this, "README.md");
        VirtualFile balelrc = baseDir.createChildData(this, ".babelrc");
        VirtualFile eslintrc = baseDir.createChildData(this, ".eslintrc");
        VirtualFile src_app = srcDir.createChildData(this, "App.vue");
        VirtualFile components_hello = componentDir.createChildData(this, "Hello.vue");
        VirtualFile index = srcDir.createChildData(this, "index.html");
        VirtualFile main = srcDir.createChildData(this, "main.js");
        VirtualFile unit_hello_spec = unitDir.createChildData(this, "Hello.spec.js");
        VirtualFile unit_index = unitDir.createChildData(this, "index.js");


        try {
            JSONObject json = new JSONObject(template_package.getText());
            json.put("name",module.getName());
            JsonElement pkgJson = new JsonParser().parse(json.toString());
            pkg.setBinaryContent(new GsonBuilder().setPrettyPrinting().create().toJson(pkgJson).getBytes());

            readme.setBinaryContent(template_readme.getText().getBytes());
            balelrc.setBinaryContent(template_balelrc.getText().getBytes());
            eslintrc.setBinaryContent(template_eslintrc.getText().getBytes());
            src_app.setBinaryContent(template_src_app.getText().getBytes());


            components_hello.setBinaryContent(template_src_components_hello.getText().getBytes());
            index.setBinaryContent(template_src_index.getText().getBytes());
            main.setBinaryContent(template_src_main.getText().getBytes());
            unit_hello_spec.setBinaryContent(template_test_unit_hello_spec.getText().getBytes());
            unit_index.setBinaryContent(template_test_unit_index.getText().getBytes());
            return Arrays.asList(balelrc,eslintrc,srcDir,assetsDir,componentDir,testDir,unitDir,pkg,readme,src_app,components_hello,index,main,unit_hello_spec,unit_index);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }
    private FileTemplate getFile(String name){
        return FileTemplateManager.getDefaultInstance().getTemplate("webpack_"+name);
    }
}
