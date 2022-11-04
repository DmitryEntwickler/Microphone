package com.example.audioapp

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreenComposable(mMainScreenViewModel: MainScreenViewModel = viewModel()) {

    val mRecordState by mMainScreenViewModel.mRecordState.observeAsState()
    val mIsPlaying by mMainScreenViewModel.mIsPlaying.observeAsState()
    val mPlayingMassage by mMainScreenViewModel.mPlayingMessage.observeAsState()
    val mPlayState by mMainScreenViewModel.mPlayState.observeAsState()
    val mTime by mMainScreenViewModel.mTime.observeAsState()
    val mOutputDir = mMainScreenViewModel.mOutputDir
    val mListOfFiles by mMainScreenViewModel.mListOfFiles.observeAsState()
    val mListState = rememberLazyListState() // for control and observe scrolling
    val mSelectedFile by mMainScreenViewModel.mSelectedFile.observeAsState()
    val mFileToDelete by mMainScreenViewModel.mFileToDelete.observeAsState()

    Column(modifier = Modifier
        .fillMaxWidth()
        .background(color = colorResource(id = R.color.background))) {
        Text(
            text = "Microphone",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            fontSize = 32.sp,
            color = colorResource(id = R.color.textColor),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = {
                mMainScreenViewModel.buttonClickMic()
                mMainScreenViewModel.mSelectedFile.value = null
                      },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.buttonColor))
        )
        {
            Text(
                text = if (mRecordState == "OFF") "Start" else "Stop",
                fontSize = 32.sp,
                color = colorResource(id = R.color.buttonTextColor)
            )
        }


        mRecordState?.let {
            if (it.equals("ON")) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_mic_24),
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(48.dp),
                    tint = colorResource(id = R.color.textColor),
                    contentDescription = null // decorative element
                )
                mRecordState?.let {
                    Text(
                        text = it,
                        modifier = Modifier
                            .fillMaxWidth(),
                        fontSize = 24.sp,
                        color = colorResource(id = R.color.textColor),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_mic_off_24),
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(48.dp),
                    tint = colorResource(id = R.color.textColor),
                    contentDescription = null // decorative element
                )
                Text(
                    text = it,
                    modifier = Modifier
                        .fillMaxWidth(),
                    fontSize = 24.sp,
                    color = colorResource(id = R.color.textColor),
                    textAlign = TextAlign.Center
                )
            }

        }

        // Record Time
        Text(
            mTime.toString(),
            modifier = Modifier
                .fillMaxWidth(),
            fontSize = 24.sp,
            color = colorResource(id = R.color.textColor),
            textAlign = TextAlign.Center
        )

        // Record Dir
        Text(
            text = "Record to: $mOutputDir",
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            fontSize = 14.sp,
            color = colorResource(id = R.color.textColor),
            textAlign = TextAlign.Center
        )
        // disk size

        Text(
            text = "Free Space on Handy: ${(mOutputDir.freeSpace) / (1024*1024*1024)} /  ${(mOutputDir.totalSpace) / (1024*1024*1024)} GB",
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            fontSize = 14.sp,
            color = colorResource(id = R.color.textColor),
            textAlign = TextAlign.Center
        )

        // Play Button
        Button(
            onClick = { mMainScreenViewModel.playAudio() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.buttonColor))
        )
        {
            Text(
                text =  if (mPlayState == "OFF") {
                        if (mSelectedFile != null && mSelectedFile?.extension == "mp3") "Play: ${mSelectedFile?.name}"  // File is selected
                        else "Play" // File si not selected
                    } else "Stop",
                fontSize = if (mMainScreenViewModel.mSelectedFile.value == null) 32.sp else 18.sp,
                color = colorResource(id = R.color.buttonTextColor)
            )
        }
        // Playing Message
        mPlayingMassage?.let {
            Text(
                text = it,
                modifier = Modifier
                    .fillMaxWidth(),
                fontSize = 32.sp,
                color = colorResource(id = R.color.textColor),
                textAlign = TextAlign.Center
            )
        }


        LazyColumn(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp),
            state = mListState,
        ) {

            stickyHeader {
                Text(
                    text = "Files in Dir: ${mListOfFiles.orEmpty().size}",
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.textColor)
                )
            }

            items(mListOfFiles.orEmpty().sortedBy { it.lastModified() }.sortedDescending()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { mMainScreenViewModel.mSelectedFile.value = it },
                            onLongClick = {
                                mMainScreenViewModel.mFileToDelete.value = it
                                mMainScreenViewModel.deleteRecord()
                                mMainScreenViewModel.mSelectedFile.value = null
                            }

                        )
                ) {
                    Image(
                        painter =
                        if (it.extension == "mp3") painterResource(id = R.drawable.ic_baseline_mic_24)
                        else if (it.extension == "txt") painterResource(id = R.drawable.text_format_24)
                        else painterResource(id = R.drawable.device_unknown_24),
                        contentDescription = "Typ image",
                        modifier = Modifier
                            .size(22.dp),
                        colorFilter = ColorFilter.tint(color = colorResource(id = R.color.textColor))
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = it.name,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(start = 16.dp),
                            color = colorResource(id = R.color.textColor)
                        )
                        Text(
                            text = " ${(it.length()/1000).toFloat()} KB",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(start = 16.dp),
                            color = colorResource(id = R.color.textColor)
                        )
                    }


                }
                Divider(color = colorResource(id = R.color.textColor))
            }
        } // end of Lazy Column


    } // end of column

}