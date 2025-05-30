package com.example.protectsong

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class VideoPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        val videoView = findViewById<VideoView>(R.id.videoView)
        val loading = findViewById<ProgressBar>(R.id.loadingIndicator)
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

        loading.visibility = View.VISIBLE

        videoView.setOnPreparedListener {
            loading.visibility = View.GONE
            videoView.start()
        }

        videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "영상 재생 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
