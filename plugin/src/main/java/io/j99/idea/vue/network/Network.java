package io.j99.idea.vue.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.j99.idea.vue.network.model.TemplateModel;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 16/5/15.
 */
public class Network {
    private final CloseableHttpClient httpClient;

    public Network() {
        this.httpClient = HttpClients.createDefault();
    }
    public List<TemplateModel> listTemplate() throws IOException {
        HttpGet get = new HttpGet("https://api.github.com/users/vuejs-templates/repos");
        get.addHeader("User-Agent","vue-plugin-idea");
        get.addHeader("Accept", "application/json");
        CloseableHttpResponse res = execute(get);
        if(res==null){
            return new ArrayList<>();
        }else{
            List<TemplateModel> templateModels = new Gson().fromJson(EntityUtils.toString(res.getEntity()), new TypeToken<ArrayList<TemplateModel>>() {
            }.getType());
            if(templateModels==null){
                return new ArrayList<>();
            }
            return templateModels;
        }
    }
    private void close(CloseableHttpResponse response) {
        try {
            if (response != null) response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CloseableHttpResponse execute(HttpGet httpGet) {
        CloseableHttpResponse response = null;
        try {

            response = httpClient.execute(httpGet);

            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                return response;
            }
            response.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
