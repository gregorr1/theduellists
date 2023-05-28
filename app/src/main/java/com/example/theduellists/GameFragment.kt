package com.example.theduellists

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.theduellists.databinding.CardItemBinding
import com.example.theduellists.databinding.FragmentGameBinding
import com.example.theduellists.model.BLUE
import com.example.theduellists.model.GREEN
import com.example.theduellists.model.GameViewModel
import com.example.theduellists.model.RED
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GameFragment : Fragment() {
    private var binding: FragmentGameBinding? = null
    private var linearLayout: LinearLayout? = null
    private val viewModel: GameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentGameBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        linearLayout = binding?.cardsLayout
        updateRadioButtonText()
        updateTextViewText()

        binding?.fightButton?.setOnClickListener {
            viewModel.setOption(::getSelectedOption)
            viewModel.onFightClicked()
            inflateCard()
            if (viewModel.playerHP.value!! > 0 && viewModel.opponentHP.value!! > 0) {
                viewModel.changeStatus()
                updateRadioButtonText()
                updateTextViewText()
            } else {
                showEndgameDialog()
            }
        }

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply{
            lifecycleOwner = viewLifecycleOwner
            viewModel = viewModel
            gameFragment = this@GameFragment
        }

        viewModel.playerHP.observe(viewLifecycleOwner
        ) { newHP ->
            if (newHP > 0 ) {
                binding?.playerHpView?.text = getString(R.string.player_hp, newHP)
            } else {
                binding?.playerHpView?.text = getString(R.string.player_hp, 0)
            }
        }

        viewModel.opponentHP.observe(viewLifecycleOwner
        ) { newHP ->
            if (newHP > 0 ) {
                binding?.opponentHpView?.text = getString(R.string.opponent_hp, newHP)
            } else {
                binding?.opponentHpView?.text = getString(R.string.opponent_hp, 0)
            }
        }
    }

    private fun getSelectedOption(): String {
        return when (binding?.attackDefenceOptions?.checkedRadioButtonId) {
            R.id.blue_option -> BLUE
            R.id.red_option -> RED
            else -> GREEN
        }
    }

    private fun inflateCard() {
        val inflater = LayoutInflater.from(requireContext())
        val cardBinding: CardItemBinding =
            CardItemBinding.inflate(inflater, linearLayout, false)

        linearLayout?.addView(cardBinding.root)
        val turnLabel = resources.getString(R.string.turn_number, viewModel.turn)
        cardBinding.turnView.text = turnLabel
        cardBinding.attackTextview.text = updateAttackTextview()
        cardBinding.defenceTextview.text = updateDefenceTextview()
        cardBinding.damageTextview.text = updateDamageTextview()
        binding?.scrollView?.post {
            binding?.scrollView?.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun updateAttackTextview(): String {
        var text = ""
        if (viewModel.attackState) {
            when (viewModel.selectedOption.value) {
                GREEN -> text = resources.getString(R.string.player_sword_attack)
                BLUE -> text = resources.getString(R.string.player_arrow_attack)
                RED -> text = resources.getString(R.string.player_fireball_attack)
            }
        } else {
            when (viewModel.opponentRoll) {
                1 -> text = resources.getString(R.string.opponent_sword_attack)
                2 -> text = resources.getString(R.string.opponent_arrow_attack)
                3 -> text = resources.getString(R.string.opponent_fireball_attack)
            }
        }
        return text
    }

    private fun updateDefenceTextview(): String {
        var text = ""
        if (!viewModel.attackState) {
            when (viewModel.selectedOption.value) {
                GREEN -> text = resources.getString(R.string.player_armour_defense)
                BLUE -> text = resources.getString(R.string.player_shield_defense)
                RED -> text = resources.getString(R.string.player_barrier_defense)
            }
        } else {
            when (viewModel.opponentRoll) {
                1 -> text = resources.getString(R.string.opponent_armour_defense)
                2 -> text = resources.getString(R.string.opponent_shield_defense)
                3 -> text = resources.getString(R.string.opponent_barrier_defense)
            }
        }
        return text
    }

    private fun updateDamageTextview(): String {
        var text = ""
        text = if (viewModel.attackState) {
            if (viewModel.selectedOption.value == RED && viewModel.opponentRoll == 3) {
                resources.getString(R.string.player_damage_suffered, viewModel.damage)
            } else {
                resources.getString(R.string.player_damage_inflicted, viewModel.damage)
            }
        } else {
            if (viewModel.selectedOption.value == RED && viewModel.opponentRoll == 3) {
                resources.getString(R.string.opponent_damage_suffered, viewModel.damage)
            } else {
                resources.getString(R.string.opponent_damage_inflicted, viewModel.damage)
            }
        }
        return text
    }

    private fun updateRadioButtonText() {
        val radioGroup = binding?.attackDefenceOptions

        radioGroup?.getChildAt(0).apply {
            if (viewModel.attackState) {
                (this as TextView).text = context.getString(R.string.sword)
            } else {
                (this as TextView).text = context.getString(R.string.armour)
            }
        }

        radioGroup?.getChildAt(1).apply {
            if (viewModel.attackState) {
                (this as TextView).text = context.getString(R.string.arrow)
            } else {
                (this as TextView).text = context.getString(R.string.shield)
            }
        }

        radioGroup?.getChildAt(2).apply {
            if (viewModel.attackState) {
                (this as TextView).text = context.getString(R.string.fireball)
            } else {
                (this as TextView).text = context.getString(R.string.barrier)
            }
        }
    }

    private fun updateTextViewText() {
        val textView = binding?.optionsText
        if (viewModel.attackState) {
            textView?.text = getString(R.string.pick_attack)
        } else {
            textView?.text = getString(R.string.pick_defence)
        }
    }

    private fun showEndgameDialog() {
        val endgameDialog = MaterialAlertDialogBuilder(requireContext())
            .setBackground(ColorDrawable(resources.getColor(R.color.background)))
            .setTitle(setEndgameTitle())
            .setMessage(setEndgameMessage())
            .setCancelable(false)
            .setPositiveButton(getString(R.string.finish)) { _, _ ->
                exitGame()
            }
            .create()

        endgameDialog.show()
    }

    private fun setEndgameTitle(): String {
        return if (viewModel.opponentHP.value?.let {it <= 0} == true) {
            getString(R.string.player_won)
        } else {
            getString(R.string.opponent_won)
        }
    }

    private fun setEndgameMessage(): String {
        var text = ""
        // Check if opponent HP <= 0. If yes:
        if (viewModel.opponentHP.value?.let {it <= 0} == true) {
            // Check if the player was attacking
            if (viewModel.attackState) {
                when (viewModel.opponentRoll) {
                    1 -> text = resources.getString(R.string.opponent_armour_defeat)
                    2 -> text = resources.getString(R.string.opponent_shield_defeat)
                    3 -> text = resources.getString(R.string.opponent_barrier_defeat)
                }
            } else {
                // Set text if opponent was attacking and lost to a barrier
                text = resources.getString(R.string.opponent_fireball_defeat)
            }

        } else {
            // Player HP <= 0, check if the opponent was attacking
            if (!viewModel.attackState) {
                when (viewModel.selectedOption.value) {
                    GREEN -> text = resources.getString(R.string.player_armour_defeat)
                    BLUE -> text = resources.getString(R.string.player_shield_defeat)
                    RED -> text = resources.getString(R.string.player_barrier_defeat)
                }
            } else {
                // Set text if player was attacking and lost to a barrier
                text = resources.getString(R.string.player_fireball_defeat)
            }
        }
        return text
    }

    private fun exitGame() {
        viewModel.reinitializeData()
        findNavController().navigate(R.id.action_gameFragment_to_startFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}