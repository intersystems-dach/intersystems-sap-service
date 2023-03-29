package com.intersystems.dach.ens.sap.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import com.intersystems.gateway.GatewayContext;
import com.intersystems.jdbc.IRIS;

// TODO make in objectscript

public class IRISXSDSchemaImporter {

    private static final SimpleDateFormat directoryTimestampFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
    private String xsdDirectoryBasePath;
    private Path xsdDirectoryPath = null;
    private Collection<String> knownSchemas = null;

    // make this class static
    public IRISXSDSchemaImporter(String xsdDirectoryPath) {
        this.xsdDirectoryBasePath = xsdDirectoryPath;
    }

    /**
     * Creates a directory to store the XSD files.
     * The directory base path specified as a class member.
     * Additonally a directory with the current timestamp as
     * name is created. If this directory already exists a
     * suffix with a consecutive number will be appended.
     * 
     * @return Returns the path of the XSD directory.
     * 
     */
    public String initialize() throws InvalidPathException, IOException {

        Path baseDirPath = Paths.get(xsdDirectoryBasePath);
        if (!Files.exists(baseDirPath)) {
            // TODO only make directory when when xsd gets saved

            Files.createDirectories(baseDirPath);
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

        this.xsdDirectoryPath = Files.createDirectory(xsdPath);
        this.knownSchemas = new ArrayList<String>();

        return this.xsdDirectoryPath.toString();
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
        if (xsdDirectoryPath == null) {
            throw new IllegalStateException("IRISXSDUtils has not been initialized.");
        }

        if (xsdSchema == null || xsdSchema.isEmpty()) {
            throw new IllegalArgumentException("Schema is null or empty.");
        }

        if (knownSchemas.contains(schemaId)) {
            return false;
        }

        // Write schema to file
        Path xsdFilePath = Paths.get(xsdDirectoryPath.toString(), schemaId + ".xsd");
        File file = xsdFilePath.toFile();
        FileWriter writer = new FileWriter(file);
        writer.write(xsdSchema);
        writer.close();

        // Import schema to iris
        IRIS iris = GatewayContext.getIRIS();
        if (iris == null) {
            throw new IllegalStateException("Could not get IRIS instance.");
        }
        iris.classMethodStatusCode("EnsLib.EDI.XML.SchemaXSD", "Import", xsdFilePath.toString());

        knownSchemas.add(schemaId);
        return true;
    }
}
