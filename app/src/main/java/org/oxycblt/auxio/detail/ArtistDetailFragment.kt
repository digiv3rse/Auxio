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
 
package org.oxycblt.auxio.detail

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.oxycblt.auxio.R
import org.oxycblt.auxio.databinding.FragmentDetailBinding
import org.oxycblt.auxio.detail.recycler.ArtistDetailAdapter
import org.oxycblt.auxio.detail.recycler.DetailAdapter
import org.oxycblt.auxio.detail.recycler.SortHeader
import org.oxycblt.auxio.music.Album
import org.oxycblt.auxio.music.Artist
import org.oxycblt.auxio.music.Music
import org.oxycblt.auxio.music.MusicParent
import org.oxycblt.auxio.music.Song
import org.oxycblt.auxio.playback.state.PlaybackMode
import org.oxycblt.auxio.ui.Header
import org.oxycblt.auxio.ui.Item
import org.oxycblt.auxio.ui.newMenu
import org.oxycblt.auxio.util.applySpans
import org.oxycblt.auxio.util.logD
import org.oxycblt.auxio.util.logW

/**
 * The [DetailFragment] for an artist.
 * @author OxygenCobalt
 */
class ArtistDetailFragment : DetailFragment(), DetailAdapter.Listener {
    private val args: ArtistDetailFragmentArgs by navArgs()
    private val detailAdapter = ArtistDetailAdapter(this)

    override fun onBindingCreated(binding: FragmentDetailBinding, savedInstanceState: Bundle?) {
        detailModel.setArtistId(args.artistId)

        setupToolbar(detailModel.currentArtist.value!!)
        requireBinding().detailRecycler.apply {
            adapter = detailAdapter
            applySpans { pos ->
                // If the item is an ActionHeader we need to also make the item full-width
                val item = detailAdapter.data.currentList[pos]
                item is Header || item is SortHeader || item is Artist
            }
        }

        // --- VIEWMODEL SETUP ---

        detailModel.artistData.observe(viewLifecycleOwner) { list ->
            detailAdapter.data.submitList(list)
        }

        detailModel.navToItem.observe(viewLifecycleOwner, ::handleNavigation)

        // Highlight songs if they are being played
        playbackModel.song.observe(viewLifecycleOwner) { song -> updateSong(song, detailAdapter) }

        // Highlight albums if they are being played
        playbackModel.parent.observe(viewLifecycleOwner) { parent ->
            updateParent(parent, detailAdapter)
        }
    }

    override fun onItemClick(item: Item) {
        when (item) {
            is Song -> playbackModel.playSong(item, PlaybackMode.IN_ARTIST)
            is Album ->
                findNavController()
                    .navigate(ArtistDetailFragmentDirections.actionShowAlbum(item.id))
        }
    }

    override fun onOpenMenu(item: Item, anchor: View) {
        newMenu(anchor, item)
    }

    override fun onPlayParent() {
        playbackModel.playArtist(requireNotNull(detailModel.currentArtist.value), false)
    }

    override fun onShuffleParent() {
        playbackModel.playArtist(requireNotNull(detailModel.currentArtist.value), true)
    }

    override fun onShowSortMenu(anchor: View) {
        showSortMenu(
            anchor,
            detailModel.artistSort,
            onConfirm = { detailModel.artistSort = it },
            showItem = { id -> id != R.id.option_sort_artist })
    }

    private fun handleNavigation(item: Music?) {
        val binding = requireBinding()

        when (item) {
            is Artist -> {
                if (item.id == detailModel.currentArtist.value?.id) {
                    logD("Navigating to the top of this artist")
                    binding.detailRecycler.scrollToPosition(0)
                    detailModel.finishNavToItem()
                } else {
                    logD("Navigating to another artist")
                    findNavController()
                        .navigate(ArtistDetailFragmentDirections.actionShowArtist(item.id))
                }
            }
            is Album -> {
                logD("Navigating to another album")
                findNavController()
                    .navigate(ArtistDetailFragmentDirections.actionShowAlbum(item.id))
            }
            is Song -> {
                logD("Navigating to another album")
                findNavController()
                    .navigate(ArtistDetailFragmentDirections.actionShowAlbum(item.album.id))
            }
            null -> {}
            else -> logW("Unsupported navigation item ${item::class.java}")
        }
    }

    private fun updateSong(song: Song?, adapter: ArtistDetailAdapter) {
        val binding = requireBinding()
        if (playbackModel.playbackMode.value == PlaybackMode.IN_ARTIST &&
            playbackModel.parent.value?.id == detailModel.currentArtist.value?.id) {
            adapter.highlightSong(song, binding.detailRecycler)
        } else {
            // Clear the ViewHolders if the mode isn't ALL_SONGS
            adapter.highlightSong(null, binding.detailRecycler)
        }
    }

    private fun updateParent(parent: MusicParent?, adapter: ArtistDetailAdapter) {
        val binding = requireBinding()
        if (parent is Album?) {
            adapter.highlightAlbum(parent, binding.detailRecycler)
        } else {
            adapter.highlightAlbum(null, binding.detailRecycler)
        }
    }
}
