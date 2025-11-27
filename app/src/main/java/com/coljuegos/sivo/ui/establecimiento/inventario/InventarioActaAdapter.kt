package com.coljuegos.sivo.ui.establecimiento.inventario

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.InventarioEntity
import com.coljuegos.sivo.databinding.ItemInventarioActaBinding

class InventarioActaAdapter(
    private val onItemClick: (InventarioEntity) -> Unit
) : ListAdapter<InventarioEntity, InventarioActaAdapter.InventarioViewHolder>(InventarioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {
        val binding = ItemInventarioActaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InventarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InventarioViewHolder(
        private val binding: ItemInventarioActaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(inventario: InventarioEntity) {
            with(binding) {
                val context = binding.root.context

                // Marca
                marcaValue.text = context.getString(R.string.inventario_acta_marca, inventario.nombreMarcaInventario)

                // Serial
                serialValue.text = context.getString(R.string.inventario_acta_serial, inventario.metSerialInventario)

                // Código
                codigoValue.text = context.getString(R.string.inventario_acta_codigo, inventario.conCodigoInventario)

                // NUC
                nucValue.text = context.getString(R.string.inventario_acta_nuc, inventario.nucInventario)

                // Código apuesta
                codigoApuestaValue.text = context.getString(R.string.inventario_acta_codigo_apuesta, inventario.codigoTipoApuestaInventario)

                // Online
                val online = context.getString(if (inventario.metOnlineInventario) R.string.inventario_acta_si else R.string.inventario_acta_no)
                onlineValue.text = context.getString(R.string.inventario_acta_online, online)

                // Click en la card
                root.setOnClickListener {
                    onItemClick(inventario)
                }
            }
        }
    }

    class InventarioDiffCallback : DiffUtil.ItemCallback<InventarioEntity>() {
        override fun areItemsTheSame(oldItem: InventarioEntity, newItem: InventarioEntity): Boolean {
            return oldItem.uuidInventario == newItem.uuidInventario
        }

        override fun areContentsTheSame(oldItem: InventarioEntity, newItem: InventarioEntity): Boolean {
            return oldItem == newItem
        }
    }

}