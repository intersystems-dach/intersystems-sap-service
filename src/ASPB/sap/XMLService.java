package ASPB.sap;

import com.intersystems.enslib.pex.ClassMetadata;
import com.intersystems.enslib.pex.FieldMetadata;
import com.intersystems.gateway.GatewayContext;
import com.intersystems.jdbc.IRIS;
import com.intersystems.jdbc.IRISObject;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.ServerDataProvider;

import ASPB.tests.XMLTestServer;
import ASPB.utils.Buffer;
import ASPB.utils.Callback;
import ASPB.utils.Logger;
import ASPB.utils.Server;

/**
 * A Service to receive messages from a SAP system and return the message in XML
 * format.
 * 
 * @author Philipp Bonin
 * @version 1.0
 */
@ClassMetadata(Description = "A Service to receive messages from a SAP system and return the message in XML format.", InfoURL = "https://github.com/phil1436/intersystems-sap-service")
public class XMLService extends com.intersystems.enslib.pex.BusinessService implements Callback<String> {

    /**
     * *****************************
     * *** Service configuration ***
     * *****************************
     */

    // SAP Service
    @FieldMetadata(Category = "SAP Service Settings", IsRequired = true, Description = "Set the buisness partner in the production. The outgoing messages will be directed here.")
    public static String BusinessPartner;

    @FieldMetadata(Category = "SAP Service Settings", Description = "Set the burst size. If the buffer size is greater than the burst size, the burst size will be used. If the burst size is set to 0, the buffer size will be used.")
    public static int BurstSize = 10;

    @FieldMetadata(Category = "SAP Service Settings", Description = "Set the maximum buffer size. It descripes how many elements can be added to the buffer. If the maximum buffer size is set to 0, the buffer size will not be limited.")
    public static int MaxBufferSize = 100;

    // Server Settings
    @FieldMetadata(Category = "SAP Server Settings", IsRequired = true, Description = "Set the gateway host address. The gateway host address is used to connect to the SAP system.")
    public static String GatewayHost;

    @FieldMetadata(Category = "SAP Server Settings", IsRequired = true, Description = "Set the gateway service. The gateway service is used to connect to the SAP system. Usually 'sapgwNN' whereas NN is the instance number")
    public static String GatewayService;

    @FieldMetadata(Category = "SAP Server Settings", IsRequired = true, Description = "Set the programm ID. The programm ID is used to identify the service in the SAP system.")
    public static String ProgrammID;

    @FieldMetadata(Category = "SAP Server Settings", IsRequired = true, Description = "Set the connection count. The connection count is used to connect to the SAP system.")
    public static int ConnectionCount;

    @FieldMetadata(Category = "SAP Server Settings", IsRequired = true, Description = "Set the repository destination. The repository destination is used to connect to the SAP system. Usually 'SAP' or 'SAP_TEST")
    public static String Repository;

    // Client Settings
    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "Set the host address. The host address is used to connect to the SAP system.")
    public static String HostAddress;

    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "Set the client ID. The client ID is used to connect to the SAP system.")
    public static String ClientID;

    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "Set the system number. The system number is used to connect to the SAP system.")
    public static String SystemNumber;

    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "Set the username. The username is used to connect to the SAP system.")
    public static String Username;

    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "Set the password. The password is used to connect to the SAP system.")
    public static String Password;

    @FieldMetadata(Category = "SAP Client Settings", IsRequired = true, Description = "Set the language. The language is used to connect to the SAP system.")
    public static String Language;

    // Logging
    @FieldMetadata(Category = "Logging", Description = "Set the log file path. If the log file path is set to an empty string, there will be no log file created. The log file path must refer to an already existing file.")
    public static String LogFilePath = "";

    @FieldMetadata(Category = "Logging", Description = "If enabled the log file will be cleared on restart.")
    public static boolean ClearLogOnRestart = false;

    /**
     * ******************
     * *** Attributes ***
     * ******************
     */

    // The buffer
    private static Buffer<String> buffer = new Buffer<String>(MaxBufferSize);

    // The iris connection
    private IRIS iris;

    // The server
    private Server server;

    @Override
    public void OnInit() throws Exception {
        // get iris connection
        iris = GatewayContext.getIRIS();
        // set the log file path
        Logger.setFilePath(LogFilePath);
        // clear the log file
        if (ClearLogOnRestart)
            Logger.clear();

        // init the server
        server = new XMLTestServer();
        server.registerCallback(this);
        // server = new SAPServer(false, this);

        // Set connection properties
        this.setSAPProperties();

        if (server.start()) {
            Logger.log("SAPService started");
            LOGINFO("SAPService started");
        } else {
            Logger.log("SAPService could not be started");
            LOGERROR("SAPService could not be started");
        }
    }

    @Override
    public Object OnProcessInput(Object arg0) throws Exception {
        // set the burst size
        int burst = buffer.size();
        if (burst > BurstSize && BurstSize > 0)
            burst = BurstSize;

        boolean result = true;

        // send
        for (int i = 0; i < burst; i++) {
            String s = buffer.poll();
            IRISObject request = (IRISObject) iris.classMethodObject("EnsLib.EDI.XML.Document", "%New", s);
            try {
                this.SendRequestAsync(XMLService.BusinessPartner, request);
            } catch (Exception e) {
                Logger.error("Error while sending request: " + e.getMessage());
                result = false;
            }
        }

        return result;
    }

    @Override
    public void OnTearDown() throws Exception {

        // TODO
        // was passiert mit dem buffer wenn der service beendet wird???
        // buffer geht verloren!!!
        iris.close();

        if (server.stop()) {
            Logger.log("SAPService stopped");
            LOGINFO("SAPService stopped");
        } else {
            Logger.log("SAPService could not be stopped");
            LOGERROR("SAPService could not be stopped");
        }
    }

    @Override
    public boolean call(String arg0) {
        return buffer.add(arg0);
    }

    private void setSAPProperties() {
        if (server == null)
            return;

        // Server settings
        server.setProperty(ServerDataProvider.JCO_PROGID, XMLService.ProgrammID);
        server.setProperty(ServerDataProvider.JCO_GWHOST, XMLService.GatewayHost);
        server.setProperty(ServerDataProvider.JCO_GWSERV, XMLService.GatewayService);
        server.setProperty(ServerDataProvider.JCO_CONNECTION_COUNT, String.valueOf(XMLService.ConnectionCount));
        server.setProperty(ServerDataProvider.JCO_REP_DEST, XMLService.Repository);

        // Client settings
        server.setProperty(DestinationDataProvider.JCO_ASHOST, XMLService.HostAddress);
        server.setProperty(DestinationDataProvider.JCO_CLIENT, XMLService.ClientID);
        server.setProperty(DestinationDataProvider.JCO_SYSNR, XMLService.SystemNumber);
        server.setProperty(DestinationDataProvider.JCO_USER, XMLService.Username);
        server.setProperty(DestinationDataProvider.JCO_PASSWD, XMLService.Password);
        server.setProperty(DestinationDataProvider.JCO_LANG, XMLService.Language);

    }
}
