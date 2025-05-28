package com.example.protectsong.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.protectsong.R
import com.example.protectsong.databinding.ItemSmsReportBinding
import com.example.protectsong.model.Report
import java.text.SimpleDateFormat
import java.util.*

class ReportAdapter : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    private val reports = mutableListOf<Report>()

    fun submitList(newList: List<Report>) {
        reports.clear()
        reports.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ReportViewHolder(private val binding: ItemSmsReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            binding.tvContent.text = report.content ?: "내용 없음"
            binding.tvBuilding.text = report.building ?: "건물 정보 없음"
            binding.tvStatus.text = report.status ?: "상태 없음"
            binding.tvCategory.text = report.type ?: "카테고리 없음" // ✅ 추가

            val formattedDate = report.timestamp?.let {
                SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA).format(it.toDate())
            } ?: "날짜 없음"
            binding.tvDate.text = formattedDate

            val badgeRes = when (report.status) {
                "접수됨" -> R.drawable.bg_badge_received
                "처리중" -> R.drawable.bg_badge_in_progress
                "처리완료" -> R.drawable.bg_badge_completed
                else -> R.drawable.bg_badge_received
            }
            binding.tvStatus.setBackgroundResource(badgeRes)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemSmsReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        Log.d("ReportAdapter", "바인딩 중: ${reports[position].content}")
        holder.bind(reports[position])
    }


    override fun getItemCount(): Int = reports.size
}
