package ca.autoworks.techlense
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ca.autoworks.techlense.demo.*
import ca.autoworks.techlense.diagnostics.*
import ca.autoworks.techlense.evidence.*

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme{MekViewAlpha()}}}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun MekViewAlpha(){
 var s by remember{mutableStateOf(DemoRepairSession())}
 val context=LocalContext.current
 val evidenceLaunchers=rememberEvidenceLaunchers(
  createUri={type->EvidenceCapture.newEvidenceUri(context,type)},
  onCaptured={type,uri->s=s.copy(evidence=s.evidence+InspectionEvidence((if(type==EvidenceMediaType.PHOTO)"photo-" else "video-")+(s.evidence.size+1),type,"Pixel camera "+type.name.lowercase()+" evidence",System.currentTimeMillis(),uri.toString(),false))}
 )
 Scaffold(topBar={TopAppBar(title={Text("MekView")})}){p->
  LazyColumn(Modifier.padding(p).padding(16.dp).fillMaxSize(),verticalArrangement=Arrangement.spacedBy(12.dp)){
   item{Text("ALPHA 0.1 • GUIDED DIAGNOSTICS",style=MaterialTheme.typography.labelLarge);Text(s.roNumber+" • "+s.vehicle);Text(s.concern)}
   item{ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Connections",style=MaterialTheme.typography.titleMedium);Text("Tekmetric • SIMULATED");Text("Autel • SIMULATED");Text("Repair info • SIMULATED");Text("Ray-Ban Meta • FOUNDATION")}}}
   item{Button(onClick={s=s.copy(dtcs=DemoData.scanResults)},Modifier.fillMaxWidth()){Text(if(s.dtcs.isEmpty())"SCAN VEHICLE" else "RESCAN VEHICLE")}}
   if(s.dtcs.isNotEmpty()){
    item{ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("SCAN TOOL DATA",style=MaterialTheme.typography.titleMedium);s.dtcs.forEach{Text(it.code+" — "+it.description)}}}}
    if(s.diagnosticSteps.isEmpty()) item{Button(onClick={s=s.copy(diagnosticSteps=DemoGuidedDiagnosis.misfireLean().steps)},Modifier.fillMaxWidth()){Text("START GUIDED DIAGNOSIS")}}
   }
   if(s.diagnosticSteps.isNotEmpty()){
    item{Text("SIMULATED TRAINING PROCEDURE — NOT VERIFIED",style=MaterialTheme.typography.labelLarge)}
    items(s.diagnosticSteps.size){i->
     val step=s.diagnosticSteps[i]
     ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
      Text("Step "+step.id,style=MaterialTheme.typography.titleMedium);Text(step.instruction)
      if(i==s.activeStep){
       Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
        Button(onClick={val n=s.diagnosticSteps.toMutableList();n[i]=step.copy(status=StepStatus.PASSED,technicianResult="Technician confirmed");s=s.copy(diagnosticSteps=n,activeStep=i+1)}){Text("PASS")}
        OutlinedButton(onClick={val n=s.diagnosticSteps.toMutableList();n[i]=step.copy(status=StepStatus.FAILED,technicianResult="Fault found");s=s.copy(diagnosticSteps=n,activeStep=i+1)}){Text("FAULT FOUND")}
       }
      } else if(step.technicianResult!=null) Text(step.technicianResult,style=MaterialTheme.typography.labelMedium)
     }}
    }
    item{Button(onClick=evidenceLaunchers.takePhoto,modifier=Modifier.fillMaxWidth()){Text("TAKE PHOTO WITH PIXEL")}}
    item{OutlinedButton(onClick=evidenceLaunchers.takeVideo,modifier=Modifier.fillMaxWidth()){Text("RECORD VIDEO WITH PIXEL")}}
   }
   if(s.evidence.isNotEmpty()){
    item{ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("INSPECTION EVIDENCE",style=MaterialTheme.typography.titleMedium);s.evidence.forEach{Text(it.type.name+" • "+it.note)}}}}
    item{Button(onClick={s=s.copy(finding="Failure documented with "+s.evidence.size+" evidence item(s). Ready for estimate review.")},Modifier.fillMaxWidth()){Text("CONFIRM FAILED COMPONENT")}}
   }
   s.finding?.let{f->item{Text(f)};item{Button(onClick={s=s.copy(estimateStatus="Estimate draft ready for service-advisor review. No customer communication or parts order submitted.")},Modifier.fillMaxWidth()){Text("PREPARE CLIENT QUOTE")}}}
   s.estimateStatus?.let{item{ElevatedCard(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("TEKMETRIC • ESTIMATE DRAFT",style=MaterialTheme.typography.titleMedium);Text(it)}}}}
  }
 }
}
