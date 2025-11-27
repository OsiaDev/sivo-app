package com.coljuegos.sivo.ui.establecimiento.novedad

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coljuegos.sivo.databinding.ItemNovedadReportadaBinding

class NovedadRegistradaAdapter(
    private val onEditClick: (NovedadConRegistro) -> Unit,
    private val onDeleteClick: (NovedadConRegistro) -> Unit
) : ListAdapter<NovedadConRegistro, NovedadRegistradaAdapter.NovedadViewHolder>(NovedadDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NovedadViewHolder {
        val binding = ItemNovedadReportadaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NovedadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NovedadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NovedadViewHolder(
        private val binding: ItemNovedadReportadaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NovedadConRegistro) {
            with(binding) {
                val novedad = item.novedad

                // Mostrar marca y serial
                marcaValue.text = "Marca: ${novedad?.marca}"
                serialValue.text = "Serial: ${novedad?.serial}"

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

    class NovedadDiffCallback : DiffUtil.ItemCallback<NovedadConRegistro>() {
        override fun areItemsTheSame(oldItem: NovedadConRegistro, newItem: NovedadConRegistro): Boolean {
            return oldItem.novedad?.serial == newItem.novedad?.serial
        }

        override fun areContentsTheSame(oldItem: NovedadConRegistro, newItem: NovedadConRegistro): Boolean {
            return oldItem == newItem
        }
    }

}