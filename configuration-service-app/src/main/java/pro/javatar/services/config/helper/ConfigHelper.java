package pro.javatar.services.config.helper;

import pro.javatar.services.config.rest.exception.ConfigParseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.Patch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Service
public class ConfigHelper {

    private static final Logger logger = LoggerFactory.getLogger(ConfigHelper.class);

    public static final String REFRESH = "refresh";

    @Value("${spring.cloud.config.server.git.username: }")
    public String gitUsername;

    @Value("${spring.cloud.config.server.git.password: }")
    public String gitPassword;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EurekaClient discoveryClient;

    public Set<String> retrieveModifiedServices(String payload) {
        Set<String> modifiedServices = new HashSet<String>();
        String diffRest = getDiffRest(payload);
        String commitDetails = getCommitDatails(diffRest);
        try {
            modifiedServices.addAll(parseCommitDetails(commitDetails));
        } catch (IOException e) {
            logger.error("Fail to parse commit details", e);
        }
        return modifiedServices;
    }

    private String getDiffRest(String payload){
        try {
            JsonObject json = new JsonParser().parse(payload).getAsJsonObject();
            JsonArray jsonArray = json.get("push").getAsJsonObject().get("changes").getAsJsonArray();
            return jsonArray.get(0).getAsJsonObject().get("links").getAsJsonObject().get("diff").getAsJsonObject()
                    .get("href").getAsString();
        } catch (Exception e) {
            logger.error("Fail to parse webhook payload: {}", payload, e);
            throw new ConfigParseException();
        }
    }

    private Set<String> parseCommitDetails(String commitDetails) throws IOException {
        Set<String> modifiedFiles = new HashSet<String>();
        Patch patch = new Patch();
        patch.parse(IOUtils.toInputStream(commitDetails, StandardCharsets.UTF_8));
        for (FileHeader fileHeader : patch.getFiles()) {
            modifiedFiles.add(FilenameUtils.removeExtension(fileHeader.getNewPath()));
        }
        return modifiedFiles;
    }

    private String getCommitDatails(String url){
        HttpEntity<String> request = new HttpEntity<String>(createHeaders(gitUsername, gitPassword));
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return response.getBody();
    }

    private HttpHeaders createHeaders(final String username, final String password ){
        return new HttpHeaders(){
            {
                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.encodeBase64(
                        auth.getBytes(Charset.forName("US-ASCII")) );
                String authHeader = "Basic " + new String( encodedAuth );
                set("Authorization", authHeader);
            }
        };
    }

    public void refreshService(String serviceName) {
        Application application = discoveryClient.getApplication(serviceName);
        if (application == null) {
            logger.warn("Service could not be found: {}", serviceName);
            return;
        }
        for (InstanceInfo instance : application.getInstances()) {
            try {
                String url = instance.getHomePageUrl() + REFRESH;
                restTemplate.postForEntity(url, new HttpEntity<String>(""), String.class);
                logger.info("Refresh request has been sent to {}", serviceName);
            } catch (Exception e) {
                logger.error("Fail to refresh {} service instance {}", serviceName, instance.getHomePageUrl(), e);
            }
        }
    }
}
