package com.infosupport.machinelearning.modelmanagement.api;

import com.infosupport.machinelearning.modelmanagement.DocumentedEndpoint;
import com.infosupport.machinelearning.modelmanagement.Messages;
import com.infosupport.machinelearning.modelmanagement.storage.ModelData;
import com.infosupport.machinelearning.modelmanagement.storage.ModelNotFoundException;
import com.infosupport.machinelearning.modelmanagement.storage.ModelStorageService;
import com.infosupport.machinelearning.modelmanagement.storage.validityChecker;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * REST Interface for the models
 */
@RestController
@DocumentedEndpoint
@Api(tags = {"Models"})
public class ModelsController {
    private final ModelStorageService modelStorageService;
    private final Messages messages;

    /**
     * Initializes a new instance of {@link ModelsController}
     *
     * @param modelStorageService Model storage service
     * @param messages            Messages instance
     */
    @Autowired
    public ModelsController(ModelStorageService modelStorageService, Messages messages) {
        this.modelStorageService = modelStorageService;
        this.messages = messages;
    }

    /**
     * Upload a model to the model repository
     *
     * @param name   Name of the model to upload
     * @param entity Model data
     * @return Returns the model metadata for the model
     */
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @RequestMapping(
            value = "models/{name}",
            method = RequestMethod.POST,
            consumes = "application/octet-stream",
            produces = "application/json")
    @ApiOperation(value = "uploadModel")
    @ApiResponses({
            @ApiResponse(code = 202, message = "The model is succesfully uploaded"),
            @ApiResponse(code = 400, message = "Invalid request data provided", response = GenericError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = GenericError.class)
    })
    public ResponseEntity<Object> uploadModel(@PathVariable String name,
                                              @ApiParam(value = "file", required = true) InputStream entity) {
        try {
            validityChecker m = new validityChecker();
            if (m.isValidInput(name, entity)) {
                modelStorageService.saveModel(name, entity);
            }
        } catch (IOException ex) {
            return genericApiError(500, messages.get("errors.internal_error"));
        }
        return ResponseEntity.accepted().build();
    }

    /**
     * Downloads a model from the model repository
     *
     * @param name    Name of the model
     * @param version Version of the model
     * @return Returns the model data
     */
    @RequestMapping(
            value = "models/{name}/{version}",
            consumes = "*/*",
            method = RequestMethod.GET
    )
    @ApiOperation(value = "downloadModel", produces = "application/octet-stream")
    @ApiResponses({
            @ApiResponse(code = 200, message = "The model data"),
            @ApiResponse(code = 404, message = "The model could not be found", response = GenericError.class),
            @ApiResponse(code = 500, message = "Internal server error", response = GenericError.class)
    })
    public ResponseEntity<Object> downloadModel(@PathVariable("name") String name, @PathVariable("version") int version) {
        try {
            ModelData responseData = modelStorageService.findModelByNameAndVersion(name, version);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(new InputStreamResource(responseData.getStream()), headers, HttpStatus.OK);
        } catch (ModelNotFoundException ex) {
            return genericApiError(404, messages.get("errors.model_not_found"));
        }
    }

    @RequestMapping(
            value = "models/{name}/{version}",
            consumes = "*/*",
            method = RequestMethod.DELETE
    )
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @ApiOperation(value = "deleteModel")
    @ApiResponses({
            @ApiResponse(code = 204, message = "The model is deleted"),
            @ApiResponse(code = 404, message = "The model could not be found", response = GenericError.class),
            @ApiResponse(code = 500, message = "Internal server error", response = GenericError.class)
    })
    public ResponseEntity<Object> deleteModel(@PathVariable("name") String name, @PathVariable("version") int version) {
        try {
            modelStorageService.deleteModel(name, version);
            return ResponseEntity.noContent().build();
        } catch (ModelNotFoundException ex) {
            return genericApiError(404, messages.get("errors.model_not_found"));
        } catch (IOException ex) {
            return genericApiError(500, messages.get("errors.internal_error"));
        }
    }

    /**
     * Generates a generic API error response
     *
     * @param statusCode   Status code for the message
     * @param errorMessage Error message to display
     * @return Returns the response entity for the error
     */
    private ResponseEntity<Object> genericApiError(int statusCode, String errorMessage) {
        return ResponseEntity
                .status(statusCode)
                .body(new GenericError(errorMessage));
    }
}

