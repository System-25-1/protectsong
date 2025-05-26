package com.example.protectsong

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.protectsong.model.SmsReport

class AdminPagedReportAdapter(
    private val onItemClick: (SmsReport) -> Unit
) : RecyclerView.Adapter<AdminPagedReportAdapter.ReportViewHolder>() {

    private var reports: List<SmsReport> = listOf()

    fun submitList(newList: List<SmsReport>) {
        reports = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_sms_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount(): Int = reports.size

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvBuilding: TextView = itemView.findViewById(R.id.tvBuilding)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvStatusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)

        fun bind(report: SmsReport) {
            tvCategory.text = report.type
            tvBuilding.text = report.building
            tvContent.text = report.content

            // ✅ 상태 뱃지 설정
            tvStatusBadge.text = report.status
            when (report.status) {
                "접수됨" -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_received)
                    tvStatusBadge.setTextColor(Color.BLACK)
                }
                "처리중" -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_processing)
                    tvStatusBadge.setTextColor(Color.BLACK)
                }
                "처리완료" -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_status_done)
                    tvStatusBadge.setTextColor(Color.BLACK)
                }
                else -> {
                    tvStatusBadge.setBackgroundColor(Color.LTGRAY)
                    tvStatusBadge.setTextColor(Color.DKGRAY)
                }
            }

            // ✅ 아이템 클릭 처리
            itemView.setOnClickListener {
                onItemClick(report)
            }
        }
    }
}
