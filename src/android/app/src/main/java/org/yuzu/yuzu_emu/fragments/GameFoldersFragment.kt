// SPDX-FileCopyrightText: 2023 yuzu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

package org.yuzu.yuzu_emu.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.launch
import org.yuzu.yuzu_emu.R
import org.yuzu.yuzu_emu.adapters.FolderAdapter
import org.yuzu.yuzu_emu.databinding.FragmentFoldersBinding
import org.yuzu.yuzu_emu.model.GamesViewModel
import org.yuzu.yuzu_emu.model.HomeViewModel
import org.yuzu.yuzu_emu.ui.main.MainActivity

class GameFoldersFragment : Fragment() {
    private var _binding: FragmentFoldersBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val gamesViewModel: GamesViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

        gamesViewModel.onOpenGameFoldersFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoldersBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel.setNavigationVisibility(visible = false, animated = true)
        homeViewModel.setStatusBarShadeVisibility(visible = false)

        binding.toolbarFolders.setNavigationOnClickListener {
            binding.root.findNavController().popBackStack()
        }

        binding.listFolders.apply {
            layoutManager = GridLayoutManager(
                requireContext(),
                resources.getInteger(R.integer.grid_columns)
            )
            adapter = FolderAdapter(requireActivity(), gamesViewModel)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                gamesViewModel.folders.collect {
                    (binding.listFolders.adapter as FolderAdapter).submitList(it)
                }
            }
        }

        val mainActivity = requireActivity() as MainActivity
        binding.buttonAdd.setOnClickListener {
            mainActivity.getGamesDirectory.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).data)
        }

        setInsets()
    }

    override fun onStop() {
        super.onStop()
        gamesViewModel.onCloseGameFoldersFragment()
    }

    private fun setInsets() =
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { _: View, windowInsets: WindowInsetsCompat ->
            val barInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val leftInsets = barInsets.left + cutoutInsets.left
            val rightInsets = barInsets.right + cutoutInsets.right

            val mlpToolbar = binding.toolbarFolders.layoutParams as ViewGroup.MarginLayoutParams
            mlpToolbar.leftMargin = leftInsets
            mlpToolbar.rightMargin = rightInsets
            binding.toolbarFolders.layoutParams = mlpToolbar

            val fabSpacing = resources.getDimensionPixelSize(R.dimen.spacing_fab)
            val mlpFab =
                binding.buttonAdd.layoutParams as ViewGroup.MarginLayoutParams
            mlpFab.leftMargin = leftInsets + fabSpacing
            mlpFab.rightMargin = rightInsets + fabSpacing
            mlpFab.bottomMargin = barInsets.bottom + fabSpacing
            binding.buttonAdd.layoutParams = mlpFab

            val mlpListFolders = binding.listFolders.layoutParams as ViewGroup.MarginLayoutParams
            mlpListFolders.leftMargin = leftInsets
            mlpListFolders.rightMargin = rightInsets
            binding.listFolders.layoutParams = mlpListFolders

            binding.listFolders.updatePadding(
                bottom = barInsets.bottom +
                    resources.getDimensionPixelSize(R.dimen.spacing_bottom_list_fab)
            )

            windowInsets
        }
}
