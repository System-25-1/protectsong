package com.example.protectsong.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.protectsong.R
import com.example.protectsong.model.Report

class ReportAdapter(
    private val onItemClick: ((Report) -> Unit)? = null
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    private val reports = mutableListOf<Report>()

    fun submitList(newList: List<Report>) {
        reports.clear()
        reports.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount(): Int = reports.size

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(report: Report) {
            tvDate.text = "신고일자: ${report.date}"
            tvNumber.text = "신고번호: ${report.number}"
            tvStatus.text = "처리상태: ${report.status}"

            itemView.setOnClickListener {
                onItemClick?.invoke(report)
            }
        }
    }
}
