package com.coljuegos.sivo.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.databinding.ItemActaBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ActaAdapter(
    private val onActaClick: (ActaEntity) -> Unit
) : ListAdapter<ActaEntity, ActaAdapter.ActaViewHolder>(ActaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActaViewHolder {
        val binding = ItemActaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActaViewHolder(
        private val binding: ItemActaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onActaClick(getItem(position))
                }
            }
        }

        fun bind(acta: ActaEntity) {
            with(binding) {
                val context = binding.root.context
                // Número de acta
                tvNumeroActa.text = context.getString(R.string.acta_numero, acta.numActa.toString())

                val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

                tvFechaVisita.text = dateFormatter.format(acta.fechaVisitaAucActa)

                // Nombre del establecimiento
                tvName.text = acta.establecimientoActa

                // Dirección
                tvDireccion.text = acta.direccionActa

                // Configurar etiqueta según tipo de visita
                setupTipoVisitaChip(acta.tipoVisitaActa)
            }
        }

        private fun setupTipoVisitaChip(tipoVisita: String) {
            with(binding.tvEtiqueta) {
                text = when (tipoVisita.lowercase()) {
                    "f" -> {
                        setBackgroundResource(R.drawable.bg_chip_establecimiento)
                        setTextColor(context.getColor(R.color.main))
                        "Establecimiento"
                    }
                    "c" -> {
                        setBackgroundResource(R.drawable.bg_chip_sede)
                        setTextColor(context.getColor(android.R.color.black))
                        "Sede"
                    }
                    else -> {
                        setBackgroundResource(R.drawable.bg_chip_establecimiento)
                        setTextColor(context.getColor(R.color.main))
                        tipoVisita.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
                }
            }
        }
    }

    class ActaDiffCallback : DiffUtil.ItemCallback<ActaEntity>() {
        override fun areItemsTheSame(oldItem: ActaEntity, newItem: ActaEntity): Boolean {
            return oldItem.uuidActa == newItem.uuidActa
        }

        override fun areContentsTheSame(oldItem: ActaEntity, newItem: ActaEntity): Boolean {
            return oldItem == newItem
        }
    }

}