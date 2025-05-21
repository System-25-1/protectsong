package com.example.protectsong

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class VideoPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        val videoView = findViewById<VideoView>(R.id.videoView)
        val videoUrl = intent.getStringExtra("videoUrl")

        if (videoUrl.isNullOrEmpty()) {
            Toast.makeText(this, "재생할 영상이 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val uri = Uri.parse(videoUrl)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)

        videoView.setMediaController(mediaController)
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { videoView.start() }
        videoView.setOnErrorListener { _, what, extra ->
            Toast.makeText(this, "영상 재생 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
