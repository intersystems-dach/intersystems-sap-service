Class com.intersystems.dach.ens.sap.TraceOperation Extends Ens.BusinessOperation
{

Parameter INVOCATION = "Queue";

Method StringMessage(pRequest As Ens.StringRequest, Output pResponse As Ens.Response) As %Status
{
	set pResponse = ##class(Ens.Response).%New()
	return $$$OK
}

Method XMLMessage(pRequest As EnsLib.EDI.XML.Document, Output pResponse As Ens.Response) As %Status
{
	set pResponse = ##class(Ens.Response).%New()
	return $$$OK
}

XData MessageMap
{
<MapItems>
	<MapItem MessageType="Ens.StringRequest"> 
		<Method>StringMessage</Method>
	</MapItem>
	<MapItem MessageType="EnsLib.EDI.XML.Document"> 
		<Method>XMLMessage</Method>
	</MapItem>
    <MapItem MessageType="Ens.StringRequest"> 
		<Method>StringMessage</Method>
	</MapItem>
</MapItems>
}

}
