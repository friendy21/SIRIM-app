package com.sirimocr.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sirimocr.app.data.database.entities.SirimRecord
import com.sirimocr.app.databinding.ItemSirimRecordBinding

class RecordsAdapter : ListAdapter<SirimRecord, RecordsAdapter.RecordViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSirimRecordBinding.inflate(inflater, parent, false)
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecordViewHolder(private val binding: ItemSirimRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: SirimRecord) {
            binding.serialText.text = record.sirimSerialNo
            binding.brandText.text = record.brandTrademark ?: "—"
            binding.statusChip.text = record.validationStatus
            binding.statusChip.isCheckable = false
            binding.statusChip.isClickable = false
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SirimRecord>() {
        override fun areItemsTheSame(oldItem: SirimRecord, newItem: SirimRecord): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SirimRecord, newItem: SirimRecord): Boolean =
            oldItem == newItem
    }
}
