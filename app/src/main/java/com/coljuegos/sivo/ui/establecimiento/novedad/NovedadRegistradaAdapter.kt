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

                // Mostrar marca, serial y contadores
                marcaValue.text = "Marca: ${novedad?.marca}"
                serialValue.text = "Serial: ${novedad?.serial}"

                val estaOperando = novedad?.operando == "Operando"
                val tieneContadores = novedad?.contadoresVerificado == true && estaOperando

                contadoresValue.text = when {
                    !estaOperando -> "Contadores: N/A"
                    tieneContadores -> "Contadores: Si"
                    else -> "Contadores: No"
                }

                // Aplicar color de fondo y borde según estado de contadores
                val rootCard = binding.root as com.google.android.material.card.MaterialCardView
                if (tieneContadores) {
                    val coinIn  = novedad?.coinInMet
                    val coinOut = novedad?.coinOutMet
                    val jackpot = novedad?.jackpotMet

                    val coinInSclm  = novedad?.coinInSclm
                    val coinOutSclm = novedad?.coinOutSclm
                    val jackpotSclm = novedad?.jackpotSclm

                    val metIncompleto  = coinIn.isNullOrEmpty()  || coinOut.isNullOrEmpty()  || jackpot.isNullOrEmpty()
                    val sclmIncompleto = coinInSclm.isNullOrEmpty() || coinOutSclm.isNullOrEmpty() || jackpotSclm.isNullOrEmpty()

                    if (metIncompleto || sclmIncompleto) {
                        val colorIncompleto = android.graphics.Color.parseColor("#FDECEA")
                        rootCard.setCardBackgroundColor(colorIncompleto)
                        rootCard.strokeColor = colorIncompleto
                    } else {
                        val colorCompleto = android.graphics.Color.parseColor("#E6F4EA")
                        rootCard.setCardBackgroundColor(colorCompleto)
                        rootCard.strokeColor = colorCompleto
                    }
                } else {
                    rootCard.setCardBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(
                            binding.root.context,
                            com.coljuegos.sivo.R.color.acta_background
                        )
                    )
                    rootCard.strokeColor = androidx.core.content.ContextCompat.getColor(
                        binding.root.context,
                        com.coljuegos.sivo.R.color.white
                    )
                }

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