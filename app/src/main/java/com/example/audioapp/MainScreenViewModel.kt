package com.example.audioapp

import android.annotation.SuppressLint
import android.app.Application
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.io.File
import java.util.*


class MainScreenViewModel(application: Application) : AndroidViewModel(application) {
    // Recorder
    var mOutputFile: String = ""
    val mOutputDir: File =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val mListOfFiles = MutableLiveData(listOf<File>())
    var mMediaRecorder: MediaRecorder? = MediaRecorder()
    val mRecordState = MutableLiveData("OFF")
    val mTime = MutableLiveData(0)
    var mJob: Job = Job()


    // Player
    @SuppressLint("StaticFieldLeak")
    val mContext = getApplication<Application>().applicationContext
    val mSelectedFile = MutableLiveData<File>()
    val mFileToHandle = MutableLiveData<File>()
    var mediaPlayer= MutableLiveData<MediaPlayer?>()
    val mPlayState = MutableLiveData("OFF")
    val mIsPlaying = mediaPlayer.map { it?.setOnCompletionListener { mPlayState.value = "OFF"; println("-> completed! State is ${mPlayState.value}") } }
    val mPlayingMessage = mPlayState.map { if (it == "OFF") null else "playing..." }

    // Custom Alert Dialog
    val mTextFieldText = mFileToHandle.map { it.name }

    init {
        mListOfFiles.value = mOutputDir.listFiles().toList()
    }

    fun buttonClickMic() {
        if (mRecordState.value == "OFF") startMic()
        else stopMic()
    }

    private fun startMic() {
        val now = Date()
        val fileName = String.format("/rec %tF %tH_%tM_%tS.mp3", now, now, now, now)
        println("-> $fileName")
        mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mOutputFile =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + fileName
        mMediaRecorder?.setOutputFile(mOutputFile)
        println("-> start")
        mMediaRecorder?.prepare()
        mMediaRecorder?.start()
        mRecordState.value = "ON"
        startTime()
    }

    private fun stopMic() {
        println("-> stop")
        mMediaRecorder?.stop()
        mMediaRecorder?.reset()
        mRecordState.value = "OFF"
        mJob.cancel()
        mListOfFiles.value = mOutputDir.listFiles().toList()
    }

    private fun startTime() {
        mTime.value = 0
        mJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                withContext(Dispatchers.Main) {
                    mTime.value = mTime.value?.plus(1)
                }
            }
        }
    }

    fun playAudio() {
        println("-> button clicked. State was ${mPlayState.value}")

        if (mPlayState.value == "OFF") {
            if (mSelectedFile.value != null && mSelectedFile.value?.extension == "mp3") {
                mediaPlayer.value = MediaPlayer.create(mContext, mSelectedFile.value?.toUri())
                mediaPlayer.value?.start()
                println("-> Starting player")
                mPlayState.value = "ON"
                println("-> State = ${mPlayState.value}")
            }
        } else {
            println("-> Stoping player")
            mediaPlayer.value?.stop()
            mPlayState.value = "OFF"
            println("-> State = ${mPlayState.value}")
        }


    }

    fun deleteRecord(){
        println("-> LongClick")
        mFileToHandle.value?.delete()
        println("-> deleteok")
        mListOfFiles.value = mOutputDir.listFiles().toList()
    }

    fun checkMP3(): Boolean{
        println("-> check: ${mFileToHandle.value?.extension } ")
        return mFileToHandle.value?.extension == "mp3"
    }

    fun renameRecord(newName: String): Boolean{
        println("-> VM: rename")
        if (newName.isNotEmpty() && isAlphaNumeric(newName)){
            val newFileString =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/$newName.mp3"
            val newFile = File(newFileString)
            mFileToHandle.value?.renameTo(newFile)
            println("-> rename ok")
            mListOfFiles.value = mOutputDir.listFiles().toList()
            return true
        }
        else {println("-> invalid name"); return false}

    }

    fun getNewFiles(){
        println("-> get all Files")
        mListOfFiles.value = mOutputDir.listFiles().toList()
    }

    fun isAlphaNumeric(string: String): Boolean {
        for (c in string)
        {
            if (c !in 'A'..'Z' && c !in 'a'..'z' && c!in '0'..'9') {
                return false
            }
        }
        return true
    }

} // end of ViewModel