/// A Service to receive messages from a SAP system using the sapjco library and return the message in JSON format.
Class ASPB.sap.JSONService Extends EnsLib.PEX.BusinessService [ GeneratedBy = EnsLib.PEX.Utils ]
{

Parameter %REMOTECLASSNAME = "ASPB.sap.JSONService";

Parameter %URL = "https://github.com/phil1436/intersystems-sap-service";

Parameter SETTINGS = "-%remoteClassname,-%remoteSettings,-%gatewayName,-%gatewayHost,-%gatewayPort,-%gatewayTimeout,-%gatewayExtraClasspaths,BusinessPartner:SAP Service Settings,BurstSize:SAP Service Settings,MaxBufferSize:SAP Service Settings,GatewayHost:SAP Server Connection,GatewayService:SAP Server Connection,ProgrammID:SAP Server Connection,ConnectionCount:SAP Server Connection,Repository:SAP Server Connection,HostAddress:SAP Client Connection,ClientID:SAP Client Connection,SystemNumber:SAP Client Connection,Username:SAP Client Connection,Password:SAP Client Connection,Language:SAP Client Connection,LogFilePath:Logging,ClearLogOnRestart:Logging";

/// Ein oder mehrere Klassenpfade (getrennt durch das Zeichen ‚|‘), die zusätzlich zu den im Remote Gateway konfigurierten Pfaden benötigt werden
Property %gatewayExtraClasspaths As %String(MAXLEN = "") [ InitialExpression = "C:\Users\pbonin\Desktop\SAPService\SAPService.jar" ];

/// Name des externen Sprachen-Servers
Property %gatewayName As %String [ InitialExpression = "%Java Server", Internal ];

Property %remoteClassname As %String [ Internal, ReadOnly ];

Property %remoteLanguage As %String [ InitialExpression = "JAVA", Internal ];

/// Set the burst size. If the buffer size is greater than the burst size, the burst size will be used. If the burst size is set to 0, the buffer size will be used.
Property BurstSize As %Integer [ InitialExpression = 10 ];

/// REQUIRED<br>Set the buisness partner in the production. The outgoing messages will be directed here.
Property BusinessPartner As %String [ Required ];

/// If enabled the log file will be cleared on restart.
Property ClearLogOnRestart As %Boolean [ InitialExpression = "false" ];

/// REQUIRED<br>Set the client ID. The client ID is used to connect to the SAP system.
Property ClientID As %String [ Required ];

/// REQUIRED<br>Set the connection count. The connection count is used to connect to the SAP system.
Property ConnectionCount As %Integer [ InitialExpression = 1, Required ];

/// REQUIRED<br>Set the gateway host address. The gateway host address is used to connect to the SAP system.
Property GatewayHost As %String [ Required ];

/// REQUIRED<br>Set the gateway service. The gateway service is used to connect to the SAP system. Usually 'sapgwNN' whereas NN is the instance number.
Property GatewayService As %String [ Required ];

/// REQUIRED<br>Set the host address. The host address is used to connect to the SAP system.
Property HostAddress As %String [ Required ];

/// REQUIRED<br>Set the language. The language is used to connect to the SAP system.
Property Language As %String [ Required ];

/// Set the log file path. If the log file path is set to an empty string, there will be no log file created. The log file path must refer to an already existing file.
Property LogFilePath As %String;

/// Set the maximum buffer size. It descripes how many elements can be added to the buffer. If the maximum buffer size is set to 0, the buffer size will not be limited.
Property MaxBufferSize As %Integer [ InitialExpression = 100 ];

/// REQUIRED<br>Set the password. The password is used to connect to the SAP system.
Property Password As %String [ Required ];

/// REQUIRED<br>Set the programm ID. The programm ID is used to identify the service in the SAP system.
Property ProgrammID As %String [ Required ];

/// Set the repository destination. The repository destination is used to connect to the SAP system. Usually 'SAP' or 'SAP_TEST
Property Repository As %String;

/// REQUIRED<br>Set the system number. The system number is used to connect to the SAP system.
Property SystemNumber As %String [ Required ];

/// REQUIRED<br>Set the username. The username is used to connect to the SAP system.
Property Username As %String [ Required ];

Method %remoteClassnameGet()
{
	Quit ..#%REMOTECLASSNAME
}

Method SetPropertyValues()
{
	Try {
		Set $PROPERTY(..%gatewayProxy,"BusinessPartner") = ..BusinessPartner
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"BurstSize") = ..BurstSize
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"MaxBufferSize") = ..MaxBufferSize
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
		Set $PROPERTY(..%gatewayProxy,"Username") = ..Username
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"Password") = ..Password
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"Language") = ..Language
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"LogFilePath") = ..LogFilePath
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Try {
		Set $PROPERTY(..%gatewayProxy,"ClearLogOnRestart") = ..ClearLogOnRestart
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	Quit
}

}