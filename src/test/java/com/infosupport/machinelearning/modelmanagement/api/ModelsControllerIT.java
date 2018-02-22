package com.infosupport.machinelearning.modelmanagement.api;

import com.infosupport.machinelearning.modelmanagement.storage.ModelMetadata;
import com.infosupport.machinelearning.modelmanagement.storage.ModelStorageService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ModelsControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ModelStorageService modelStorageService;

    @Value("${modelmanagement.storage.rootpath}")
    private String modelRepositoryPath;



    @AfterEach
    public void tearDown() throws Exception {
        Paths.get(modelRepositoryPath).toFile().delete();
    }

    @Test
    public void shouldUploadFileSuccesfully() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        HttpEntity entity = new HttpEntity("test-model", headers);

        ResponseEntity<String> response = restTemplate.exchange("/models/test-model", HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.ACCEPTED));
    }

    @Test
    public void shouldDownloadModelsSuccesfully() throws Exception {

        InputStream modelStream = new ByteArrayInputStream("hello-world".getBytes());
        modelStorageService.saveModel("test-model", modelStream);

        HttpEntity<String> request = buildModelDownloadRequestEntity();

        ResponseEntity<byte[]> response = restTemplate.exchange("/models/test-model/1", HttpMethod.GET, request, byte[].class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void downloadModelShouldReturn404ForNonExistingModels() {
        HttpEntity<String> request = buildModelDownloadRequestEntity();
        ResponseEntity<byte[]> response = restTemplate.exchange(
                "/models/test-model/2", HttpMethod.GET, request, byte[].class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void deleteModelShouldReturn204ForExistingModels() throws Exception {
        ModelMetadata metadata = modelStorageService.saveModel("test-model",
                new ByteArrayInputStream("hello-world".getBytes()));

        HttpEntity<String> request = buildModelDownloadRequestEntity();

        ResponseEntity<byte[]> response = restTemplate.exchange(
                String.format("/models/%s/%s", metadata.getName(), metadata.getVersion()),
                HttpMethod.DELETE, request, byte[].class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));
    }

    @Test
    public void deleteModelShouldReturn404ForNonExistingModels() throws Exception {
        HttpEntity<String> request = buildModelDownloadRequestEntity();

        ResponseEntity<byte[]> response = restTemplate.exchange(
                "/models/test-model/3", HttpMethod.DELETE, request, byte[].class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @NotNull
    private HttpEntity<String> buildModelDownloadRequestEntity() {
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));

        return new HttpEntity<>(headers);
    }
}
