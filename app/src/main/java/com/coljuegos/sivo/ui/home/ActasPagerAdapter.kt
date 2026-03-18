package com.coljuegos.sivo.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coljuegos.sivo.databinding.PageActasListBinding
import com.coljuegos.sivo.data.entity.ActaEntity

class ActasPagerAdapter(
    private val onActaPendienteClick: (ActaEntity) -> Unit,
    private val onActaCompletadaClick: (ActaEntity) -> Unit
) : RecyclerView.Adapter<ActasPagerAdapter.PageViewHolder>() {

    private val pendientesAdapter = ActaAdapter(onActaPendienteClick)
    private val completadasAdapter = ActaCompletadaAdapter(onActaCompletadaClick)

    fun submitPendientes(actas: List<ActaEntity>) {
        pendientesAdapter.submitList(actas)
    }

    fun submitCompletadas(actas: List<ActaCompletadaUiModel>) {
        completadasAdapter.submitList(actas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = PageActasListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PageViewHolder(binding)
    }

    override fun getItemCount(): Int = 2 // Tab 0: Pendientes, Tab 1: Completadas

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class PageViewHolder(
        private val binding: PageActasListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
        }

        fun bind(position: Int) {
            if (position == 0) {
                binding.recyclerView.adapter = pendientesAdapter
            } else {
                binding.recyclerView.adapter = completadasAdapter
            }
        }
    }
}
