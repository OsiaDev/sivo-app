package com.coljuegos.sivo.ui.establecimiento.acta

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.MunicipioDisplayItem

class MunicipioAdapter(
    context: Context,
    private var allMunicipios: List<MunicipioDisplayItem>
) : ArrayAdapter<MunicipioDisplayItem>(context, R.layout.item_dropdown_municipio, allMunicipios) {

    private var filteredMunicipios = allMunicipios

    private val inflater = LayoutInflater.from(context)

    override fun getCount(): Int = filteredMunicipios.size

    override fun getItem(position: Int): MunicipioDisplayItem = filteredMunicipios[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.item_dropdown_municipio, parent, false)
        val textView = view.findViewById<TextView>(R.id.dropdown_item_text)

        val municipio = getItem(position)
        textView.text = municipio.displayName

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""

                val filtered = if (query.isEmpty()) {
                    allMunicipios
                } else {
                    allMunicipios.filter { municipio ->
                        val searchText = "${municipio.municipioNombre} ${municipio.departamentoNombre}".lowercase()
                        searchText.contains(query) ||
                                municipio.municipioNombre.lowercase().startsWith(query) ||
                                municipio.departamentoNombre.lowercase().startsWith(query)
                    }
                }

                return FilterResults().apply {
                    values = filtered
                    count = filtered.size
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredMunicipios = (results?.values as? List<MunicipioDisplayItem>) ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    fun updateData(newMunicipios: List<MunicipioDisplayItem>) {
        allMunicipios = newMunicipios
        filteredMunicipios = newMunicipios
        notifyDataSetChanged()
    }

}