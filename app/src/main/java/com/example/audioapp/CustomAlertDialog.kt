package com.example.audioapp


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomAlertDialog(file: File, setShowDialog: (Boolean) -> Unit, mainScreenviewModel: MainScreenViewModel = viewModel()) {

    var mText by remember { mutableStateOf(TextFieldValue(file.nameWithoutExtension)) }
    val mNewFileNameIsCorrect = mainScreenviewModel.mNewFileNameIsCorrect.observeAsState()

    Dialog(
        onDismissRequest = { mainScreenviewModel.mNewFileNameIsCorrect.value = true; setShowDialog(false) },
        properties = DialogProperties(usePlatformDefaultWidth = false), // <- make wrap new content size und resize Dialog Box
        content = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = Color.White,
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    OutlinedTextField(
                        value = mText,
                        label = { Text(text = "Enter new Name") },
                        onValueChange = { mText = it; mainScreenviewModel.mNewFileNameIsCorrect.value = true },
                        placeholder = { Text(text = "Enter new Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (mNewFileNameIsCorrect.value == false)
                        Text(
                            text = "Incorrect file name. Use only Letters, Numbers, Space, '-' and '_'",
                            color = Color.Red
                        )

                    Row(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {
                        Button(onClick = {mainScreenviewModel.mNewFileNameIsCorrect.value = true; setShowDialog(false) })
                        {
                            Text(text = "Cancel")
                            println("-> ${mainScreenviewModel.mNewFileNameIsCorrect.value}")
                        }

                        Button(
                            onClick = { if (mainScreenviewModel.renameRecord(mText.text)) setShowDialog(false) }
                        ) {
                            Text(text = "Rename")
                        }
                    }
                }

            }
        }
    )

}
