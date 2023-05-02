package com.intersystems.dach.ens.sap.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import com.intersystems.dach.utils.ObjectProvider;
import com.intersystems.dach.utils.TraceManager;
import com.intersystems.gateway.GatewayContext;
import com.intersystems.jdbc.IRIS;

// TODO make in objectscript

/**
 * This class imports XSD schemas into the IRIS database and writes them to the
 * file.
 */
public class IRISXSDSchemaImporter {

    private static final SimpleDateFormat directoryTimestampFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
    private Path xsdDirectoryPath = null;
    private Collection<String> knownSchemas = null;
    private boolean structureCreated;
    private ObjectProvider objectProvider;

    public IRISXSDSchemaImporter(String xsdDirectoryPath, ObjectProvider objectProvider) throws IOException {
        this.knownSchemas = new ArrayList<String>();
        this.structureCreated = false;
        this.objectProvider = objectProvider;

        Path baseDirPath = Paths.get(xsdDirectoryPath);
        if (!Files.exists(baseDirPath)) {
            Files.createDirectories(baseDirPath);
            trace("Created base directory: " + baseDirPath.toString());
        } else if (!Files.isWritable(baseDirPath)) {
            throw new IOException("Path is not a writeable: " + baseDirPath);
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String folderNamePrefix = directoryTimestampFormat.format(timestamp);
        String folderNameSuffix = "";
        long index = 1;
        Path xsdPath;

        do {
            xsdPath = Paths.get(baseDirPath.toString(), folderNamePrefix + folderNameSuffix);
            folderNameSuffix = "_" + index;
            index++;
        } while (Files.exists(xsdPath));
        this.xsdDirectoryPath = xsdPath;
    }

    /**
     * @return XsdDirectoryPath
     */
    public Path getXsdDirectoryPath() {
        return xsdDirectoryPath;
    }

    /**
     * Saves to XSD schema as file and imports it to the current IRIS instance.
     * 
     * @param schemaId  The Identifier of the XSD schema
     * @param xsdSchema The XSD schama
     * @return true if the schema was imported, false if the schema already exists.
     */
    public boolean importSchemaIfNotExists(String schemaId, String xsdSchema)
            throws IOException, IllegalStateException, IllegalArgumentException, RuntimeException {

        if (knownSchemas.contains(schemaId)) {
            return false;
        }

        if (xsdSchema == null || xsdSchema.isEmpty()) {
            throw new IllegalArgumentException("Schema is null or empty.");
        }

        if (xsdDirectoryPath == null) {
            throw new IllegalStateException("IRISXSDUtils has not been initialized.");
        }

        // Create directory if it does not exist
        if (!this.structureCreated) {
            Files.createDirectory(xsdDirectoryPath);
            this.structureCreated = true;
            trace("Created directory for XSD schemas: " + xsdDirectoryPath.toString());
        }

        // Write schema to file
        Path xsdFilePath = Paths.get(xsdDirectoryPath.toString(), schemaId + ".xsd");
        File file = xsdFilePath.toFile();
        FileWriter writer = new FileWriter(file);
        writer.write(xsdSchema);
        writer.close();
        trace("Writing XSD schema to file: " + schemaId + ".xsd");

        // Import schema to iris
        IRIS iris = GatewayContext.getIRIS();
        if (iris == null) {
            throw new IllegalStateException("Could not get IRIS instance.");
        }
        iris.classMethodStatusCode("EnsLib.EDI.XML.SchemaXSD", "Import", xsdFilePath.toString());

        trace("Imported XSD schema to IRIS: " + schemaId + ".xsd");

        knownSchemas.add(schemaId);
        return true;
    }

    /**
     * Trace a message.
     * 
     * @param msg The message to trace
     */
    private void trace(String msg) {
        TraceManager.getTraceManager(objectProvider.getTraceManagerHandle()).traceMessage(msg);
    }
}
