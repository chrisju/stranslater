package win.moez.translater

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedLanguages by remember { mutableStateOf(setOf("en", "zh", "ja", "fr", "de")) }
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val coroutineScope = rememberCoroutineScope()

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

    DisposableEffect(Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isRecording = false }
            override fun onError(error: Int) { isRecording = false }

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

        onDispose { speechRecognizer.destroy() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 按钮行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 语言过滤按钮，只翻译这些语言（多选语言）
            Button(
                onClick = { showLanguageDialog = true },
                modifier = Modifier
                    .width(80.dp)
                    .height(40.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(if (targetLanguage == "zh") "EN" else "ZH", fontSize = 14.sp, color = Color.White)
            }

            // 录音按钮（动态样式）
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
                    .size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color.White, shape = if (isRecording) RoundedCornerShape(4.dp) else CircleShape)
                )
            }

            // 目标语言（单选选语言）
            Button(
                onClick = { targetLanguage = if (targetLanguage == "zh") "en" else "zh" },
                modifier = Modifier
                    .width(80.dp)
                    .height(40.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(if (targetLanguage == "zh") "EN" else "ZH", fontSize = 14.sp, color = Color.White)
            }
        }

        // 识别文本框 + 翻译文本框（等高）
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = recognizedText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            )

            OutlinedTextField(
                value = translatedText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            )
        }
    }

    // 语言选择弹窗
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguages = selectedLanguages,
            onDismiss = { showLanguageDialog = false },
            onConfirm = { selectedLanguages = it }
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    selectedLanguages: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val languages = listOf("en", "zh", "ja", "fr", "de")
    var selected by remember { mutableStateOf(selectedLanguages) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onConfirm(selected); onDismiss() }) {
                Text("确认")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = { Text("选择翻译语言") },
        text = {
            Column {
                languages.forEach { lang ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = selected.toggle(lang) }
                    ) {
                        Checkbox(checked = lang in selected, onCheckedChange = { selected = selected.toggle(lang) })
                        Text(text = lang)
                    }
                }
            }
        }
    )
}

fun Set<String>.toggle(item: String) = if (item in this) this - item else this + item
