package com.intersystems.dach.ens.sap;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.intersystems.dach.ens.common.annotations.ClassMetadata;
import com.intersystems.dach.ens.common.annotations.FieldMetadata;
import com.intersystems.dach.ens.sap.testing.SAPServiceTestCase;
import com.intersystems.dach.ens.sap.testing.SAPServiceTestRunner;
import com.intersystems.dach.ens.sap.testing.TestCases;
import com.intersystems.dach.ens.sap.utils.IRISXSDSchemaImporter;
import com.intersystems.dach.sap.SAPServer;
import com.intersystems.dach.sap.SAPServerImportData;
import com.intersystems.dach.sap.annotations.SAPJCoPropertyAnnotation;
import com.intersystems.dach.sap.handlers.SAPServerErrorHandler;
import com.intersystems.dach.sap.handlers.SAPServerExceptionHandler;
import com.intersystems.dach.sap.handlers.SAPServerImportDataHandler;
//import com.intersystems.enslib.pex.ClassMetadata; //intersystems-util-3.3.0 or newer
//import com.intersystems.enslib.pex.FieldMetadata; //intersystems-util-3.3.0 or newer
import com.intersystems.gateway.GatewayContext;
import com.intersystems.jdbc.IRISObject;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.ServerDataProvider;

/**
 * A Service to receive messages from a SAP system.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 */
@ClassMetadata(Description = "A InterSystems Business Service to receive messages from a SAP system.", InfoURL = "https://github.com/phil1436/intersystems-sap-service")
public class SAPService extends com.intersystems.enslib.pex.BusinessService
        implements SAPServerImportDataHandler, SAPServerErrorHandler, SAPServerExceptionHandler {

    /**
     * *****************************
     * *** Service configuration ***
     * *****************************
     */

    // SAP Service
    @FieldMetadata(Category = "SAP Service Settings", IsRequired = true, Description = "REQUIRED<br>Set the buisness partner in the production. The outgoing messages will be directed here.")
    public String BusinessPartner = "";

    @FieldMetadata(Category = "SAP Service Settings", Description = "If enabled the service will return a JSON object instead of a XML object")
    public boolean UseJSON = false;

    @FieldMetadata(Category = "SAP Service Settings", Description = "If enabled new XML schemas will be saved and imported to the production automatically.")
    public boolean ImportXMLSchemas = false;

    @FieldMetadata(Category = "SAP Service Settings", Description = "If import XML schemas is enabled the XSD files are stored here. This folder must be accessible by the IRIS instance and the JAVA language server.")
    public String XMLSchemaPath = "";

    @FieldMetadata(Category = "SAP Service Settings", IsRequired = true, Description = "REQUIRED<br>This is the maximum time the SAP function handler waits till the processing of the input data has been confirmed. If the confirmation takes longer an exception is thrown. The value should be at least twice (better three times) the Inbound Adapter Call Interval.")
    public Integer ConfirmationTimeoutSec = 10;

    @FieldMetadata(Category = "SAP Service Settings", Description = "Send test messages for debugging and testing purposes.")
    public boolean EnableTesting = false;

    // Server Connection
    @SAPJCoPropertyAnnotation(jCoName = ServerDataProvider.JCO_GWHOST)
    @FieldMetadata(Category = "SAP Server Connection", IsRequired = true, Description = "REQUIRED<br>Set the gateway host address. The gateway host address is used to connect to the SAP system.")
    public String GatewayHost = "";

    @SAPJCoPropertyAnnotation(jCoName = ServerDataProvider.JCO_GWSERV)
    @FieldMetadata(Category = "SAP Server Connection", IsRequired = true, Description = "REQUIRED<br>Set the gateway service. The gateway service is used to connect to the SAP system. Usually 'sapgwNN' whereas NN is the instance number.")
    public String GatewayService = "";

    @SAPJCoPropertyAnnotation(jCoName = ServerDataProvider.JCO_PROGID)
    @FieldMetadata(Category = "SAP Server Connection", IsRequired = true, Description = "REQUIRED<br>Set the programm ID. The programm ID is used to identify the service in the SAP system.")
    public String ProgrammID = "";

    @SAPJCoPropertyAnnotation(jCoName = ServerDataProvider.JCO_CONNECTION_COUNT)
    @FieldMetadata(Category = "SAP Server Connection", IsRequired = true, Description = "REQUIRED<br>Set the connection count. The connection count is used to connect to the SAP system.")
    public int ConnectionCount = 1;

    @SAPJCoPropertyAnnotation(jCoName = ServerDataProvider.JCO_REP_DEST)
    @FieldMetadata(Category = "SAP Server Connection", Description = "Set the repository destination. The repository destination is used to connect to the SAP system. Usually 'SAP' or 'SAP_TEST")
    public String Repository = "";

    // Client Connection
    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_ASHOST)
    @FieldMetadata(Category = "SAP Client Connection", IsRequired = true, Description = "REQUIRED<br>Set the host address. The host address is used to connect to the SAP system.")
    public String HostAddress = "";

    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_CLIENT)
    @FieldMetadata(Category = "SAP Client Connection", IsRequired = true, Description = "REQUIRED<br>Set the client ID. The client ID is used to connect to the SAP system.")
    public String ClientID = "";

    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_SYSNR)
    @FieldMetadata(Category = "SAP Client Connection", IsRequired = true, Description = "REQUIRED<br>Set the system number. The system number is used to connect to the SAP system.")
    public String SystemNumber = "";

    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_USER)
    @FieldMetadata(Category = "SAP Client Connection", IsRequired = true, Description = "REQUIRED<br>Set the username. The username is used to connect to the SAP system.")
    public String Username = "";

    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_PASSWD)
    @FieldMetadata(Category = "SAP Client Connection", IsRequired = true, Description = "REQUIRED<br>Set the password. The password is used to connect to the SAP system.")
    public String Password = "";

    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_LANG)
    @FieldMetadata(Category = "SAP Client Connection", IsRequired = true, Description = "REQUIRED<br>Set the language. The language is used to connect to the SAP system.")
    public String SAPLanguage = "";

    /**
     * ******************
     * *** Members ***
     * ******************
     */

    private SAPServer sapServer;

    private Queue<SAPServerImportData> importDataQueue;
    private Queue<Error> errorBuffer;
    private Queue<Exception> exceptionBuffer;

    private boolean warningActiveFlag = false;

    private IRISXSDSchemaImporter irisSchemaImporter;

    @Override
    public void OnInit() throws Exception {
        // Prepare buffers
        importDataQueue = new ConcurrentLinkedQueue<SAPServerImportData>();
        errorBuffer = new ConcurrentLinkedQueue<Error>();
        exceptionBuffer = new ConcurrentLinkedQueue<Exception>();

        // Prepare Schema Import
        if (!UseJSON && ImportXMLSchemas) {
            LOGINFO("XML Schemas import is enabled.");
            irisSchemaImporter = new IRISXSDSchemaImporter(this.XMLSchemaPath);
            String xsdDir = irisSchemaImporter.initialize();
            LOGINFO("Created XML schema folder: " + xsdDir);
        }

        // Prepare SAP JCo server
        try {
            Properties settings = this.generateSettingsProperties();
            sapServer = new SAPServer(settings, this, this.UseJSON);
            sapServer.registerErrorHandler(this);
            sapServer.registerExceptionHandler(this);
            sapServer.setConfirmationTimeoutMs(ConfirmationTimeoutSec * 1000);
            sapServer.start();
            LOGINFO("Started SAP Service.");
        } catch (Exception e) {
            LOGERROR("SAPService could not be started: " + e.getMessage());
            throw new RuntimeException();
        }

        if (this.EnableTesting) {
            LOGWARNING("Testing is enabled.");
            testing();
        }

    }

    private void testing() {
        SAPServiceTestRunner testRunner = new SAPServiceTestRunner();
        Collection<SAPServiceTestCase> testCases;
        if (this.UseJSON) {
            testCases = TestCases.getJSONTestCases();
            LOGINFO("Running " + testCases.size() + " JSON test cases.");
        } else {
            testCases = TestCases.getXMLTestCases();
            LOGINFO("Running " + testCases.size() + " XML test cases.");
        }
        testRunner.addTestCases(testCases);
        testRunner.runTestsAsync(this);
    }

    private boolean processImportData() {
        if (importDataQueue.isEmpty()) {
            // No data in queue, wait for next call intervall
            _WaitForNextCallInterval = true;
            // Reset warning flag
            warningActiveFlag = false;
            return true;
        }
        
        if (!warningActiveFlag && importDataQueue.size() > 100) {
            LOGWARNING("High load. Current messages in Queue: " + importDataQueue.size());
            warningActiveFlag = true;
        }

        _WaitForNextCallInterval = false;
        boolean result = true;
        SAPServerImportData importData = importDataQueue.poll();
        IRISObject request;       
        if (UseJSON) {
            request = (IRISObject) GatewayContext.getIRIS().classMethodObject(
                    "Ens.StringRequest",
                    "%New",
                    importData.getData());
        } else {
            request = (IRISObject) GatewayContext.getIRIS().classMethodObject(
                    "EnsLib.EDI.XML.Document",
                    "%New",
                    importData.getData());
            request.set("DocType", importData.getFunctionName());

            if (ImportXMLSchemas && irisSchemaImporter != null) {
                try {
                    boolean importResult = irisSchemaImporter.importSchemaIfNotExists(
                            importData.getFunctionName(),
                            importData.getSchema());
                    if (importResult) {
                        LOGINFO("Imported new XML schema: " + importData.getFunctionName());
                    }
                } catch (Exception e) {
                    LOGERROR("Error while importing XML schema for function '" + 
                        importData.getFunctionName() + "': " + e.getMessage());
                }
            }
        }

        try {
            this.SendRequestAsync(this.BusinessPartner, request);
            synchronized(importData){
                importData.notifyAll();
            }
        } catch (IllegalMonitorStateException e) {
            LOGWARNING("Confirmation of import data processing failed.");
        } catch (Exception e) {
            LOGERROR("Error while sending request: " + e.getMessage());
            result = false;
        }

        /* Wait for next call intervall if queue is empty or call
           OnProcessInput immediately again. */
        _WaitForNextCallInterval = importDataQueue.isEmpty();
        return result;
    }

    @Override
    public Object OnProcessInput(Object msg) throws Exception {        
        boolean result = this.processImportData();

        // Handle errors and exceptions
        boolean errorOrExceptionOccured = false;
        while (exceptionBuffer.size() > 0) {
            Exception e = exceptionBuffer.poll();
            LOGERROR("An exception occured in SAP server: " + e.getMessage());
            errorOrExceptionOccured = true;
        }

        while (errorBuffer.size() > 0) {
            Error err = errorBuffer.poll();
            LOGERROR("An error occured in SAP server: " + err.getMessage());
            errorOrExceptionOccured = true;
        }

        if (errorOrExceptionOccured) {
            throw new RuntimeException();
        }

        return result;

    }

    @Override
    public void OnTearDown() throws Exception {
        try {
            sapServer.stop();
            LOGINFO("SAPService stopped");
        } catch (Exception e) {
            LOGERROR("An exception occured during stop of the server: " + e.getMessage());
        }

        // Close iris connection
        GatewayContext.getIRIS().close();

        // Reset iris XML schema importer
        this.irisSchemaImporter = null;
    }

    @Override
    public void onImportDataReceived(SAPServerImportData data) {
        importDataQueue.add(data);
    }

    @Override
    public void onErrorOccured(Error err) {
        this.errorBuffer.add(err);
    }

    @Override
    public void onExceptionOccured(Exception e) {
        this.exceptionBuffer.add(e);
    }

    // This is a workaround to handle a bug in IRIS < 2022.1
    public void dispatchOnInit(com.intersystems.jdbc.IRISObject hostObject) throws java.lang.Exception {
        _dispatchOnInit(hostObject);
    }

    // Helper method to generate SAP settings properties by using field annotations.
    private Properties generateSettingsProperties() throws Exception {
        Properties properties = new Properties();

        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SAPJCoPropertyAnnotation.class) &&
                    field.isAnnotationPresent(FieldMetadata.class)) {
                SAPJCoPropertyAnnotation jcoProperty = field.getAnnotation(SAPJCoPropertyAnnotation.class);
                FieldMetadata fieldMetadata = field.getAnnotation(FieldMetadata.class);
                try {
                    String value = field.get(this).toString();
                    if (fieldMetadata.IsRequired() && value.isEmpty()) {
                        throw new Exception("Required field is empty.");
                    }
                    String key = jcoProperty.jCoName();
                    properties.setProperty(key, value);
                } catch (Exception e) {
                    throw new Exception("Field " + field.getName() + ": " + e.getMessage());
                }
            }
        }
        return properties;
    }

    
    /**
     * @return The name of the inbound adapter to be used with this class.
     */
    public String getAdapterType() {
        return "com.intersystems.dach.ens.adapter.ManagedInboundAdapter";
    }

}
