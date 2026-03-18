package com.coljuegos.sivo.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.data.entity.ActaStateEnum
import com.coljuegos.sivo.databinding.ItemActaCompletadaBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ActaCompletadaAdapter(
    private val onActaClick: (ActaEntity) -> Unit
) : ListAdapter<ActaCompletadaUiModel, ActaCompletadaAdapter.ActaViewHolder>(ActaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActaViewHolder {
        val binding = ItemActaCompletadaBinding.inflate(
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
        private val binding: ItemActaCompletadaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onActaClick(getItem(position).acta)
                }
            }
        }

        fun bind(uiModel: ActaCompletadaUiModel) {
            val acta = uiModel.acta
            with(binding) {
                val context = binding.root.context
                
                // Número de acta
                tvNumeroActa.text = context.getString(R.string.acta_numero, acta.numActa.toString())

                val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                tvFechaVisita.text = dateFormatter.format(acta.lastUpdatedActa)

                // Nombre del establecimiento
                tvName.text = acta.establecimientoActa

                // Dirección
                tvDireccion.text = acta.direccionActa

                setupTipoVisitaChip(acta.tipoVisitaActa)

                // Estado 
                tvEstado.text = when(acta.stateActa) {
                    ActaStateEnum.COMPLETE -> "Completada"
                    ActaStateEnum.SINCRONIZADO -> "Sincronizada"
                    else -> "Finalizada"
                }

                // Check verde solo si todo está sincronizado
                ivCheck.visibility = if (uiModel.todoSincronizado) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.INVISIBLE
                }
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

    class ActaDiffCallback : DiffUtil.ItemCallback<ActaCompletadaUiModel>() {
        override fun areItemsTheSame(oldItem: ActaCompletadaUiModel, newItem: ActaCompletadaUiModel): Boolean {
            return oldItem.acta.uuidActa == newItem.acta.uuidActa
        }

        override fun areContentsTheSame(oldItem: ActaCompletadaUiModel, newItem: ActaCompletadaUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
