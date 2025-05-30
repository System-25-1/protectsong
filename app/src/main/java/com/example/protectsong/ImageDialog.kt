package com.example.protectsong

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

class ImageDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val imageView = ImageView(requireContext())
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        dialog?.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.BLACK))

        val url = arguments?.getString("imageUrl") ?: ""
        Glide.with(this).load(url).into(imageView)

        imageView.setOnClickListener { dismiss() }

        return imageView
    }

    companion object {
        fun newInstance(url: String): ImageDialog {
            val args = Bundle().apply { putString("imageUrl", url) }
            return ImageDialog().apply { arguments = args }
        }
    }
}
