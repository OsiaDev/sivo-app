package com.coljuegos.sivo.ui.establecimiento.bingo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.EstadoInventarioEnum
import com.coljuegos.sivo.databinding.ItemInventarioBingoRegistradoBinding

class InventarioBingoRegistradoAdapter(
    private val onItemClick: (InventarioBingoConRegistro) -> Unit,
    private val onEditClick: (InventarioBingoConRegistro) -> Unit,
    private val onDeleteClick: (InventarioBingoConRegistro) -> Unit
) : ListAdapter<InventarioBingoConRegistro, InventarioBingoRegistradoAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventarioBingoRegistradoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemInventarioBingoRegistradoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InventarioBingoConRegistro) {
            val inv = item.inventario
            val reg = item.registro

            binding.tipoApuestaNombreText.text = inv.tipoApuestaNombreInventario
            binding.codigoText.text = binding.root.context
                .getString(R.string.inventario_acta_codigo_apuesta, inv.codigoTipoApuestaInventario)
            binding.sillasText.text = binding.root.context
                .getString(R.string.registrar_bingo_sillas_reportadas, inv.invSillasInventario)

            if (reg != null) {
                binding.estadoText.text = reg.estado.name
                binding.sinRegistrarBadge.isVisible = false
                binding.btnEditar.isVisible = true
                binding.btnEliminar.isVisible = true

                val colorRes = when (reg.estado) {
                    EstadoInventarioEnum.OPERANDO -> R.color.green
                    EstadoInventarioEnum.APAGADO -> R.color.orange
                    EstadoInventarioEnum.NO_ENCONTRADO -> R.color.red
                    else -> R.color.grey
                }
                binding.estadoIndicador.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, colorRes)
                )

                binding.btnEditar.setOnClickListener { onEditClick(item) }
                binding.btnEliminar.setOnClickListener { onDeleteClick(item) }
            } else {
                binding.estadoText.text = ""
                binding.sinRegistrarBadge.isVisible = true
                binding.btnEditar.isVisible = false
                binding.btnEliminar.isVisible = false
                binding.estadoIndicador.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.grey)
                )
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<InventarioBingoConRegistro>() {
        override fun areItemsTheSame(a: InventarioBingoConRegistro, b: InventarioBingoConRegistro) =
            a.inventario.uuidInventario == b.inventario.uuidInventario

        override fun areContentsTheSame(a: InventarioBingoConRegistro, b: InventarioBingoConRegistro) =
            a == b
    }

}