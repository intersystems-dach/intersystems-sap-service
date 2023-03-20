package com.intersystems.dach.ens.bs;

import java.lang.reflect.Field;
import java.util.Properties;

import com.intersystems.dach.ens.bs.utils.Buffer;
import com.intersystems.dach.ens.bs.annotations.ClassMetadata; //intersystems-util-3.2.0 or older
import com.intersystems.dach.ens.bs.annotations.FieldMetadata; //intersystems-util-3.2.0 or older
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
 * @author Philipp Bonin
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

    @FieldMetadata(Category = "SAP Service Settings", Description = "Set the maximum buffer size. It descripes how many elements can be added to the buffer. If the maximum buffer size is set to 0, the buffer size will not be limited.")
    public int MaxBufferSize = 100;

    @FieldMetadata(Category = "SAP Service Settings", Description = "If the buffer is the service will retry to add incomming messages to the buffer for a specified period.")
    public int RetryPeriodSeconds = 10;

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

    private Buffer<SAPServerImportData> buffer;
    private Buffer<Error> errorBuffer;
    private Buffer<Exception> exceptionBuffer;

    @Override
    public void OnInit() throws Exception {

        // Prepare buffers
        buffer = new Buffer<SAPServerImportData>(this.MaxBufferSize);
        errorBuffer = new Buffer<Error>();
        exceptionBuffer = new Buffer<Exception>();

        // Prepare SAP JCo server
        try {
            Properties settings = this.generateSettingsProperties();
            sapServer = new SAPServer(settings, this, this.UseJSON);
            sapServer.registerErrorHandler(this);
            sapServer.registerExceptionHandler(this);
            sapServer.start();
            LOGINFO("Started SAP Service.");
        } catch (Exception e) {
            LOGERROR("SAPService could not be started: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    private boolean ProcessImportDataQueue() {
        // set the burst size
        int burst = buffer.size();
        boolean result = true;

        // send
        for (int i = 0; i < burst; i++) {
            SAPServerImportData importData = buffer.poll();

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

                // TODO schema import handling
            }

            try {
                this.SendRequestAsync(this.BusinessPartner, request);
            } catch (Exception e) {
                LOGERROR("Error while sending request: " + e.getMessage());
                result = false;
            }
        }

        return result;
    }

    @Override
    public Object OnProcessInput(Object msg) throws Exception {
        boolean result = this.ProcessImportDataQueue();

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

        // Empty imort data queue
        this.ProcessImportDataQueue();

        // Close iris connection
        GatewayContext.getIRIS().close();
    }

    @Override
    public boolean OnImportDataReceived(SAPServerImportData data) {
        int retryCount = 0;
        while (!buffer.add(data)) {
            retryCount++;
            if (retryCount > this.RetryPeriodSeconds) {
                this.errorBuffer.add(new Error("Could not add incomming message to buffer."));
                return false;
            }

            // Wait 1 second until next retry
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        return true;
    }

    @Override
    public void OnErrorOccured(Error err) {
        this.errorBuffer.add(err);
    }

    @Override
    public void OnExceptionOccured(Exception e) {
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

}
