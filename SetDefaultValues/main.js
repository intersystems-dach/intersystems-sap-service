let inboundAdapterText = getInboundAdapterTemplate();
/* let inboundAdapterText = fetch('InboundAdapterTemplate.txt')
    .then((response) => response.text())
    .then((data) => console.log(data))
    .catch((error) => console.error(error));
 */
let passThroughServiceText = undefined;

function submit() {
    inboundAdapterText = getInboundAdapterTemplate();
    let obj = {
        UseJSON: 'bool',
        ConfirmationTimeoutSec: 'num',
        EnableTesting: 'bool',
        EnableTracing: 'bool',
        QueueWarningThreshold: 'num',
        GatewayHost: 'string',
        GatewayService: 'string',
        ConnectionCount: 'num',
        HostAddress: 'string',
        ClientID: 'string',
        SystemNumber: 'string',
        SAPLanguage: 'string',
        SAPCredentials: 'string',
        ImportXMLSchemas: 'bool',
        XMLSchemaPath: 'string',
        FlattenTablesItems: 'bool',
        XMLNamespace: 'string',
        CompleteSchema: 'bool',
        LookUpTableName: 'string',
    };

    for (let key in obj) {
        let value;
        if (obj[key] === 'num') {
            value = parseInt(document.getElementById(key).value);
        }
        if (obj[key] === 'bool') {
            value = document.getElementById(key).checked ? 1 : 0;
        }
        if (obj[key] === 'string') {
            value = document.getElementById(key).value;
        }
        inboundAdapterText = inboundAdapterText.replace(
            '+#' + key + '#+',
            value
        );
    }

    for (let i of document.getElementsByClassName('export')) {
        i.style.display = 'block';
    }
    for (let i of document.getElementsByClassName('instruction')) {
        i.style.display = 'none';
    }
}

function darkmode() {
    document.body.style.backgroundColor = '#09091a';
    document.body.style.color = 'white';
}

function copyToClipboard() {
    navigator.clipboard.writeText(inboundAdapterText);

    document.getElementById('copyInstruction').style.display = 'block';
    document.getElementById('downloadInstruction').style.display = 'none';
}

function downloadFile() {
    download('InboundAdapter.cls', inboundAdapterText);

    document.getElementById('copyInstruction').style.display = 'none';
    document.getElementById('downloadInstruction').style.display = 'block';
}

function download(filename, text) {
    let element = document.createElement('a');
    element.setAttribute(
        'href',
        'data:text/plain;charset=utf-8,' + encodeURIComponent(text)
    );
    element.setAttribute('download', filename);

    element.style.display = 'none';
    document.body.appendChild(element);

    element.click();

    document.body.removeChild(element);
}

function getInboundAdapterTemplate() {
    return `
/// v1.0
/// A InterSystems InboundAdapter to receive messages from a SAP system.
Class com.intersystems.dach.ens.sap.InboundAdapter Extends EnsLib.PEX.InboundAdapter
{

Property CallInterval As %Numeric(MINVAL = 0.1) [ InitialExpression = 0.1 ];

Parameter %REMOTECLASSNAME = "com.intersystems.dach.ens.sap.InboundAdapter";

Parameter SETTINGS = "-%remoteClassname,-%remoteSettings,-%gatewayPort,-%gatewayHost,-%gatewayTimeout,-%useHostConnection,UseJSON:SAP Service,ConfirmationTimeoutSec:SAP Service,EnableTesting:SAP Service,EnableTracing:SAP Service,QueueWarningThreshold:SAP Service,GatewayHost:SAP Server Settings,GatewayService:SAP Server Settings,ProgrammID:SAP Server Settings,ConnectionCount:SAP Server Settings,Repository:SAP Server Settings,HostAddress:SAP Client Settings,ClientID:SAP Client Settings,SystemNumber:SAP Client Settings,SAPLanguage:SAP Client Settings,SAPCredentials:SAP Client Settings,ImportXMLSchemas:XML,XMLSchemaPath:XML,FlattenTablesItems:XML,XMLNamespace:XML,CompleteSchema:XML";

/// One or more Classpaths (separated by '|' character) needed in addition to the ones configured in the Remote Gateway
Property %gatewayExtraClasspaths As %String(MAXLEN = "") [ InitialExpression = "intersystems-sap-service.jar" ];

/// External Language Server Name
Property %gatewayName As %String [ InitialExpression = "%Java Server", Internal ];

/// The remote class name of the adapter
Property %remoteClassname As %String [ Internal, ReadOnly ];

/// The remote external language
Property %remoteLanguage As %String [ InitialExpression = "JAVA", Internal ];

/// Use Host Connection. If set to true, this adapter will use the connection from the Business Host.
/// If true, this supersedes the External Language Server this Adapter was registered with.
Property %useHostConnection As %Boolean [ InitialExpression = 0, Internal ];

//	*****************
//	***SAP Service***
//	*****************

/// If enabled the service will return a JSON object instead of a XML object
Property UseJSON As %Boolean [ InitialExpression = +#UseJSON#+ ];

/// REQUIRED<br>This is the timout for the SAP function handler. If the confirmation takes longer an AbapException exception is thrown.
Property ConfirmationTimeoutSec As %Integer(MAXVAL = 600, MINVAL = 1) [ InitialExpression = +#ConfirmationTimeoutSec#+, Required ];

/// Send test messages for debugging and testing purposes.
Property EnableTesting As %Boolean [ InitialExpression = +#EnableTesting#+ ];

/// If enabled the service will print all messages to the log. This is useful for debugging purposes.
Property EnableTracing As %Boolean [ InitialExpression = +#EnableTracing#+ ];

/// REQUIRED<br>The maximum number of messages that can be queued for processing. If the queue is full, the adapter will print a warning and increase the throughput.
Property QueueWarningThreshold As %Integer(MAXVAL = 10000, MINVAL = 1) [ InitialExpression = +#QueueWarningThreshold#+, Required ];

//	*************************
//	***SAP Server Settings***
//	*************************

/// REQUIRED<br>Set the gateway host address. The gateway host address is used to connect to the SAP system.
Property GatewayHost As %String(MAXLEN = "") [ InitialExpression = "+#GatewayHost#+", Required ];

/// REQUIRED<br>Set the gateway service. The gateway service is used to connect to the SAP system. Usually 'sapgwNN' whereas NN is the instance number.
Property GatewayService As %String [ InitialExpression = "+#GatewayService#+", Required ];

/// REQUIRED<br>Set the programm ID. The programm ID is used to identify the service in the SAP system.
Property ProgrammID As %String(MAXLEN = "") [ InitialExpression = "", Required ];

/// REQUIRED<br>Set the connection count. The connection count is used to connect to the SAP system.
Property ConnectionCount As %Integer [ InitialExpression = +#ConnectionCount#+, Required ];

/// Set the repository destination. The repository destination is used to connect to the SAP system. Usually 'SAP' or 'SAP_TEST
Property Repository As %String(MAXLEN = "");

//	*************************
//	***SAP Client Settings***
//	*************************

/// REQUIRED<br>Set the host address. The host address is used to connect to the SAP system.
Property HostAddress As %String(MAXLEN = "") [ InitialExpression = "+#HostAddress#+", Required ];

/// REQUIRED<br>Set the client ID. The client ID is used to connect to the SAP system.
Property ClientID As %String [ InitialExpression = "+#ClientID#+", Required ];

/// REQUIRED<br>Set the system number. The system number is used to connect to the SAP system.
Property SystemNumber As %String [ InitialExpression = "+#SystemNumber#+", Required ];

/// REQUIRED<br>Set the language. The language is used to connect to the SAP system.
Property SAPLanguage As %String [ InitialExpression = "+#SAPLanguage#+", Required ];

/// This is the ID name of the set of credentials values to be used to access the external system
Property SAPCredentials As %String [ InitialExpression = "+#SAPCredentials#+" ];

//	*********
//	***XML***
//	*********

/// If enabled new XML schemas will be saved and imported to the production automatically. If UseJson is enabled this will be ignored.
Property ImportXMLSchemas As %Boolean [ InitialExpression = +#ImportXMLSchemas#+ ];

/// If import XML schemas is enabled the XSD files are stored here. This folder must be accessible by the IRIS instance and the JAVA language server.
Property XMLSchemaPath As %String(MAXLEN = "") [ InitialExpression = "+#XMLSchemaPath#+" ];

/// REQUIRED<br>If enabled the adapter will flatten the tables and will remove the item tags.
Property FlattenTablesItems As %Boolean [ InitialExpression = +#FlattenTablesItems#+ ];

/// Configure the XML namespace for the generated XML. Use "{functionName}" as placeholder for the function name. If left empty the namespace will be generated.
Property XMLNamespace As %String(MAXLEN = "") [ InitialExpression = "+#XMLNamespace#+" ];

/// REQUIRED<br>If enabled the adapter will try to merge the Table schemas to make a complete schema.
Property CompleteSchema As %Boolean [ InitialExpression = +#CompleteSchema#+ ];

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
		Set $PROPERTY(..%gatewayProxy,"EnableTracing") = ..EnableTracing
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
		Set $PROPERTY(..%gatewayProxy,"QueueWarningThreshold") = ..QueueWarningThreshold
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}

	Try {
		Set $PROPERTY(..%gatewayProxy,"FlattenTablesItems") = ..FlattenTablesItems
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	
	Try {
		Set $PROPERTY(..%gatewayProxy,"XMLNamespace") = ..XMLNamespace
	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}
	
	Try {
		Set $PROPERTY(..%gatewayProxy,"CompleteSchema") = ..CompleteSchema
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

    `;
}
