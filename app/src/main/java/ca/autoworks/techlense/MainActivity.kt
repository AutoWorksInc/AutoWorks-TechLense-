package ca.autoworks.techlense
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.autoworks.techlense.demo.DemoData
import ca.autoworks.techlense.demo.DemoRepairSession
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme{TechLenseAlpha()}}}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun TechLenseAlpha(){
 var session by remember{mutableStateOf(DemoRepairSession())}
 Scaffold(topBar={TopAppBar(title={Text("AutoWorks TechLense")})}){padding->
  LazyColumn(modifier=Modifier.padding(padding).padding(16.dp).fillMaxSize(),verticalArrangement=Arrangement.spacedBy(12.dp)){
   item{Text("ALPHA 0.1 • TECHNICIAN WORKSPACE",style=MaterialTheme.typography.labelLarge);Text("Closed-loop repair prototype")}
   item{ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(session.roNumber,style=MaterialTheme.typography.titleLarge);Text(session.vehicle,style=MaterialTheme.typography.titleMedium);Text("VIN: "+session.vin);Text(session.mileage);HorizontalDivider(Modifier.padding(vertical=4.dp));Text("Customer concern",style=MaterialTheme.typography.labelLarge);Text(session.concern)}}}
   item{ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Connections",style=MaterialTheme.typography.titleMedium);Text("Tekmetric: SIMULATED");Text("Autel: SIMULATED");Text("Repair information: SIMULATED");Text("NexPart via Tekmetric: SIMULATED");Text("Ray-Ban Meta: integration foundation installed")}}}
   item{Button(onClick={session=session.copy(dtcs=DemoData.scanResults)},modifier=Modifier.fillMaxWidth()){Text(if(session.dtcs.isEmpty())"SCAN VEHICLE" else "RESCAN VEHICLE")}}
   if(session.dtcs.isNotEmpty()){
    item{ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("AUTEL • SCAN TOOL DATA",style=MaterialTheme.typography.titleMedium);session.dtcs.forEach{Text(it.code+" — "+it.description)};Text("Source: simulated scan connector",style=MaterialTheme.typography.labelSmall)}}}
    item{Button(onClick={session=session.copy(researchSummary="Relevant diagnostic information found. Verify the fault with scan data and directed testing before replacing components.")},modifier=Modifier.fillMaxWidth()){Text("RESEARCH DTCs")}}
   }
   session.researchSummary?.let{summary->item{ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("REPAIR INFORMATION",style=MaterialTheme.typography.titleMedium);Text(summary);Text("SIMULATED — NOT VERIFIED REPAIR INFORMATION",style=MaterialTheme.typography.labelLarge)}}};item{Button(onClick={session=session.copy(finding="Technician confirmed a failed component after directed testing. Photo/video evidence will attach here.")},modifier=Modifier.fillMaxWidth()){Text("DOCUMENT FAILED COMPONENT")}}}
   session.finding?.let{finding->item{ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("TECHNICIAN FINDING",style=MaterialTheme.typography.titleMedium);Text(finding)}}};item{Button(onClick={session=session.copy(estimateStatus="Estimate draft prepared for service-advisor review.")},modifier=Modifier.fillMaxWidth()){Text("PREPARE CLIENT QUOTE")}}}
   session.estimateStatus?.let{status->item{ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("TEKMETRIC • ESTIMATE",style=MaterialTheme.typography.titleMedium);Text(status);Text("No order or customer communication has been submitted.",style=MaterialTheme.typography.labelMedium)}}}}
   item{Text("Simulation is intentionally labeled. Vendor data is VERIFIED only after authorized integrations are connected.",style=MaterialTheme.typography.bodySmall)}
  }
 }
}
