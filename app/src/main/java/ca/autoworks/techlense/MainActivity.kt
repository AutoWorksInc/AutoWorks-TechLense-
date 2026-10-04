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
import ca.autoworks.techlense.model.EvidenceSource
import ca.autoworks.techlense.model.VehicleSession

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { TechLenseApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TechLenseApp() {
    var vehicle by remember { mutableStateOf(VehicleSession()) }
    var mode by remember { mutableStateOf("DIAGNOSE") }
    Scaffold(topBar = { TopAppBar(title = { Text("AutoWorks TechLense") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("TECHNICIAN COPILOT", style = MaterialTheme.typography.labelLarge)
                Text("Pixel + Ray-Ban Meta Gen 2")
            }
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current vehicle", style = MaterialTheme.typography.titleMedium)
                        Field("VIN", vehicle.vin) { vehicle = vehicle.copy(vin = it) }
                        Field("Year", vehicle.year) { vehicle = vehicle.copy(year = it) }
                        Field("Make", vehicle.make) { vehicle = vehicle.copy(make = it) }
                        Field("Model", vehicle.model) { vehicle = vehicle.copy(model = it) }
                        Field("Engine", vehicle.engine) { vehicle = vehicle.copy(engine = it) }
                        Field("Mileage (km)", vehicle.mileageKm) { vehicle = vehicle.copy(mileageKm = it) }
                        Field("Customer complaint", vehicle.complaint) { vehicle = vehicle.copy(complaint = it) }
                        Field("DTCs", vehicle.dtcs) { vehicle = vehicle.copy(dtcs = it) }
                    }
                }
            }
            item {
                Text("Mode", style = MaterialTheme.typography.titleMedium)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DIAGNOSE", "LOOK / IDENTIFY", "PROCEDURE", "INSPECTION", "NOTES").forEach { choice ->
                        Button(onClick = { mode = choice }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (mode == choice) "• " + choice else choice)
                        }
                    }
                }
            }
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Source guardrails", style = MaterialTheme.typography.titleMedium)
                        EvidenceSource.entries.forEach { source -> Text("• " + source.label) }
                        Text("Critical specs are never marked verified unless supplied by an authorized repair-information source.")
                    }
                }
            }
            item { Text("Glasses: integration foundation installed") }
            item { Text("v0.1.0 • development build", style = MaterialTheme.typography.labelMedium) }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), singleLine = label != "Customer complaint")
}
