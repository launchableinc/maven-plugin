package com.launchableinc.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Left over code from earlier version. Kept here for now free floating until other stuff gets filled in.
 */
public class LeftOver {
    String url;

    String resourcePath;

//    public void execute() throws IOException {
//        try {
//            getLog().info("Start Inference");
//            getLog().debug(url);
//            String json = getJson();
//            String[] results = getResult(json);
//            for (String hoge : results) {
//                getLog().debug(hoge);
//            }
//        } catch (IOException e) {
//            throw new MojoExecutionException("Unexpected error", e);
//        }
//    }

    String[] getResult(String json) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.addHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(json));
            try (CloseableHttpResponse httpResponse = httpClient.execute(post)) {
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String result = EntityUtils.toString(httpResponse.getEntity());
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(result, String[].class);
                }
            }
        }
        return null;
    }

    String getJson() throws IOException {
        Path file = Paths.get(resourcePath);
        return Files.readAllLines(file).get(0);
    }
}
