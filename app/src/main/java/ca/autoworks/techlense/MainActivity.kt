package ca.autoworks.techlense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.autoworks.techlense.demo.*
import ca.autoworks.techlense.diagnostics.*
import ca.autoworks.techlense.evidence.*

private val MekGreen=Color(0xFF43B83E)
private val ShopCharcoal=Color(0xFF223136)
private val Workspace=Color(0xFFF3F4F4)
private val Muted=Color(0xFF667176)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{
  MaterialTheme(colorScheme=lightColorScheme(primary=MekGreen,surface=Color.White,background=Workspace)){MekViewAlpha()}
 }}
}

@Composable private fun MekViewAlpha(){
 var s by remember{mutableStateOf(DemoRepairSession())}
 var tab by remember{mutableIntStateOf(0)}
 val context=LocalContext.current
 val evidenceLaunchers=rememberEvidenceLaunchers(
  createUri={type->EvidenceCapture.newEvidenceUri(context,type)},
  onCaptured={type,uri->s=s.copy(evidence=s.evidence+InspectionEvidence((if(type==EvidenceMediaType.PHOTO)"photo-" else "video-")+(s.evidence.size+1),type,"Pixel camera "+type.name.lowercase()+" evidence",System.currentTimeMillis(),uri.toString(),false))}
 )
 Scaffold(
  containerColor=Workspace,
  floatingActionButton={FloatingActionButton(onClick={},containerColor=MekGreen,contentColor=Color.White,shape=CircleShape){Text("MV",fontWeight=FontWeight.Bold)}}
 ){p->
  Column(Modifier.padding(p).fillMaxSize()){
   VehicleHeader(s)
   DiagnosticTabs(tab){tab=it}
   when(tab){
    0->Overview(s)
    1->ScanScreen(s){s=it}
    2->DiagnosisScreen(s,{s=it},evidenceLaunchers)
    3->RepairScreen(s){s=it}
    else->VerifyScreen(s)
   }
  }
 }
}

@Composable private fun VehicleHeader(s:DemoRepairSession){
 Column(Modifier.fillMaxWidth().background(ShopCharcoal).padding(horizontal=20.dp,vertical=18.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("‹",color=Color.White,style=MaterialTheme.typography.headlineMedium);Text(s.roNumber,color=Color.White,fontWeight=FontWeight.Bold);Text("MekView",color=MekGreen,fontWeight=FontWeight.Bold)}
  Text(s.vehicle,color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  Text(s.concern,color=Color.White.copy(alpha=.82f),style=MaterialTheme.typography.bodyMedium)
  Surface(color=MekGreen.copy(alpha=.18f),shape=RoundedCornerShape(6.dp)){Text("DIAGNOSTIC SESSION",Modifier.padding(horizontal=10.dp,vertical=5.dp),color=Color.White,fontWeight=FontWeight.SemiBold)}
 }
}

@Composable private fun DiagnosticTabs(selected:Int,onSelect:(Int)->Unit){
 val labels=listOf("Overview","Scan","Diagnose","Repair","Verify")
 Row(Modifier.fillMaxWidth().background(Color.White)){labels.forEachIndexed{i,label->
  Column(Modifier.weight(1f).clickable{onSelect(i)}.padding(top=14.dp),horizontalAlignment=Alignment.CenterHorizontally){
   Text(label,color=if(i==selected)ShopCharcoal else Muted,fontWeight=if(i==selected)FontWeight.Bold else FontWeight.Normal,style=MaterialTheme.typography.labelMedium)
   Spacer(Modifier.height(11.dp));Box(Modifier.fillMaxWidth().height(4.dp).background(if(i==selected)MekGreen else Color.Transparent))
  }
 }}
}

@Composable private fun Overview(s:DemoRepairSession){
 LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{SectionTitle("Vehicle / RO")}
  item{InfoCard("Repair order",s.roNumber,"Vehicle",s.vehicle)}
  item{SectionTitle("Customer concern")}
  item{Card(Modifier.fillMaxWidth()){Text(s.concern,Modifier.padding(18.dp),style=MaterialTheme.typography.bodyLarge)}}
  item{SectionTitle("Diagnostic progress")}
  item{InfoCard("Scan",if(s.dtcs.isEmpty())"Not started" else s.dtcs.size.toString()+" DTC(s) found","Evidence",s.evidence.size.toString()+" item(s)")}
  item{Text("Tekmetric • Autel • Repair information are simulated in this alpha.",color=Muted,style=MaterialTheme.typography.bodySmall)}
 }
}

@Composable private fun ScanScreen(s:DemoRepairSession,set:(DemoRepairSession)->Unit){
 LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{SectionTitle("Vehicle scan")}
  item{Card(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Autel scan data",fontWeight=FontWeight.Bold);Text("SIMULATED",color=Muted);s.dtcs.forEach{Text(it.code+"  •  "+it.description)}}}}
  item{Button(onClick={set(s.copy(dtcs=DemoData.scanResults))},Modifier.fillMaxWidth()){Text(if(s.dtcs.isEmpty())"SCAN VEHICLE" else "RESCAN VEHICLE")}}
 }
}

@Composable private fun DiagnosisScreen(s:DemoRepairSession,set:(DemoRepairSession)->Unit,evidence:EvidenceLaunchers){
 LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{SectionTitle("Guided diagnosis")}
  if(s.dtcs.isEmpty()) item{NoticeCard("Run a vehicle scan before beginning diagnosis.")}
  else if(s.diagnosticSteps.isEmpty()) item{Button(onClick={set(s.copy(diagnosticSteps=DemoGuidedDiagnosis.misfireLean().steps))},Modifier.fillMaxWidth()){Text("START GUIDED DIAGNOSIS")}}
  if(s.diagnosticSteps.isNotEmpty()) item{NoticeCard("SIMULATED TRAINING PROCEDURE • NOT VERIFIED")}
  items(s.diagnosticSteps.size){i->
   val step=s.diagnosticSteps[i]
   Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
    Row(verticalAlignment=Alignment.CenterVertically){StatusDot(step.status);Spacer(Modifier.width(10.dp));Text("Step "+step.id,fontWeight=FontWeight.Bold)}
    Text(step.instruction)
    if(i==s.activeStep) Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
     Button(onClick={val n=s.diagnosticSteps.toMutableList();n[i]=step.copy(status=StepStatus.PASSED,technicianResult="Technician confirmed");set(s.copy(diagnosticSteps=n,activeStep=i+1))}){Text("PASS")}
     OutlinedButton(onClick={val n=s.diagnosticSteps.toMutableList();n[i]=step.copy(status=StepStatus.FAILED,technicianResult="Fault found");set(s.copy(diagnosticSteps=n,activeStep=i+1))}){Text("FAULT FOUND")}
    } else step.technicianResult?.let{Text(it,color=Muted)}
   }}
  }
  if(s.diagnosticSteps.isNotEmpty()){
   item{OutlinedButton(onClick=evidence.takePhoto,Modifier.fillMaxWidth()){Text("ADD PHOTO EVIDENCE")}}
   item{OutlinedButton(onClick=evidence.takeVideo,Modifier.fillMaxWidth()){Text("ADD VIDEO EVIDENCE")}}
  }
 }
}

@Composable private fun RepairScreen(s:DemoRepairSession,set:(DemoRepairSession)->Unit){
 LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{SectionTitle("Repair")}
  item{InfoCard("Evidence",s.evidence.size.toString()+" item(s)","Finding",s.finding?:"Not confirmed")}
  if(s.evidence.isNotEmpty()&&s.finding==null) item{Button(onClick={set(s.copy(finding="Failure documented with "+s.evidence.size+" evidence item(s). Ready for estimate review."))},Modifier.fillMaxWidth()){Text("CONFIRM FAILED COMPONENT")}}
  s.finding?.let{item{Button(onClick={set(s.copy(estimateStatus="Estimate draft ready for service-advisor review. No customer communication or parts order submitted."))},Modifier.fillMaxWidth()){Text("PREPARE CLIENT QUOTE")}}}
  s.estimateStatus?.let{item{NoticeCard(it)}}
 }
}

@Composable private fun VerifyScreen(s:DemoRepairSession){
 LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{SectionTitle("Verify repair")}
  item{Card(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Post-repair verification",fontWeight=FontWeight.Bold);Text(if(s.estimateStatus==null)"Complete diagnosis and repair workflow first." else "Ready for post-scan and technician verification.",color=Muted)}}}
 }
}

@Composable private fun SectionTitle(t:String){Text(t,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
@Composable private fun NoticeCard(t:String){Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF8E1))){Text(t,Modifier.padding(16.dp),color=Color(0xFF725B00))}}
@Composable private fun InfoCard(a:String,av:String,b:String,bv:String){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(a,color=Muted);Text(av,fontWeight=FontWeight.SemiBold);HorizontalDivider();Text(b,color=Muted);Text(bv,fontWeight=FontWeight.SemiBold)}}}
@Composable private fun StatusDot(status:StepStatus){val c=when(status){StepStatus.PASSED->Color(0xFF159447);StepStatus.FAILED->Color(0xFFD34A3A);else->Color(0xFFE4A900)};Box(Modifier.size(22.dp).background(c,CircleShape))}
