/// v1.0
/// A InterSystems InboundAdapter to receive messages from a SAP system.
Class com.intersystems.dach.ens.sap.PassthroughService Extends Ens.BusinessService
{

Parameter ADAPTER = "com.intersystems.dach.ens.sap.InboundAdapter";

/// Configuration item(s) to which to send file stream messages
Property TargetConfigNames As %String(MAXLEN = 1000);

Parameter SETTINGS = "TargetConfigNames:Basic:selector?multiSelect=1&context={Ens.ContextSearch/ProductionItems?targets=1&productionName=@productionId},LookUpTableName:XML";

/// The name of the lookup table to use for the lookup of the xml schemas
Property LookUpTableName As %String(MAXLEN = 1000);

Method OnProcessInput(pInput As com.intersystems.dach.ens.sap.SAPDataObject, pOutput As %RegisteredObject) As %Status
{
   #dim tSC As %Status = $$$OK
   #dim pRequest As Ens.Request

   If (pInput.IsJson) {
      Set pRequest = ##class(Ens.StringRequest).%New(pInput.Data)
   } Else {
      // Data is XML
      Set tStream = ##class(%Stream.TmpCharacter).%New()
      Set tSC = tStream.Write(pInput.Data)
      Quit:$$$ISERR(tSC) tSC
      Kill %objlasterror
      Set pRequest = ##class(EnsLib.EDI.XML.Document).%New(tStream)
      Set:'$ISOBJECT(pRequest) tSC = $$$ERROR($$$EnsErrGeneral, "Failed to generate XML document: "_$$$StatusDisplayString(%objlasterror))
      Quit:$$$ISERR(tSC) tSC

      // search the schema in the lookup table
      Set lookUpValue = ..GetLookUpTableEntry(pInput.FunctionName, ..LookUpTableName)
      If lookUpValue = "" {
         Set pRequest.DocType = pInput.FunctionName_":"_pInput.FunctionName
      }
      Else {
         Set pRequest.DocType = lookUpValue
      }
   }
   
   For iTarget=1:1:$L(..TargetConfigNames, ",") {
      Set tOneTarget=$ZSTRIP($P(..TargetConfigNames,",",iTarget),"<>W")
      Continue:""=tOneTarget
      $$$sysTRACE("Sending message to '"_tOneTarget_"'")
      Set tSC = ..SendRequestAsync(tOneTarget, pRequest)
   }

   Quit tSC
}

/// Return an array of connections for drawing lines on the config diagram
ClassMethod OnGetConnections(Output pArray As %String, pItem As Ens.Config.Item)
{
	Do ##super(.pArray,pItem)
	If pItem.GetModifiedSetting("TargetConfigNames",.tValue) {
		For i=1:1:$L(tValue,",") { Set tOne=$ZSTRIP($P(tValue,",",i),"<>W")  Continue:""=tOne  Set pArray(tOne)="" }
	}
}

/// Looks up a value in the lookup table
/// <li>pKey: The key for the value</li>
/// <li>pLookUpTableName: The LookUp Table name</li>
/// <li><b>returns</b>: The value or an empty String if the value could not be found</li>
/// <EXAMPLE> Do ##class(com.intersystems.dach.ens.sap.PassthroughService).GetLookUpTableEntry("MyKey", "MyTableName")</EXAMPLE>
ClassMethod GetLookUpTableEntry(pKey As %String, pLookUpTableName As %String) As %String
{
   
   // TODO

   #dim result As %String = ""

   If pLookUpTableName = "" {
      Return ""
   }

	Try {
        Set tLookUpTableName= pLookUpTableName
        Set key = ""
        Set value = ""
        &sql(
			DECLARE C1 CURSOR FOR
			SELECT KeyName,DataValue 
			INTO :key,:value
			FROM Ens_Util.LookupTable 
			WHERE TableName=:tLookUpTableName
		)
		&sql(OPEN C1)
			If SQLCODE '= 0 {
				$$$LOGWARNING("Did not find the lookup table")
				Return ""
			}

 		&sql(FETCH C1)

 		While (SQLCODE = 0) {
			If key = pKey{
				Set result = value
				Break
			}
 		   &sql(FETCH C1)
 		}

 		&sql(CLOSE C1)

	} Catch ex {
		$$$LOGWARNING(ex.DisplayString())
	}

   Return result
}

}
