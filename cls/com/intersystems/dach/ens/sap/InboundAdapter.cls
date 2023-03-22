/// A InterSystems InboundAdapter to receive messages from a SAP system.
Class com.intersystems.dach.ens.sap.InboundAdapter Extends EnsLib.PEX.InboundAdapter [ GeneratedBy = EnsLib.PEX.Utils ]
{

Property CallInterval As %Numeric(MINVAL = 0.1) [ InitialExpression = 0.1 ];

Parameter %REMOTECLASSNAME = "com.intersystems.dach.ens.sap.InboundAdapter";

// TODO: Hide EnableTesting for productive release.

Parameter SETTINGS = "-%remoteClassname,-%remoteSettings,-%gatewayPort,-%gatewayHost,-%gatewayTimeout,-%useHostConnection,ImportXMLSchemas:Additional,XMLSchemaPath:Additional,UseJSON:SAP Server Settings,ConfirmationTimeoutSec:SAP Server Settings,EnableTesting:SAP Server Settings,GatewayHost:SAP Server Settings,GatewayService:SAP Server Settings,ProgrammID:SAP Server Settings,ConnectionCount:SAP Server Settings,Repository:SAP Server Settings,HostAddress:SAP Server Settings,ClientID:SAP Server Settings,SystemNumber:SAP Server Settings,Username:SAP Server Settings,Password:SAP Server Settings,SAPLanguage:SAP Server Settings";

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

/// REQUIRED<br>Set the password. The password is used to connect to the SAP system.
Property Password As %String [ Required ];

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

/// REQUIRED<br>Set the username. The username is used to connect to the SAP system.
Property Username As %String(MAXLEN = 150) [ Required ];

/// If import XML schemas is enabled the XSD files are stored here. This folder must be accessible by the IRIS instance and the JAVA language server.
Property XMLSchemaPath As %String(MAXLEN = "");

Method %remoteClassnameGet()
{
	quit ..#%REMOTECLASSNAME
}

Method SetPropertyValues()
{
	try {
		set $property(..%gatewayProxy,"UseJSON") = ..UseJSON
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"ImportXMLSchemas") = ..ImportXMLSchemas
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"XMLSchemaPath") = ..XMLSchemaPath
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"ConfirmationTimeoutSec") = ..ConfirmationTimeoutSec
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"EnableTesting") = ..EnableTesting
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"GatewayHost") = ..GatewayHost
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"GatewayService") = ..GatewayService
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"ProgrammID") = ..ProgrammID
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"ConnectionCount") = ..ConnectionCount
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"Repository") = ..Repository
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"HostAddress") = ..HostAddress
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"ClientID") = ..ClientID
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"SystemNumber") = ..SystemNumber
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"Username") = ..Username
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"Password") = ..Password
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	try {
		set $property(..%gatewayProxy,"SAPLanguage") = ..SAPLanguage
	} catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	quit
}

}
