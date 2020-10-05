package org.oxycblt.auxio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.oxycblt.auxio.databinding.FragmentMainBinding
import org.oxycblt.auxio.library.LibraryFragment
import org.oxycblt.auxio.music.MusicViewModel
import org.oxycblt.auxio.songs.SongsFragment
import org.oxycblt.auxio.theme.accent
import org.oxycblt.auxio.theme.getInactiveAlpha
import org.oxycblt.auxio.theme.getTransparentAccent
import org.oxycblt.auxio.theme.toColor

class MainFragment : Fragment() {
    private val musicModel: MusicViewModel by activityViewModels {
        MusicViewModel.Factory(requireActivity().application)
    }

    private val shownFragments = listOf(0, 1)

    private val tabIcons = listOf(
        R.drawable.ic_library,
        R.drawable.ic_song
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMainBinding.inflate(inflater)

        // If musicModel was cleared while the app was closed [Likely due to Auxio being suspended
        // in the background], then navigate back to loading to reload the music.
        if (musicModel.response.value == null) {
            findNavController().navigate(MainFragmentDirections.actionReturnToLoading())

            return null
        }

        val colorActive = accent.first.toColor(requireContext())
        val colorInactive = getTransparentAccent(
            requireContext(),
            accent.first,
            getInactiveAlpha(accent.first)
        )

        // --- UI SETUP ---

        binding.lifecycleOwner = viewLifecycleOwner
        binding.mainViewPager.adapter = PagerAdapter()

        // Link the ViewPager & Tab View
        TabLayoutMediator(binding.mainTabs, binding.mainViewPager) { tab, position ->
            tab.icon = ContextCompat.getDrawable(requireContext(), tabIcons[position])

            // Set the icon tint to deselected if its not the default tab
            if (position > 0) {
                tab.icon?.setTint(colorInactive)
            }
        }.attach()

        // Set up the selected/deselected colors
        binding.mainTabs.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab) {
                    tab.icon?.setTint(colorActive)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    tab.icon?.setTint(colorInactive)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            }
        )

        Log.d(this::class.simpleName, "Fragment Created.")

        return binding.root
    }

    private fun fragmentAt(position: Int): Fragment {
        return when (position) {
            0 -> LibraryFragment()
            1 -> SongsFragment()

            else -> SongsFragment()
        }
    }

    private inner class PagerAdapter :
        FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
        override fun getItemCount(): Int = shownFragments.size

        override fun createFragment(position: Int): Fragment {
            Log.d(this::class.simpleName, "Switching to fragment $position.")

            if (shownFragments.contains(position)) {
                return fragmentAt(position)
            }

            // If a fragment that shouldn't be shown is somehow shown anyway, just return
            // its intended fragment.
            Log.e(
                this::class.simpleName,
                "Attempted to index a fragment that shouldn't be shown."
            )

            return fragmentAt(position)
        }
    }
}
