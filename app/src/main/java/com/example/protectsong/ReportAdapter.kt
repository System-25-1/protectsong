<<<<<<< HEAD
package com.example.protectsong
=======
package com.example.protectsong.adapter
>>>>>>> feature/eunseo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
<<<<<<< HEAD

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
=======
import com.example.protectsong.R
import com.example.protectsong.model.Report

class ReportAdapter : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    private val reports = mutableListOf<Report>()

    fun submitList(newList: List<Report>) {
        reports.clear()
        reports.addAll(newList)
        notifyDataSetChanged()
>>>>>>> feature/eunseo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

<<<<<<< HEAD
    override fun getItemCount() = reports.size
=======
    override fun getItemCount(): Int = reports.size

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(report: Report) {
            tvDate.text = "신고일자: ${report.date}"
            tvNumber.text = "신고번호: ${report.number}"
            tvStatus.text = "처리상태: ${report.status}"
        }
    }
>>>>>>> feature/eunseo
}
