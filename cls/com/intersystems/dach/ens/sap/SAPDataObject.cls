/// Class of SAP InboundAdapter and SAP Passthrough Business Service input data.
Class com.intersystems.dach.ens.sap.SAPDataObject Extends %Library.RegisteredObject
{

Property FunctionName As %String(MAXLEN = 128);

Property Data As %String(MAXLEN = 10000);

Property Schema As %VarString;

Property IsJson As %Boolean;

Method %OnNew(FunctionName As %String(MAXLEN=128) = "", Data As %String(MAXLEN=10000) = "", Schema As %VarString = "", IsJson As %Boolean) As %Status
{
	Set ..FunctionName = FunctionName
	Set ..Data = Data
	Set ..Schema = Schema
    Set ..IsJson = IsJson
	Quit $$$OK
}

}
