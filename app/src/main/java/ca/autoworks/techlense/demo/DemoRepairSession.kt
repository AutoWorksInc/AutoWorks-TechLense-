package ca.autoworks.techlense.demo
data class DemoDtc(val code:String,val description:String)
data class DemoRepairSession(val roNumber:String="RO #18432",val vehicle:String="2020 Ford F-150 5.0L",val vin:String="1FTFW1E50LFA00001",val mileage:String="126,400 km",val concern:String="Check-engine light and rough idle",val dtcs:List<DemoDtc> = emptyList(),val researchSummary:String?=null,val finding:String?=null,val estimateStatus:String?=null)
object DemoData { val scanResults=listOf(DemoDtc("P0300","Random/Multiple Cylinder Misfire Detected"),DemoDtc("P0171","System Too Lean (Bank 1)")) }
