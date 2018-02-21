package com.infosupport.machinelearning.modelmanagement.storage;


import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ModelStorageServiceTests {
    private ModelDataRepository modelDataRepository;
    private ModelMetadataRepository modelMetadataRepository;
    private ModelStorageService modelStorageService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeEach
    public void setUp() throws Exception {
        modelMetadataRepository = Mockito.mock(ModelMetadataRepository.class);
        modelDataRepository = Mockito.mock(ModelDataRepository.class);

        modelStorageService = new ModelStorageServiceImpl(modelMetadataRepository, modelDataRepository);
    }

    @Test
    public void saveModelStoresModelInRepositories() throws Exception{
        modelStorageService.saveModel("test-model", new ByteArrayInputStream("hello-world".getBytes()));

        Mockito.verify(modelMetadataRepository).save(
                Mockito.any(ModelMetadata.class));

        Mockito.verify(modelDataRepository).save(
                Mockito.eq("test-model"),
                Mockito.eq(1),
                Mockito.any(InputStream.class));
    }

    @Test
    public void findModelByNameAndVersionForExistingModelReturnsResult() throws Exception {
        Mockito.when(
                modelMetadataRepository.findByNameAndVersion("test-model", 1)
        ).thenReturn(new ModelMetadata( "test-model",1, new Date()));

        Mockito.when(
                modelDataRepository.findByNameAndVersion("test-model", 1)
        ).thenReturn(Mockito.mock(InputStream.class));

        ModelData metadata = modelStorageService.findModelByNameAndVersion("test-model", 1);

        assertThat(metadata, not(nullValue()));
    }

    @Test
    public void findModelByNameAndVersionForNonExistingModelThrowsException() throws Exception {
        assertThrows(ModelNotFoundException.class, () -> modelStorageService.findModelByNameAndVersion("test-model", 2));
    }

    @Test
    public void deleteModelForExistingModelRemovesModel() throws Exception {
        Mockito.when(modelMetadataRepository.findByNameAndVersion("test-model", 3))
                .thenReturn(new ModelMetadata("test-model", 3, new Date()));

        modelStorageService.deleteModel("test-model", 3);

        Mockito.verify(modelDataRepository).delete("test-model", 3);
        Mockito.verify(modelMetadataRepository).delete(Mockito.any(ModelMetadata.class));
    }
}
