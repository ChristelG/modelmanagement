package com.infosupport.machinelearning.modelmanagement.api;

import com.infosupport.machinelearning.modelmanagement.storage.ModelData;
import com.infosupport.machinelearning.modelmanagement.storage.ModelMetadata;
import com.infosupport.machinelearning.modelmanagement.storage.ModelNotFoundException;
import com.infosupport.machinelearning.modelmanagement.storage.ModelStorageService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc()
public class ModelsControllerTests {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ModelStorageService modelStorageService;

    @Test
    public void deleteModelForExistingModelReturnsNoContent() throws Exception {
        ModelMetadata testModelMetadata = new ModelMetadata("test-model", 1, new Date());
        InputStream testModelStream = new ByteArrayInputStream("hello-world".getBytes());

        given(modelStorageService.findModelByNameAndVersion("test-model", 1))
                .willReturn(new ModelData(testModelMetadata, testModelStream));

        mvc.perform(delete("/models/test-model/1")).andExpect(status().isNoContent());
    }

    @Test
    public void deleteModelForNonExistingModelReturnsNotFound() throws Exception {
        doThrow(new ModelNotFoundException("test-model", 2))
                .when(modelStorageService).deleteModel("test-model", 2);

        mvc.perform(delete("/models/test-model/2")).andExpect(status().isNotFound());
    }

    @Test
    public void deleteModelWithIOErrorReturnsInternalServerError() throws Exception {
        doThrow(new IOException())
                .when(modelStorageService).deleteModel("test-model", 2);

        mvc.perform(delete("/models/test-model/2")).andExpect(status().isInternalServerError());
    }

    @Test
    public void getModelExistingReturnsModel() throws Exception {
        ModelMetadata testModelMetadata = new ModelMetadata("test-model", 1, new Date());
        InputStream testModelStream = new ByteArrayInputStream("hello-world".getBytes());

        given(modelStorageService.findModelByNameAndVersion("test-model", 1))
                .willReturn(new ModelData(testModelMetadata, testModelStream));

        mvc.perform(get("/models/test-model/1")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
        verify(modelStorageService, times(1)).findModelByNameAndVersion("test-model", 1);
        verifyNoMoreInteractions(modelStorageService);
    }

    @Test
    public void getModelNotExistingReturns404() throws Exception {
        doThrow(new ModelNotFoundException("test-model", 2))
                .when(modelStorageService).findModelByNameAndVersion("test-model", 2);

        mvc.perform(get("/models/test-model/2")).andExpect(status().isNotFound());
        verify(modelStorageService, times(1)).findModelByNameAndVersion("test-model", 2);
        verifyNoMoreInteractions(modelStorageService);
    }

    @Test
    public void postModelReturns202() throws Exception{
        ModelMetadata testModelMetadata = new ModelMetadata("test-model", 1, new Date());
        InputStream testModelStream = new ByteArrayInputStream("hello-world".getBytes());

        given(modelStorageService.saveModel("test-model", testModelStream))
                .willReturn(testModelMetadata);

        mvc.perform(post("models/test-model")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(StreamUtils.copyToByteArray(testModelStream))
                .accept(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isAccepted());
    }
}
