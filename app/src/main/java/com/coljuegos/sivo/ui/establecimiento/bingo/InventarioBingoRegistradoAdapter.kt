package com.coljuegos.sivo.ui.establecimiento.bingo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.EstadoInventarioEnum
import com.coljuegos.sivo.databinding.ItemInventarioBingoRegistradoBinding

class InventarioBingoRegistradoAdapter(
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
            val context = binding.root.context

            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                binding.numeroRegistro.text = "# ${pos + 1}"
            }
            binding.tipoApuestaNombreText.text = inv.tipoApuestaNombreInventario
            binding.codigoText.text = context.getString(
                R.string.inventario_acta_codigo_apuesta, inv.codigoTipoApuestaInventario
            )
            binding.sillasText.text = context.getString(
                R.string.registrar_bingo_sillas_reportadas, inv.invSillasInventario
            )
            binding.estadoText.text = EstadoInventarioEnum.toString(reg.estado)

            binding.btnEditar.setOnClickListener { onEditClick(item) }
            binding.btnEliminar.setOnClickListener { onDeleteClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<InventarioBingoConRegistro>() {
        override fun areItemsTheSame(a: InventarioBingoConRegistro, b: InventarioBingoConRegistro) =
            a.registro.uuidInventarioBingoRegistrado == b.registro.uuidInventarioBingoRegistrado
        override fun areContentsTheSame(a: InventarioBingoConRegistro, b: InventarioBingoConRegistro) = a == b
    }

}