package top1hub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import top1hub.message.ResourceException;
import top1hub.message.Result;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 源速的资源管理类
 */
public class ResourceManager {

    private final CloseableHttpClient httpClient;
    private String token;
    private String container;
    private String safeCode;

    private ResourceManager() {
        this.httpClient = HttpClients.createMinimal();
    }

    public static ResourceManager create() {
        return new ResourceManager();
    }

    public ResourceManager container(String container) {
        this.container = container;
        return this;
    }

    public ResourceManager safeCode(String safeCode) {
        this.safeCode = safeCode;
        return this;
    }

    public ResourceManager build() {
        try {
            HttpGet get = new HttpGet(String.format("http://api.top1cloud.com/getuploadtoken?SafeCode=%s", this.safeCode));
            CloseableHttpResponse response = this.httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() >= 300) {
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
            if (response.getEntity() == null) {
                throw new ClientProtocolException("Response contains no content");
            }
            Gson gson = new GsonBuilder().create();
            ContentType contentType = ContentType.get(response.getEntity());
            Reader reader = new InputStreamReader(response.getEntity().getContent(), contentType.getCharset());
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            this.token = jsonObject.get("message").getAsString();
            return this;
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }

    public Result upload(Path path) {
        return upload(path.getFileName().toString(), path, false);
    }

    public Result upload(String fileKey, Path path) {
        return upload(fileKey, path, false);
    }

    public Result upload(String fileKey, String filePath) {
        return upload(fileKey, Paths.get(filePath), false);
    }

    public Result upload(String fileKey, String filePath, boolean isCover) {
        return upload(fileKey, Paths.get(filePath), isCover);
    }

    public Result upload(String fileKey, Path path, boolean isCover) {
        try {
            String hash = DigestUtils.sha1Hex(Files.newInputStream(path));
            HttpPost post = new HttpPost("http://upload.top1cloud.com/upload");
            ContentType contentType = ContentType.APPLICATION_OCTET_STREAM;
            String fileType = URLConnection.getFileNameMap().getContentTypeFor(path.getFileName().toString());
            if (fileType != null) {
                contentType = ContentType.create(fileType);
            }
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addPart("token", new StringBody(this.token, ContentType.TEXT_PLAIN.withCharset(Consts.UTF_8)))
                    .addPart("container", new StringBody(this.container, ContentType.TEXT_PLAIN.withCharset(Consts.UTF_8)))
                    .addPart("fileKey", new StringBody(fileKey, ContentType.TEXT_PLAIN.withCharset(Consts.UTF_8)))
                    .addPart("isCover", new StringBody(String.valueOf(isCover), ContentType.TEXT_PLAIN.withCharset(Consts.UTF_8)))
                    .addPart("dataHash", new StringBody(hash, ContentType.TEXT_PLAIN.withCharset(Consts.UTF_8)))
                    .addPart("file", new FileBody(new File(path.toAbsolutePath().toString()), contentType))
                    .setContentType(ContentType.MULTIPART_FORM_DATA)
                    .build();
            post.setEntity(entity);
            CloseableHttpResponse response = this.httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() >= 300) {
                return new Result(false, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
            if (response.getEntity() == null) {
                return new Result(false, response.getStatusLine().getStatusCode(), "Response contains no content");
            }
            Gson gson = new GsonBuilder().create();
            contentType = ContentType.get(response.getEntity());

            Reader reader = new InputStreamReader(response.getEntity().getContent(), contentType.getCharset());
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            String errorMessage = jsonObject.get("errorMessage").getAsString();
            return new Result(errorMessage.equals("success"), response.getStatusLine().getStatusCode(), errorMessage);
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }

    public Result delete(String fileKey) {
        try {
            URI url = new URIBuilder()
                    .setScheme("http")
                    .setHost("api.top1cloud.com")
                    .setPath("/deletefile")
                    .setParameter("token", this.token)
                    .setParameter("container", this.container)
                    .setParameter("fileKey", fileKey)
                    .build();
            HttpGet get = new HttpGet(url);
            CloseableHttpResponse response = this.httpClient.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 300) {
                return new Result(false, statusCode, response.getStatusLine().getReasonPhrase());
            }
            if (response.getEntity() == null) {
                return new Result(false, statusCode, "Response contains no content");
            }
            Gson gson = new GsonBuilder().create();
            ContentType contentType = ContentType.get(response.getEntity());
            Reader reader = new InputStreamReader(response.getEntity().getContent(), contentType.getCharset());
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            String message = jsonObject.get("message").getAsString();
            if (message == null) {
                return new Result(false, statusCode, "Wrong response reason");
            }
            return new Result(message.equals("Success"), statusCode, message);
        } catch (Exception e) {
            throw new ResourceException(e);
        }
    }

    public void destory() {
        try {
            this.httpClient.close();
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

    public boolean authorized() {
        return this.token != null;
    }
}
