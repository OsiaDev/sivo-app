package com.coljuegos.sivo.ui.establecimiento.bingo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.InventarioEntity
import com.coljuegos.sivo.databinding.ItemInventarioBingoActaBinding

class InventarioBingoActaAdapter(
    private val onItemClick: (InventarioEntity) -> Unit
) : ListAdapter<InventarioEntity, InventarioBingoActaAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventarioBingoActaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemInventarioBingoActaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(inv: InventarioEntity) {
            val context = binding.root.context
            binding.tipoApuestaNombreText.text = inv.tipoApuestaNombreInventario
            binding.codigoApuestaText.text = context.getString(
                R.string.inventario_acta_codigo_apuesta, inv.codigoTipoApuestaInventario
            )
            binding.sillasText.text = context.getString(
                R.string.registrar_bingo_sillas_reportadas, inv.invSillasInventario
            )
            binding.root.setOnClickListener { onItemClick(inv) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<InventarioEntity>() {
        override fun areItemsTheSame(a: InventarioEntity, b: InventarioEntity) =
            a.uuidInventario == b.uuidInventario
        override fun areContentsTheSame(a: InventarioEntity, b: InventarioEntity) = a == b
    }

}