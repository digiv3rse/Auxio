/*
 * Copyright (c) 2021 Auxio Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package org.oxycblt.auxio.accent

import android.content.Context
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.RecyclerView
import org.oxycblt.auxio.R
import org.oxycblt.auxio.databinding.ItemAccentBinding
import org.oxycblt.auxio.ui.BackingData
import org.oxycblt.auxio.ui.BindingViewHolder
import org.oxycblt.auxio.ui.MonoAdapter
import org.oxycblt.auxio.util.getAttrColorSafe
import org.oxycblt.auxio.util.getColorSafe
import org.oxycblt.auxio.util.getViewHolderAt
import org.oxycblt.auxio.util.inflater
import org.oxycblt.auxio.util.stateList

/** An adapter that displays the accent palette. */
class AccentAdapter(listener: Listener) :
    MonoAdapter<Accent, AccentAdapter.Listener, NewAccentViewHolder>(listener) {
    var selectedAccent: Accent? = null
        private set
    private var selectedViewHolder: NewAccentViewHolder? = null

    override val data = AccentData()
    override val creator = NewAccentViewHolder.CREATOR

    override fun onBindViewHolder(viewHolder: NewAccentViewHolder, position: Int) {
        super.onBindViewHolder(viewHolder, position)

        if (data.getItem(position) == selectedAccent) {
            selectedViewHolder?.setSelected(false)
            selectedViewHolder = viewHolder
            viewHolder.setSelected(true)
        }
    }

    fun setSelectedAccent(accent: Accent, recycler: RecyclerView) {
        if (accent == selectedAccent) return
        selectedAccent = accent
        selectedViewHolder?.setSelected(false)
        selectedViewHolder = recycler.getViewHolderAt(accent.index) as NewAccentViewHolder?
        selectedViewHolder?.setSelected(true)
    }

    interface Listener {
        fun onAccentSelected(accent: Accent)
    }

    class AccentData : BackingData<Accent>() {
        override fun getItem(position: Int) = Accent(position)
        override fun getItemCount() = ACCENT_COUNT
    }
}

class NewAccentViewHolder private constructor(private val binding: ItemAccentBinding) :
    BindingViewHolder<Accent, AccentAdapter.Listener>(binding.root) {

    override fun bind(item: Accent, listener: AccentAdapter.Listener) {
        setSelected(false)

        binding.accent.apply {
            backgroundTintList = context.getColorSafe(item.primary).stateList
            contentDescription = context.getString(item.name)
            TooltipCompat.setTooltipText(this, contentDescription)
        }

        binding.accent.setOnClickListener { listener.onAccentSelected(item) }
    }

    fun setSelected(isSelected: Boolean) {
        val context = binding.accent.context

        binding.accent.isEnabled = !isSelected
        binding.accent.imageTintList =
            if (isSelected) {
                context.getAttrColorSafe(R.attr.colorSurface).stateList
            } else {
                context.getColorSafe(android.R.color.transparent).stateList
            }
    }

    companion object {
        val CREATOR =
            object : Creator<NewAccentViewHolder> {
                override val viewType: Int
                    get() = throw UnsupportedOperationException()

                override fun create(context: Context) =
                    NewAccentViewHolder(ItemAccentBinding.inflate(context.inflater))
            }
    }
}
