/*
 * Copyright (c) 2022 Auxio Project
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
 
package org.oxycblt.auxio.music.picker

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.oxycblt.auxio.R
import org.oxycblt.auxio.databinding.DialogMusicPickerBinding
import org.oxycblt.auxio.list.BasicListListener
import org.oxycblt.auxio.list.Item
import org.oxycblt.auxio.music.Artist
import org.oxycblt.auxio.shared.ViewBindingDialogFragment
import org.oxycblt.auxio.util.collectImmediately

/**
 * The base class for dialogs that implements common behavior across all [Artist] pickers.
 * These are shown whenever what to do with an item's [Artist] is ambiguous, as there are
 * multiple [Artist]'s to choose from.
 * @author Alexander Capehart (OxygenCobalt)
 */
abstract class ArtistPickerDialog : ViewBindingDialogFragment<DialogMusicPickerBinding>(), BasicListListener {
    protected val pickerModel: PickerViewModel by viewModels()
    // Okay to leak this since the Listener will not be called until after full initialization.
    private val artistAdapter = ArtistChoiceAdapter(@Suppress("LeakingThis") this)

    override fun onCreateBinding(inflater: LayoutInflater) =
        DialogMusicPickerBinding.inflate(inflater)

    override fun onConfigDialog(builder: AlertDialog.Builder) {
        builder.setTitle(R.string.lbl_artists).setNegativeButton(R.string.lbl_cancel, null)
    }

    override fun onBindingCreated(binding: DialogMusicPickerBinding, savedInstanceState: Bundle?) {
        binding.pickerRecycler.adapter = artistAdapter

        collectImmediately(pickerModel.currentArtists) { artists ->
            if (!artists.isNullOrEmpty()) {
                // Make sure the artist choices align with the current music library.
                // TODO: I really don't think it makes sense to do this. I'd imagine it would
                //  be more productive to just exit this dialog rather than try to update it.
                artistAdapter.submitList(artists)
            } else {
                // Not showing any choices, navigate up.
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyBinding(binding: DialogMusicPickerBinding) {
        binding.pickerRecycler.adapter = null
    }

    override fun onClick(item: Item) {
        findNavController().navigateUp()
    }
}
