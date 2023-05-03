package com.intersystems.dach.ens.sap;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.intersystems.dach.ens.common.annotations.ClassMetadata; //intersystems-util-3.2.x or older
import com.intersystems.dach.ens.common.annotations.FieldMetadata; //intersystems-util-3.2.x or older
import com.intersystems.dach.ens.sap.testing.TestCase;
import com.intersystems.dach.ens.sap.testing.TestRunner;
import com.intersystems.dach.ens.sap.testing.TestStatusHandler;
import com.intersystems.dach.ens.sap.testing.TestCaseCollection;
import com.intersystems.dach.ens.sap.utils.IRISXSDSchemaImporter;
import com.intersystems.dach.sap.SAPServer;
import com.intersystems.dach.sap.SAPServerArgs;
import com.intersystems.dach.sap.SAPImportData;
import com.intersystems.dach.sap.annotations.SAPJCoPropertyAnnotation;
import com.intersystems.dach.sap.handlers.SAPServerErrorHandler;
import com.intersystems.dach.sap.handlers.SAPServerExceptionHandler;
import com.intersystems.dach.sap.handlers.SAPServerImportDataHandler;
import com.intersystems.dach.sap.handlers.SAPServerStateHandler;
import com.intersystems.dach.utils.TraceManager;
//import com.intersystems.enslib.pex.ClassMetadata; //intersystems-util-3.3.0 or newer
//import com.intersystems.enslib.pex.FieldMetadata; //intersystems-util-3.3.0 or newer
import com.intersystems.gateway.GatewayContext;
import com.intersystems.jdbc.IRIS;
import com.intersystems.jdbc.IRISObject;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.server.JCoServerState;

/**
 * A InboundAdapter to receive messages from a SAP system.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 */
@ClassMetadata(Description = "A InterSystems InboundAdapter to receive messages from a SAP system.", InfoURL = "https://github.com/phil1436/intersystems-sap-service")
public class InboundAdapter extends com.intersystems.enslib.pex.InboundAdapter
        implements SAPServerImportDataHandler, SAPServerErrorHandler, SAPServerExceptionHandler, SAPServerStateHandler {

    /**
     * *****************************
     * *** Service configuration ***
     * *****************************
     */

    // SAP Service
    @FieldMetadata(Category = "SAP Service", Description = "If enabled the service will return a JSON object instead of a XML object")
    public boolean UseJSON = false;

    @FieldMetadata(Category = "SAP Service", IsRequired = true, Description = "REQUIRED<br>This is the timout for the SAP function handler. If the confirmation takes longer an AbapException exception is thrown.")
    public Integer ConfirmationTimeoutSec = 10;

    @FieldMetadata(Category = "SAP Service", Description = "Send test messages for debugging and testing purposes.")
    public boolean EnableTesting = false;

    @FieldMetadata(Category = "SAP Service", Description = "If enabled the service will print all messages to the console. This is useful for debugging purposes.")
    public boolean EnableTracing = false;

    @FieldMetadata(Category = "SAP Service", IsRequired = true, Description = "REQUIRED<br>The maximum number of messages that can be queued for processing. If the queue is full, the adapter will print a warning and increase the throughput.")
    public int QueueWarningThreshold = 100;

    // SAP Server Settings
    @SAPJCoPropertyAnnotation(jCoName = ServerDataProvider.JCO_GWHOST)
    @FieldMetadata(Category = "SAP Server Settings", IsRequired = true, Description = "REQUIRED<br>Set the gateway host address. The gateway host address is used to connect to the SAP system.")
    public String GatewayHost = "";

    @SAPJCoPropertyAnnotation(jCoName = ServerDataProvider.JCO_GWSERV)
    @FieldMetadata(Category = "SAP Server Settings", IsRequired = true, Description = "REQUIRED<br>Set the gateway service. The gateway service is used to connect to the SAP system. Usually 'sapgwNN' whereas NN is the instance number.")
    public String GatewayService = "";

    @SAPJCoPropertyAnnotation(jCoName = ServerDataProvider.JCO_PROGID)
    @FieldMetadata(Category = "SAP Server Settings", IsRequired = true, Description = "REQUIRED<br>Set the programm ID. The programm ID is used to identify the service in the SAP system.")
    public String ProgrammID = "";

    @SAPJCoPropertyAnnotation(jCoName = ServerDataProvider.JCO_CONNECTION_COUNT)
    @FieldMetadata(Category = "SAP Server Settings", IsRequired = true, Description = "REQUIRED<br>Set the connection count. The connection count is used to connect to the SAP system.")
    public int ConnectionCount = 1;

    @SAPJCoPropertyAnnotation(jCoName = ServerDataProvider.JCO_REP_DEST)
    @FieldMetadata(Category = "SAP Server Settings", Description = "Set the repository destination. The repository destination is used to connect to the SAP system. Usually 'SAP' or 'SAP_TEST")
    public String Repository = "";

    // SAP Client Settings
    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_ASHOST)
    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "REQUIRED<br>Set the host address. The host address is used to connect to the SAP system.")
    public String HostAddress = "";

    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_CLIENT)
    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "REQUIRED<br>Set the client ID. The client ID is used to connect to the SAP system.")
    public String ClientID = "";

    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_SYSNR)
    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "REQUIRED<br>Set the system number. The system number is used to connect to the SAP system.")
    public String SystemNumber = "";

    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_LANG)
    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "REQUIRED<br>Set the language. The language is used to connect to the SAP system.")
    public String SAPLanguage = "";

    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_USER)
    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "REQUIRED<br>Set the username. The username is used to connect to the SAP system.")
    public String Username = "";

    @SAPJCoPropertyAnnotation(jCoName = DestinationDataProvider.JCO_PASSWD)
    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "REQUIRED<br>Set the password. The password is used to connect to the SAP system.")
    public String Password = "";

    // XML
    @FieldMetadata(Category = "XML", Description = "If enabled new XML schemas will be saved and imported to the production automatically. If UseJson is enabled this will be ignored.")
    public boolean ImportXMLSchemas = false;

    @FieldMetadata(Category = "XML", Description = "If import XML schemas is enabled the XSD files are stored here. This folder must be accessible by the IRIS instance and the JAVA language server.")
    public String XMLSchemaPath = "";

    @FieldMetadata(Category = "XML", Description = "REQUIRED<br>The maximum number of messages that can be queued for processing. If the queue is full, the adapter will print a warning and increase the throughput.")
    public boolean FlattenTablesItems = false;

    /**
     * ***************
     * *** Members ***
     * ***************
     */

    private IRIS iris;
    private SAPServer sapServer;
    private IRISXSDSchemaImporter irisSchemaImporter;

    private Queue<SAPImportData> importDataQueue;
    private Queue<Error> errorBuffer;
    private Queue<Exception> exceptionBuffer;
    private Queue<String> traceBuffer;
    private Queue<String> warningTraceBuffer;

    private boolean warningActiveFlag = false;

    @Override
    public void OnInit() throws Exception {

        TraceManager traceManager = new TraceManager();
        // register trace handler
        if (this.EnableTracing) {
            LOGINFO("Tracing is enabled.");
            traceBuffer = new ConcurrentLinkedQueue<String>();
            traceManager.registerTraceMsgHandler((traceMsg) -> traceBuffer.add(traceMsg));

            // TODO only enable with traces?
            /*
             * warningTraceBuffer = new ConcurrentLinkedQueue<String>();
             * TraceManager.getTraceManager(objectProvider.getWarningTraceManagerHandle())
             * .registerTraceMsgHandler((warningTraceMsg) ->
             * warningTraceBuffer.add(warningTraceMsg));
             */
        }

        // get iris instance
        iris = GatewayContext.getIRIS();

        // print format
        LOGINFO((UseJSON ? "JSON" : "XML") + " is enabled.");

        if (FlattenTablesItems) {
            LOGINFO("FlattenTablesItems is enabled.");
        }

        // Prepare buffers
        importDataQueue = new ConcurrentLinkedQueue<SAPImportData>();
        errorBuffer = new ConcurrentLinkedQueue<Error>();
        exceptionBuffer = new ConcurrentLinkedQueue<Exception>();

        // Prepare Schema Import
        if (!UseJSON && ImportXMLSchemas) {
            LOGINFO("XML Schemas import is enabled.");
            irisSchemaImporter = new IRISXSDSchemaImporter(this.XMLSchemaPath, traceManager);
            LOGINFO("XSD directory: " + irisSchemaImporter.getXsdDirectoryPath());
        }

        // Prepare SAP JCo server
        try {
            Properties settings = this.generateSettingsProperties();

            SAPServerArgs sapServerArgs = new SAPServerArgs(settings,
                    traceManager,
                    this.FlattenTablesItems,
                    this.ConfirmationTimeoutSec * 1000,
                    this.UseJSON);

            sapServer = new SAPServer(sapServerArgs);
            sapServer.registerImportDataHandler(this);
            sapServer.registerErrorHandler(this);
            sapServer.registerExceptionHandler(this);
            sapServer.registerStateHandler(this);
            sapServer.start();
            LOGINFO("Started SAP Service.");
        } catch (Exception e) {
            LOGERROR("SAPService could not be started: " + e.getMessage());
            sapServer.stop();
            handleMessages();
            throw new RuntimeException();
        }

        if (this.EnableTesting) {
            LOGWARNING("Testing is enabled.");
            testing();
        }
    }

    /**
     * Handle trace and errors messages and exceptions.
     * 
     * @return True if an error or exception occured, false if not.
     */
    private boolean handleMessages() {
        // Handle trace, errors and exceptions
        if (traceBuffer != null) {
            while (!traceBuffer.isEmpty()) {
                LOGINFO("## " + traceBuffer.poll());
            }
        }

        if (warningTraceBuffer != null) {
            while (!warningTraceBuffer.isEmpty()) {
                LOGWARNING("## " + warningTraceBuffer.poll());
            }
        }

        boolean errorOrExceptionOccured = false;

        while (!exceptionBuffer.isEmpty()) {
            Exception e = exceptionBuffer.poll();
            LOGERROR("An exception occured in SAP server: " + e.getMessage());
            errorOrExceptionOccured = true;
        }

        while (!errorBuffer.isEmpty()) {
            Error err = errorBuffer.poll();
            LOGERROR("An error occured in SAP server: " + err.getMessage());
            errorOrExceptionOccured = true;
        }

        return errorOrExceptionOccured;

    }

    @Override
    public void OnTask() throws Exception {

        if (importDataQueue.isEmpty()) {
            if (handleMessages()) {
                sapServer.stop();
                handleMessages();
                throw new RuntimeException();
            }

            // No data in queue, wait for next call intervall
            BusinessHost.irisHandle.set("%WaitForNextCallInterval", true);
            // Reset warning flag
            if (warningActiveFlag) {
                LOGINFO("Back to normal load.");
                warningActiveFlag = false;
            }

            return;
        }

        // Trigger high load warning
        if (!warningActiveFlag && importDataQueue.size() > this.QueueWarningThreshold) {
            LOGWARNING("High load. Current messages in Queue: " + importDataQueue.size());
            warningActiveFlag = true;
        }

        // Get import data from queue
        SAPImportData importData = importDataQueue.poll();

        // Import XML schemas
        if (!importData.isJSON() && ImportXMLSchemas && irisSchemaImporter != null) {
            // import xsd
            if (!importData.isSchemaComplete()) {
                LOGWARNING("Importing incomplete XML schema for function '" + importData.getFunctionName() + "'.");
            }
            try {
                boolean importResult = irisSchemaImporter.importSchemaIfNotExists(
                        importData.getFunctionName(),
                        importData.getSchema(),
                        importData.isSchemaComplete());
                if (importResult) {
                    LOGINFO("Imported new XML schema: " + importData.getFunctionName());
                }
            } catch (Exception e) {
                LOGERROR("Error while importing XML schema for function '" +
                        importData.getFunctionName() + "': " + e.getMessage());
            }
        }

        // Call ProcessInput
        IRISObject irisObject = (IRISObject) iris.classMethodObject("com.intersystems.dach.ens.sap.SAPDataObject",
                "%New", importData.getFunctionName(), importData.getData(), importData.getSchema(),
                importData.isJSON());
        BusinessHost.ProcessInput(irisObject);
        importData.confirmProcessed(); // Data is now persistent in the Business process queue.

        if (handleMessages()) {
            sapServer.stop();
            handleMessages();
            throw new RuntimeException();
        }

        /*
         * Wait for next call intervall if queue is empty or call
         * OnProcessInput immediately again.
         */
        BusinessHost.irisHandle.set("%WaitForNextCallInterval", importDataQueue.isEmpty());

        if (this.EnableTracing) {
            if (importDataQueue.isEmpty()) {
                this.LOGINFO("## No data in queue, wait for next call intervall.");
            } else {
                this.LOGINFO("## Data in queue, call OnProcessInput again.");
            }
        }
    }

    @Override
    public void OnTearDown() throws Exception {
        try {
            sapServer.stop();
            LOGINFO("SAPService stopped");
        } catch (Exception e) {
            LOGERROR("An exception occured during stop of the server: " + e.getMessage());
        }

        handleMessages();

        // Close iris connection
        GatewayContext.getIRIS().close();

        // Reset iris XML schema importer
        this.irisSchemaImporter = null;
    }

    /**
     * Initialize and start testing.
     */
    private void testing() {
        TestRunner testRunner = new TestRunner();
        Collection<TestCase> testCases;
        if (this.UseJSON) {
            testCases = TestCaseCollection.getJSONTestCases();
            LOGINFO("Running " + testCases.size() + " JSON test cases.");
        } else {
            testCases = TestCaseCollection.getXMLTestCases();
            LOGINFO("Running " + testCases.size() + " XML test cases.");
        }
        testRunner.addTestCases(testCases);
        testRunner.runTestsAsync(this, new TestStatusHandler() {
            public void onTestStatus(String msg) {
                LOGINFO("TESTING STATUS: " + msg);
            }
        });
    }

    @Override
    public void onImportDataReceived(SAPImportData data) {
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

    @Override
    public void onStateChanged(JCoServerState oldState, JCoServerState newState) {
        if (this.EnableTracing) {
            traceBuffer.add("SAP server state changed from " + oldState.toString() + " to " + newState.toString());
        }
    }

    /**
     * This is a workaround to handle a bug in IRIS < 2022.1
     * 
     * @param hostObject
     * @throws java.lang.Exception
     */
    public void dispatchOnInit(com.intersystems.jdbc.IRISObject hostObject) throws java.lang.Exception {
        _dispatchOnInit(hostObject);
    }

    /**
     * This is a workaround to handle a bug in IRIS < 2022.1
     */
    public void _setIrisHandles(com.intersystems.jdbc.IRISObject handleCurrent,
            com.intersystems.jdbc.IRISObject handlePartner) {
        setIrisHandles(handleCurrent, handlePartner);
    }

    /**
     * Helper method to generate SAP settings properties by using field annotations.
     * 
     * @return Properties for SAP server settings.
     * @throws Exception when a required field is missing.
     */
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

}