package io.j99.idea.vue.network.model;

import com.google.gson.annotations.SerializedName;
import com.intellij.openapi.util.text.StringUtil;

/**
 * Created by apple on 16/5/15.
 */
public class TemplateModel {
    private long id;
    private String name;
    @SerializedName("full_name")
    private String fullName;
    private String url;
    @SerializedName("clone_url")
    private String cloneUrl;
    @SerializedName("git_url")
    private String gitUrl;
    private String description;

    public String getDisplayName() {
        if(StringUtil.isEmpty(name)){
            return "";
        }else{
            StringBuilder sb = new StringBuilder(name);
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            return sb.toString();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

    public void setCloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }
}
