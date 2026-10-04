package ca.autoworks.techlense.evidence
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
object EvidenceCapture {
 fun newEvidenceUri(context: Context, type: EvidenceMediaType): Uri {
  val directory=File(context.filesDir,"evidence").apply{mkdirs()}
  val extension=if(type==EvidenceMediaType.PHOTO)"jpg" else "mp4"
  val file=File(directory,"evidence_"+System.currentTimeMillis()+"."+extension)
  return FileProvider.getUriForFile(context,context.packageName+".fileprovider",file)
 }
}
