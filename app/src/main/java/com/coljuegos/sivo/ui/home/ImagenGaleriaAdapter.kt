package com.coljuegos.sivo.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.ImagenEntity
import com.coljuegos.sivo.databinding.ItemImagenGaleriaBinding
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.util.UUID

class ImagenGaleriaAdapter(
    private val onItemSelected: (UUID) -> Unit
) : ListAdapter<ImagenItemUiModel, ImagenGaleriaAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImagenGaleriaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onItemSelected)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemImagenGaleriaBinding,
        private val onItemSelected: (UUID) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ImagenItemUiModel) {
            val entity = item.entity
            val file = File(entity.rutaImagen)
            if (file.exists()) {
                Glide.with(binding.ivImagen.context)
                    .load(file)
                    .apply(RequestOptions.centerCropTransform())
                    .into(binding.ivImagen)
            } else {
                binding.ivImagen.setImageResource(R.drawable.ic_camara)
            }

            // Mostramos el estado visual de sincronización
            if (entity.isSincronizada) {
                binding.ivStatus.setImageResource(R.drawable.ic_check_circle)
                binding.ivStatus.background = binding.root.context.getDrawable(R.drawable.bg_circle_green)
                binding.vOverlay.visibility = View.GONE
                binding.ivSelection.visibility = View.GONE
            } else {
                binding.ivSelection.visibility = View.VISIBLE
                if (entity.ultimoError != null) {
                    binding.ivStatus.setImageResource(R.drawable.ic_error_circle)
                    binding.ivStatus.background = binding.root.context.getDrawable(R.drawable.bg_circle_red)
                    binding.vOverlay.visibility = View.VISIBLE
                } else {
                    binding.ivStatus.setImageResource(R.drawable.ic_pending_clipboard)
                    binding.ivStatus.background = binding.root.context.getDrawable(R.drawable.bg_circle_orange)
                    binding.vOverlay.visibility = View.GONE
                }
                
                // Visualización de SELECCIÓN
                if (item.isSelected) {
                    binding.ivSelection.background = binding.root.context.getDrawable(R.drawable.bg_circle_green)
                    binding.ivSelection.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
                } else {
                    binding.ivSelection.background = binding.root.context.getDrawable(R.drawable.bg_circle_salmon)
                    binding.ivSelection.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
                }
            }
            binding.ivStatus.setPadding(4, 4, 4, 4)

            binding.root.setOnClickListener {
                if (!entity.isSincronizada) {
                    onItemSelected(entity.uuidImagen)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ImagenItemUiModel>() {
        override fun areItemsTheSame(oldItem: ImagenItemUiModel, newItem: ImagenItemUiModel) = 
            oldItem.entity.uuidImagen == newItem.entity.uuidImagen
        override fun areContentsTheSame(oldItem: ImagenItemUiModel, newItem: ImagenItemUiModel) = 
            oldItem == newItem
    }
}
