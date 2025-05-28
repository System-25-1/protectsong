package com.example.protectsong.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.protectsong.R
import com.example.protectsong.model.Report

class AdminReportAdapter(
    private val onStatusChanged: (Report, String) -> Unit
) : RecyclerView.Adapter<AdminReportAdapter.AdminReportViewHolder>() {

    private val reports = mutableListOf<Report>()

    fun submitList(newList: List<Report>) {
        reports.clear()
        reports.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_report, parent, false)
        return AdminReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount(): Int = reports.size

    inner class AdminReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val spinnerStatus: Spinner = itemView.findViewById(R.id.spinnerStatus)

        fun bind(report: Report) {
            tvNumber.text = "신고번호: ${report.id}"
            tvContent.text = "내용: ${report.content}"

            val statusOptions = listOf("접수됨", "처리중", "완료")
            val adapter = ArrayAdapter(itemView.context, android.R.layout.simple_spinner_item, statusOptions)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerStatus.adapter = adapter
            spinnerStatus.setSelection(statusOptions.indexOf(report.status))

            spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedStatus = statusOptions[position]
                    if (selectedStatus != report.status) {
                        onStatusChanged(report, selectedStatus)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }
}
