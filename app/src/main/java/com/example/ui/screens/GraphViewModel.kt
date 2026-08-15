package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.VaultRepository
import com.example.domain.model.GraphData
import com.example.domain.model.GraphNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class GraphUiState(
    val graphData: GraphData = GraphData(emptyList(), emptyList()),
    val selectedNode: GraphNode? = null,
    val isLoading: Boolean = false
)

class GraphViewModel(
    private val repository: VaultRepository
) : ViewModel() {

    private val _selectedNode = MutableStateFlow<GraphNode?>(null)

    val uiState: StateFlow<GraphUiState> = combine(
        repository.graphData,
        _selectedNode,
        repository.isLoading
    ) { graphData, selectedNode, isLoading ->
        GraphUiState(
            graphData = graphData,
            selectedNode = selectedNode,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GraphUiState()
    )

    fun selectNode(node: GraphNode?) {
        _selectedNode.value = node
    }
}
