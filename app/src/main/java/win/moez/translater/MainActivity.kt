package win.moez.translater

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpeechTranslationApp()
        }
    }
}

@Composable
fun SpeechTranslationApp() {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var targetLanguage by remember { mutableStateOf("zh") }
    var selectedLanguages by remember { mutableStateOf(setOf("en", "zh", "ja", "fr", "de")) }
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "需要录音权限", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isRecording = false
            }

            override fun onError(error: Int) {
                isRecording = false
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                recognizedText = text

                coroutineScope.launch {
                    if (targetLanguage !in selectedLanguages) {
                        translatedText = text
                    } else {
                        translatedText = GoogleTranslateAPI.translate(text, targetLanguage)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        onDispose {
            speechRecognizer.destroy()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 语言选择按钮（左侧）
            Button(
                onClick = { showLanguageSelectionDialog(selectedLanguages) },
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp),
                shape = CircleShape
            ) {
                Text("语言")
            }

            // 录音按钮（中间）
            Button(
                onClick = {
                    if (isRecording) {
                        speechRecognizer.stopListening()
                    } else {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().language)
                        }
                        speechRecognizer.startListening(intent)
                    }
                    isRecording = !isRecording
                },
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color.Red else Color.Blue)
            ) {
                Text(if (isRecording) "停止" else "录音")
            }

            // 目标语言切换按钮（右侧）
            Button(
                onClick = { targetLanguage = if (targetLanguage == "zh") "en" else "zh" },
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp),
                shape = CircleShape
            ) {
                Text(if (targetLanguage == "zh") "EN" else "ZH")
            }
        }

        // 识别文本框
        TextBox(value = recognizedText, label = "识别文本")

        // 翻译文本框
        TextBox(value = translatedText, label = "翻译结果")
    }
}

@Composable
fun TextBox(value: String, label: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth().weight(1f),
            textAlign = TextAlign.Center
        )
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}

fun showLanguageSelectionDialog(selectedLanguages: Set<String>) {
    // TODO: 弹出多选对话框，支持全选/反选
}
