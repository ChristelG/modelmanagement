package com.infosupport.machinelearning.modelmanagement.storage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Paths;


/**
 * Implementation of the model data repository
 */
@Repository
public class ModelDataRepositoryImpl implements ModelDataRepository {
    private final String rootPath;

    /**
     * Initializes a new instance of {@link ModelDataRepositoryImpl}
     *
     * @param rootPath Root path for the repository
     */
    public ModelDataRepositoryImpl(@Value("${modelmanagement.storage.rootpath}") String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * Finds model data by name and version
     *
     * @param name    Name of the model
     * @param version Version of the model
     * @return Returns the model data stream
     */
    @Override
    public InputStream findByNameAndVersion(String name, int version) throws FileNotFoundException {
        ensureRootFolderExists();

        File modelDataFile = Paths.get(rootPath, name, String.valueOf(version), "model.zip").toFile();

        return new FileInputStream(modelDataFile);
    }

    /**
     * Stores model data in the model repository on disk
     *
     * @param name        Name of the model
     * @param version     Version of the model
     * @param modelStream Stream to store on disk
     */
    @Override
    public void save(String name, int version, InputStream modelStream) throws IOException {
        ensureRootFolderExists();

        File modelDirectory = Paths.get(rootPath, name, String.valueOf(version)).toFile();
        File modelFile = Paths.get(rootPath, name, String.valueOf(version), "model.zip").toFile();

        if (!modelDirectory.exists()) {
            modelDirectory.mkdirs();
        }

        try (FileOutputStream outputStream = new FileOutputStream(modelFile)) {
            IOUtils.copy(modelStream, outputStream);
            outputStream.flush();
        }
    }

    /**
     * Deletes an existing model
     *
     * @param name    Name of the model
     * @param version Version of the model
     * @throws IOException Gets thrown when the model folder cannot be deleted
     */
    @Override
    public void delete(String name, int version) throws IOException {
        File modelDirectory = Paths.get(rootPath, name, String.valueOf(version)).toFile();

        if (modelDirectory.exists()) {
            FileUtils.deleteDirectory(modelDirectory);
        }
    }

    /**
     * Ensures that the root folder exists for the repository
     */
    private void ensureRootFolderExists() {
        File rootDirectory = new File(rootPath);

        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs();
        }
    }
}
