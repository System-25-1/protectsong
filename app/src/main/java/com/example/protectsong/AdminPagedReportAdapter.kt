package com.example.protectsong

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

        fun bind(report: SmsReport) {
            tvCategory.text = report.type
            tvBuilding.text = report.building
            tvContent.text = report.content

            itemView.setOnClickListener {
                onItemClick(report)
            }
        }
    }
}
