package com.coljuegos.sivo.ui.establecimiento.inventario

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coljuegos.sivo.databinding.ItemInventarioReportadoBinding

class InventarioRegistradoAdapter(
    private val onEditClick: (InventarioConRegistro) -> Unit,
    private val onDeleteClick: (InventarioConRegistro) -> Unit
) : ListAdapter<InventarioConRegistro, InventarioRegistradoAdapter.InventarioViewHolder>(InventarioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {
        val binding = ItemInventarioReportadoBinding.inflate(
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
        private val binding: ItemInventarioReportadoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InventarioConRegistro) {
            with(binding) {
                val inventario = item.inventario

                // Mostrar marca y serial
                marcaValue.text = "Marca: ${inventario.nombreMarcaInventario}"
                serialValue.text = "Serial: ${inventario.metSerialInventario}"

                // Configurar botón editar
                btnEditar.setOnClickListener {
                    onEditClick(item)
                }

                // Configurar botón eliminar
                btnEliminar.setOnClickListener {
                    onDeleteClick(item)
                }
            }
        }
    }

    class InventarioDiffCallback : DiffUtil.ItemCallback<InventarioConRegistro>() {
        override fun areItemsTheSame(oldItem: InventarioConRegistro, newItem: InventarioConRegistro): Boolean {
            return oldItem.inventario.uuidInventario == newItem.inventario.uuidInventario
        }

        override fun areContentsTheSame(oldItem: InventarioConRegistro, newItem: InventarioConRegistro): Boolean {
            return oldItem == newItem
        }
    }

}