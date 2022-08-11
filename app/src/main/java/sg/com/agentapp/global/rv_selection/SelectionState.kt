package sg.com.agentapp.global.rv_selection

import androidx.recyclerview.selection.SelectionTracker

class SelectionState(private val canSetStateForKey: Boolean, private val canSetStateAtPosition: Boolean, private val canSelectMultiple: Boolean) : SelectionTracker.SelectionPredicate<Long>() {

    override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean {
        return canSetStateForKey
    }

    override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
        return canSetStateAtPosition
    }

    override fun canSelectMultiple(): Boolean {
        return canSelectMultiple
    }
}