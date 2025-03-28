package win.moez.translater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 录音和语言选择按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { /* 语言过滤逻辑 */ },
                modifier = Modifier
                    .width(100.dp)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("语言过滤", fontSize = 14.sp, color = Color.White)
            }

            Button(
                onClick = { isRecording = !isRecording },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            Color.White,
                            shape = if (isRecording) RoundedCornerShape(4.dp) else CircleShape
                        )
                )
            }

            Button(
                onClick = { /* 目标语言切换逻辑 */ },
                modifier = Modifier
                    .width(80.dp)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("目标语言", fontSize = 14.sp, color = Color.White)
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
