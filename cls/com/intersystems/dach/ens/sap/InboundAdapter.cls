/// A InterSystems InboundAdapter to receive messages from a SAP system.
Class com.intersystems.dach.ens.sap.InboundAdapter Extends EnsLib.PEX.InboundAdapter
{

Property CallInterval As %Numeric(MINVAL = 0.1) [ InitialExpression = 0.1 ];

Parameter %REMOTECLASSNAME = "com.intersystems.dach.ens.sap.InboundAdapter";

// TODO: Hide EnableTesting for productive release.

Parameter SETTINGS = "-%remoteClassname,-%remoteSettings,-%gatewayPort,-%gatewayHost,-%gatewayTimeout,-%useHostConnection,ImportXMLSchemas:XML,XMLSchemaPath:XML,UseJSON:SAP Server Settings,ConfirmationTimeoutSec:SAP Server Settings,EnableTesting:SAP Server Settings,GatewayHost:SAP Server Settings,GatewayService:SAP Server Settings,ProgrammID:SAP Server Settings,ConnectionCount:SAP Server Settings,Repository:SAP Server Settings,HostAddress:SAP Server Settings,ClientID:SAP Server Settings,SystemNumber:SAP Server Settings,SAPLanguage:SAP Server Settings,SAPCredentials:SAP Server Settings";

/// One or more Classpaths (separated by '|' character) needed in addition to the ones configured in the Remote Gateway
Property %gatewayExtraClasspaths As %String(MAXLEN = "") [ InitialExpression = "intersystems-sap-service.jar" ];

/// External Language Server Name
Property %gatewayName As %String [ InitialExpression = "%Java Server", Internal ];

Property %remoteClassname As %String [ Internal, ReadOnly ];

Property %remoteLanguage As %String [ InitialExpression = "JAVA", Internal ];

/// Use Host Connection. If set to true, this adapter will use the connection from the Business Host.
/// If true, this supersedes the External Language Server this Adapter was registered with.
Property %useHostConnection As %Boolean [ InitialExpression = 0, Internal ];

/// REQUIRED<br>Set the buisness partner in the production. The outgoing messages will be directed here.
Property BusinessPartner As %String(MAXLEN = "") [ Required ];

/// REQUIRED<br>Set the client ID. The client ID is used to connect to the SAP system.
Property ClientID As %String [ Required ];

/// "REQUIRED<br>This is the timout for the SAP function handler. If the confirmation takes longer an AbapException exception is thrown.
Property ConfirmationTimeoutSec As %Integer(MAXVAL = 600, MINVAL = 1) [ InitialExpression = 10, Required ];

/// REQUIRED<br>Set the connection count. The connection count is used to connect to the SAP system.
Property ConnectionCount As %Integer [ InitialExpression = 1, Required ];

/// Send test messages for debugging and testing purposes.
Property EnableTesting As %Boolean [ InitialExpression = 0 ];

/// REQUIRED<br>Set the gateway host address. The gateway host address is used to connect to the SAP system.
Property GatewayHost As %String(MAXLEN = "") [ Required ];

/// REQUIRED<br>Set the gateway service. The gateway service is used to connect to the SAP system. Usually 'sapgwNN' whereas NN is the instance number.
Property GatewayService As %String [ Required ];

/// REQUIRED<br>Set the host address. The host address is used to connect to the SAP system.
Property HostAddress As %String(MAXLEN = "") [ Required ];

/// If enabled new XML schemas will be saved and imported to the production automatically.
Property ImportXMLSchemas As %Boolean [ InitialExpression = 0 ];

/*
/// REQUIRED<br>Set the password. The password is used to connect to the SAP system.
Property Password As %String [ Required ];
*/
/// REQUIRED<br>Set the programm ID. The programm ID is used to identify the service in the SAP system.
Property ProgrammID As %String(MAXLEN = "") [ Required ];

/// Set the repository destination. The repository destination is used to connect to the SAP system. Usually 'SAP' or 'SAP_TEST
Property Repository As %String(MAXLEN = "");

/// REQUIRED<br>Set the language. The language is used to connect to the SAP system.
Property SAPLanguage As %String [ Required ];

/// REQUIRED<br>Set the system number. The system number is used to connect to the SAP system.
Property SystemNumber As %String [ Required ];

/// If enabled the service will return a JSON object instead of a XML object
Property UseJSON As %Boolean [ InitialExpression = "false" ];

/*
/// REQUIRED<br>Set the username. The username is used to connect to the SAP system.
 Property Username As %String(MAXLEN = 150) [ Required ];
 */
/// If import XML schemas is enabled the XSD files are stored here. This folder must be accessible by the IRIS instance and the JAVA language server.
Property XMLSchemaPath As %String(MAXLEN = "");

/// This is the ID name of the set of credentials values to be used to access the external system
Property SAPCredentials As %String;

Method %remoteClassnameGet()
{
	Quit ..#%REMOTECLASSNAME
}

Method SetPropertyValues()
{
	Try {
		Set $PROPERTY(..%gatewayProxy,"UseJSON") = ..UseJSON
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"ImportXMLSchemas") = ..ImportXMLSchemas
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"XMLSchemaPath") = ..XMLSchemaPath
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"ConfirmationTimeoutSec") = ..ConfirmationTimeoutSec
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"EnableTesting") = ..EnableTesting
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"GatewayHost") = ..GatewayHost
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"GatewayService") = ..GatewayService
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"ProgrammID") = ..ProgrammID
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"ConnectionCount") = ..ConnectionCount
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"Repository") = ..Repository
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"HostAddress") = ..HostAddress
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"ClientID") = ..ClientID
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"SystemNumber") = ..SystemNumber
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	
	Try {
		Set $PROPERTY(..%gatewayProxy,"SAPLanguage") = ..SAPLanguage
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}


    Try {
        // Use the supplied credentials to determine the SAP username and password
        Set tCredentials=..SAPCredentials
        Set tSAPUser = ""
        Set tSAPPassword = ""
        &sql(select Username,Password into :tSAPUser,:tSAPPassword  from ens_config.credentials where ID=:tCredentials)
        If SQLCODE'=0 {
            //Set tSC=$$$ERROR($$$EnsErrGeneral,$$$Text("Failed to initialize SAPUsername and SAPPassword from supplied Credentials"))
            $$$LOGWARNING("Failed to initialize SAPUsername and SAPPassword from supplied Credentials")
            Quit
        }
		Set $PROPERTY(..%gatewayProxy,"Username") = tSAPUser
		Set $PROPERTY(..%gatewayProxy,"Password") = tSAPPassword
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
    
	Quit
}

}
