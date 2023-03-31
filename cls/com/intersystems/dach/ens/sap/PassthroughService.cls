/// v1.0
/// A InterSystems InboundAdapter to receive messages from a SAP system.
Class com.intersystems.dach.ens.sap.PassthroughService Extends Ens.BusinessService
{

Parameter ADAPTER = "com.intersystems.dach.ens.sap.InboundAdapter";

/// Configuration item(s) to which to send file stream messages
Property TargetConfigNames As %String(MAXLEN = 1000);

Parameter SETTINGS = "TargetConfigNames:Basic:selector?multiSelect=1&context={Ens.ContextSearch/ProductionItems?targets=1&productionName=@productionId}";

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
      Set pRequest.DocType = pInput.FunctionName_":"_pInput.FunctionName
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

}
