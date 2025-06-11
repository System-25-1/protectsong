package com.example.protectsong

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

class FullImageDialog : DialogFragment() {

    companion object {
        fun newInstance(imageUrl: String): FullImageDialog {
            val args = Bundle()
            args.putString("imageUrl", imageUrl)
            val fragment = FullImageDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val imageView = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
            setOnClickListener { dismiss() }  // 클릭 시 닫기
        }

        val imageUrl = arguments?.getString("imageUrl")
        Glide.with(this).load(imageUrl).into(imageView)

        return imageView
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.black)
    }
}
