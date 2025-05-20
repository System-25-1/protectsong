package com.example.protectsong

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportAdapter(
    private val reports: List<Report>,
    private val onItemClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvBuilding: TextView = itemView.findViewById(R.id.tvBuilding)

        fun bind(report: Report) {
            tvType.text = "분류: ${report.type}"
            tvContent.text = report.content
            tvBuilding.text = "위치: ${report.building}"

            itemView.setOnClickListener {
                onItemClick(report)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount() = reports.size
}
