package ca.autoworks.techlense

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.meta.wearable.dat.core.Wearables
import com.meta.wearable.dat.core.types.RegistrationState
import com.meta.wearable.dat.core.types.Permission
import com.meta.wearable.dat.core.types.PermissionStatus
import com.meta.wearable.dat.core.selectors.AutoDeviceSelector
import com.meta.wearable.dat.core.session.DeviceSession
import com.meta.wearable.dat.core.session.DeviceSessionState
import com.meta.wearable.dat.camera.Camera
import com.meta.wearable.dat.camera.addCamera
import com.meta.wearable.dat.camera.types.StreamConfiguration
import com.meta.wearable.dat.camera.types.StreamState
import com.meta.wearable.dat.camera.types.VideoQuality
import com.meta.wearable.dat.camera.types.PhotoData
import com.meta.wearable.dat.inputs.addInputs
import com.meta.wearable.dat.inputs.types.InputEvent
import com.meta.wearable.dat.inputs.types.InputsConfiguration
import com.meta.wearable.dat.speech.Speech
import com.meta.wearable.dat.speech.addSpeech
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ca.autoworks.techlense.demo.*
import ca.autoworks.techlense.diagnostics.*
import ca.autoworks.techlense.evidence.*

private val MekGreen=Color(0xFF43B83E)
private val ShopCharcoal=Color(0xFF223136)
private val Workspace=Color(0xFFF3F4F4)
private val Muted=Color(0xFF667176)

class MainActivity:ComponentActivity(){
 private val _glassesStatus=MutableStateFlow("Ready to start camera session")
 val glassesStatus:StateFlow<String> = _glassesStatus
 private var deviceSession:DeviceSession?=null
 private var glassesCamera:Camera?=null
 private var sessionJob:Job?=null
 private var streamJob:Job?=null
 private var inputsJob:Job?=null
 private var speechJob:Job?=null
 private var glassesSpeech:Speech?=null
 private var pendingCameraStart=false
 private val _lastGlassesAction=MutableStateFlow("No capture yet")
 val lastGlassesAction:StateFlow<String> = _lastGlassesAction

 private val datPermissions=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){grants->
  if(grants.values.all{it}) Wearables.initialize(this)
 }
 private val cameraPermission=registerForActivityResult(Wearables.RequestPermissionContract()){result->
  result.onSuccess{status->if(status==PermissionStatus.Granted) startGlassesSession() else _glassesStatus.value="Camera permission not granted"}
   .onFailure{error,_-> _glassesStatus.value="Camera permission error: "+error.description}
 }
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{
  MaterialTheme(colorScheme=lightColorScheme(primary=MekGreen,surface=Color.White,background=Workspace)){MekViewAlpha()}
 }}
 override fun onStart(){super.onStart();val required=arrayOf(Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_CONNECT);if(required.all{ContextCompat.checkSelfPermission(this,it)==PackageManager.PERMISSION_GRANTED}) Wearables.initialize(this) else datPermissions.launch(required)}

 fun beginGlassesCamera(){
  _glassesStatus.value="Checking glasses camera permission…"
  lifecycleScope.launch{
   Wearables.checkPermissionStatus(Permission.CAMERA)
    .onSuccess{status->if(status==PermissionStatus.Granted) startGlassesSession() else cameraPermission.launch(Permission.CAMERA)}
    .onFailure{error,_-> _glassesStatus.value="Permission check failed: "+error.description}
  }
 }
 private fun startGlassesSession(){
  if(deviceSession!=null){_glassesStatus.value="Glasses session already active";return}
  _glassesStatus.value="Starting glasses session…"
  Wearables.createSession(AutoDeviceSelector())
   .onSuccess{session->
    deviceSession=session
    sessionJob=lifecycleScope.launch{
     session.state.collect{state->
      _glassesStatus.value="Session: "+state.name.lowercase().replaceFirstChar{it.uppercase()}
      if(state==DeviceSessionState.STARTED){
       if(glassesCamera==null) attachGlassesCamera(session)
       attachGlassesInputs(session)
      }
     }
    }
    session.start()
   }
   .onFailure{error,_-> _glassesStatus.value="Session failed: "+error.description}
 }
 private fun attachGlassesInputs(session:DeviceSession){
  if(inputsJob!=null)return
  session.addInputs(InputsConfiguration(consumeBack=false))
   .onSuccess{inputs->inputsJob=lifecycleScope.launch{inputs.events.collect{event->if(event is InputEvent.Capture) captureInspectionPhoto("Glasses button")}}}
   .onFailure{error,_-> _lastGlassesAction.value="Capture button unavailable: "+error.description}
 }
 fun enableHandsFreeCommands(){
  val session=deviceSession?:run{_lastGlassesAction.value="Start the glasses session first";return}
  lifecycleScope.launch{
   Wearables.checkPermissionStatus(Permission.MICROPHONE).onSuccess{status->
    if(status!=PermissionStatus.Granted){_lastGlassesAction.value="Microphone permission required in Meta AI";return@onSuccess}
    if(glassesSpeech!=null)return@onSuccess
    session.addSpeech().onSuccess{speech->
     glassesSpeech=speech
     speechJob=lifecycleScope.launch{speech.transcriptions.collect{result->
      if(result!=null && result.isFinal) handleGlassesCommand(result.text)
     }}
     speech.start().onSuccess{_lastGlassesAction.value="Hands-free commands listening"}.onFailure{error,_-> _lastGlassesAction.value="Speech failed: "+error.description}
    }.onFailure{error,_-> _lastGlassesAction.value="Speech unavailable: "+error.description}
   }.onFailure{error,_-> _lastGlassesAction.value="Microphone check failed: "+error.description}
  }
 }
 private fun handleGlassesCommand(text:String){
  val command=text.trim().lowercase()
  when{
   command.contains("take")&&command.contains("photo")->captureInspectionPhoto("Voice")
   command.contains("take")&&command.contains("picture")->captureInspectionPhoto("Voice")
   command.contains("scan")&&command.contains("vin")->_lastGlassesAction.value="VIN scan command recognized • capture/OCR is next"
   command.contains("start")&&command.contains("video")->_lastGlassesAction.value="Start video command recognized • recorder is next"
   command.contains("stop")&&command.contains("video")->_lastGlassesAction.value="Stop video command recognized"
   else->_lastGlassesAction.value="Heard: "+text
  }
 }
 private fun captureInspectionPhoto(source:String){
  val camera=glassesCamera?:run{_lastGlassesAction.value="Camera is not streaming";return}
  lifecycleScope.launch{
   camera.stream.capturePhoto().onSuccess{photo->
    val kind=when(photo){is PhotoData.Bitmap->"photo";is PhotoData.HEIC->"HEIC photo"}
    _lastGlassesAction.value="$source captured $kind for active inspection"
   }.onFailure{error,_-> _lastGlassesAction.value="Photo capture failed: "+error.description}
  }
 }
 private fun attachGlassesCamera(session:DeviceSession){
  if(pendingCameraStart||glassesCamera!=null)return
  pendingCameraStart=true
  session.addCamera(StreamConfiguration(videoQuality=VideoQuality.MEDIUM,frameRate=15))
   .onSuccess{camera->
    pendingCameraStart=false;glassesCamera=camera
    streamJob=lifecycleScope.launch{camera.stream.state.collect{state->_glassesStatus.value=when(state){StreamState.STREAMING->"POV camera streaming";StreamState.PAUSED->"POV camera paused";else->"Camera: "+state.name.lowercase().replaceFirstChar{it.uppercase()}}}}
    camera.stream.start().onFailure{error,_-> _glassesStatus.value="Camera stream failed: "+error.description}
   }
   .onFailure{error,_->pendingCameraStart=false;_glassesStatus.value="Camera setup failed: "+error.description}
 }
 fun stopGlassesCamera(){
  glassesCamera?.stop();glassesCamera=null;deviceSession?.stop();deviceSession=null
  sessionJob?.cancel();streamJob?.cancel();inputsJob?.cancel();inputsJob=null;speechJob?.cancel();speechJob=null;glassesSpeech=null;_glassesStatus.value="Camera session stopped"
 }
 override fun onDestroy(){stopGlassesCamera();super.onDestroy()}
}

@Composable private fun MekViewAlpha(){
 var selected by remember{mutableStateOf<DemoRepairSession?>(null)}
 if(selected==null){
  JobBoard{selected=it}
 } else {
  VehicleWorkspace(selected!!,onBack={selected=null},onSessionChange={selected=it})
 }
}

private data class DemoJob(val session:DemoRepairSession,val customer:String,val status:String,val dropOff:String="Drop Off")

@Composable private fun JobBoard(onSelect:(DemoRepairSession)->Unit){
 val jobs=remember{listOf(
  DemoJob(DemoRepairSession(),"Alex Martin","Diagnostic"),
  DemoJob(DemoRepairSession(roNumber="RO #18427",vehicle="2018 Chevrolet Silverado 1500",vin="3GCUKREC0JG000027",mileage="154,200 km",concern="Brake vibration and front-end noise"),"Jordan Lee","Work in Progress"),
  DemoJob(DemoRepairSession(roNumber="RO #18419",vehicle="2021 Toyota RAV4",vin="2T3R1RFV8MW000019",mileage="82,100 km",concern="Maintenance inspection and tire concern"),"Taylor Chen","Estimate"),
  DemoJob(DemoRepairSession(roNumber="RO #18411",vehicle="2017 Honda CR-V",vin="2HKRW2H80HH000011",mileage="139,800 km",concern="A/C not cooling"),"Morgan Davis","Estimate")
 )}
 var boardTab by remember{mutableIntStateOf(0)}
 var stage by remember{mutableIntStateOf(0)}
 val stages=listOf("Estimates","Work In Progress","Completed")
 val filtered=when(stage){0->jobs.filter{it.status=="Estimate"||it.status=="Diagnostic"};1->jobs.filter{it.status=="Work in Progress"};else->emptyList()}
 Column(Modifier.fillMaxSize().background(Workspace)){
  Column(Modifier.fillMaxWidth().background(ShopCharcoal).padding(start=18.dp,top=52.dp,end=18.dp)){
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
    Text("☰",color=Color.White,style=MaterialTheme.typography.headlineMedium)
    Row(Modifier.background(Color(0xFF3B4D52),RoundedCornerShape(10.dp)).padding(4.dp)){
     listOf("Job Board","My Work").forEachIndexed{i,t->Surface(color=if(boardTab==i)MekGreen else Color.Transparent,shape=RoundedCornerShape(8.dp),modifier=Modifier.clickable{boardTab=i}){Text(t,Modifier.padding(horizontal=20.dp,vertical=10.dp),color=Color.White,fontWeight=FontWeight.SemiBold)}}
    }
    Image(painter=painterResource(R.drawable.mekview_logo),contentDescription="MekView",modifier=Modifier.size(36.dp))
   }
   Spacer(Modifier.height(18.dp))
   Row(Modifier.fillMaxWidth()){stages.forEachIndexed{i,t->Column(Modifier.weight(1f).clickable{stage=i},horizontalAlignment=Alignment.CenterHorizontally){Text(t,color=if(stage==i)Color.White else Color.White.copy(alpha=.5f),fontWeight=if(stage==i)FontWeight.Bold else FontWeight.Normal,style=MaterialTheme.typography.labelLarge);Spacer(Modifier.height(10.dp));Box(Modifier.fillMaxWidth().height(4.dp).background(if(stage==i)MekGreen else Color.Transparent))}}}
  }
  Row(Modifier.fillMaxWidth().background(Color.White).padding(14.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){
   AssistChip(onClick={},label={Text("Search")});AssistChip(onClick={},label={Text("Technician ▾")});AssistChip(onClick={},label={Text("RO Status ▾")})
  }
  if(filtered.isEmpty()) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("No jobs in this stage",color=Muted)}
  else LazyColumn(Modifier.fillMaxSize().padding(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   items(filtered.size){i->val j=filtered[i];Card(Modifier.fillMaxWidth().clickable{onSelect(j.session)},colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(j.session.vehicle,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(j.session.roNumber,color=Muted,fontWeight=FontWeight.SemiBold)}
    Text(j.dropOff+"  •  "+j.customer,color=Muted)
    Text(j.session.concern,maxLines=2,style=MaterialTheme.typography.bodyMedium)
    Surface(color=if(j.status=="Estimate")Color(0xFFE8F0FF) else MekGreen.copy(alpha=.14f),shape=RoundedCornerShape(6.dp)){Text(j.status,Modifier.padding(horizontal=10.dp,vertical=5.dp),color=ShopCharcoal,fontWeight=FontWeight.SemiBold)}
   }}}
  }
 }
}

@Composable private fun VehicleWorkspace(initial:DemoRepairSession,onBack:()->Unit,onSessionChange:(DemoRepairSession)->Unit){
 var s by remember(initial.roNumber){mutableStateOf(initial)}
 var tab by remember{mutableIntStateOf(0)}
 val context=LocalContext.current
 val evidenceLaunchers=rememberEvidenceLaunchers(
  createUri={type->EvidenceCapture.newEvidenceUri(context,type)},
  onCaptured={type,uri->s=s.copy(evidence=s.evidence+InspectionEvidence((if(type==EvidenceMediaType.PHOTO)"photo-" else "video-")+(s.evidence.size+1),type,"Pixel camera "+type.name.lowercase()+" evidence",System.currentTimeMillis(),uri.toString(),false))}
 )
 DisposableEffect(s){onDispose{onSessionChange(s)}}
 Scaffold(containerColor=Workspace,floatingActionButton={FloatingActionButton(onClick={},containerColor=MekGreen,contentColor=Color.White,shape=CircleShape){Text("MV",fontWeight=FontWeight.Bold)}}){p->
  Column(Modifier.padding(p).fillMaxSize()){
   VehicleHeader(s,onBack)
   DiagnosticTabs(tab){tab=it}
   when(tab){0->Overview(s);1->ScanScreen(s){s=it};2->DiagnosisScreen(s,{s=it},evidenceLaunchers);3->RepairScreen(s){s=it};else->VerifyScreen(s)}
  }
 }
}

@Composable private fun VehicleHeader(s:DemoRepairSession,onBack:()->Unit){
 Column(Modifier.fillMaxWidth().background(ShopCharcoal).padding(horizontal=20.dp,vertical=18.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("‹",Modifier.clickable{onBack()},color=Color.White,style=MaterialTheme.typography.headlineMedium);Text(s.roNumber,color=Color.White,fontWeight=FontWeight.Bold);Text("MekView",color=MekGreen,fontWeight=FontWeight.Bold)}
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
  item{GlassesCard()}
  item{Text("Tekmetric • Autel • Repair information are simulated in this alpha.",color=Muted,style=MaterialTheme.typography.bodySmall)}
 }
}

@Composable private fun GlassesCard(){
 val activity=LocalContext.current as? MainActivity
 val registration by Wearables.registrationState.collectAsState()
 val devices by Wearables.devices.collectAsState()
 val cameraStatus by (activity?.glassesStatus?:MutableStateFlow("Unavailable")).collectAsState()
 val lastAction by (activity?.lastGlassesAction?:MutableStateFlow("Unavailable")).collectAsState()
 val cameraActive=cameraStatus.contains("streaming",ignoreCase=true)||cameraStatus.startsWith("Camera:")||cameraStatus.startsWith("Session:")
 Card(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
  Text("MekView Glasses",fontWeight=FontWeight.Bold)
  Text(when(registration){RegistrationState.REGISTERED->if(devices.isEmpty()) "Registered • No glasses detected" else "Registered • "+devices.size+" device(s) detected";else->"Not registered with Meta AI"},color=if(registration==RegistrationState.REGISTERED)MekGreen else Muted)
  if(registration!=RegistrationState.REGISTERED) Button(onClick={activity?.let{Wearables.startRegistration(it)}},Modifier.fillMaxWidth()){Text("CONNECT META GLASSES")}
  else if(devices.isNotEmpty()){
   Text(cameraStatus,style=MaterialTheme.typography.bodySmall,color=if(cameraStatus=="POV camera streaming")MekGreen else Muted)
   Button(onClick={if(cameraActive) activity?.stopGlassesCamera() else activity?.beginGlassesCamera()},Modifier.fillMaxWidth()){Text(if(cameraActive)"END GLASSES SESSION" else "START GLASSES SESSION")}
   if(cameraStatus=="POV camera streaming"){
    Button(onClick={activity?.enableHandsFreeCommands()},Modifier.fillMaxWidth()){Text("ENABLE HANDS-FREE COMMANDS")}
    Text("Capture button: photo • Voice: take photo / scan VIN / start video",style=MaterialTheme.typography.bodySmall,color=Muted)
    Text(lastAction,style=MaterialTheme.typography.bodySmall,color=Muted)
   }
  }
 }}
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
