/// Managed InboundAdapter: Business Service decides to wait for next call intervall or re-call OnTask immediately.
Class com.intersystems.dach.ens.adapter.ManagedInboundAdapter Extends Ens.InboundAdapter
{

Method OnTask() As %Status
{
	Set tSC = ..BusinessHost.ProcessInput($$$NULLOREF)
	Quit tSC
}

}