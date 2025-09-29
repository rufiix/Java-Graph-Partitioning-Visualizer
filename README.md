# Graph Partitioning Visualizer

A desktop application for visualizing the Kernighan-Lin algorithm, a classic heuristic for partitioning graphs into balanced subsets while minimizing edge cuts.

## Overview

This Java Swing application provides an interactive, step-by-step visualization of the Kernighan-Lin graph partitioning algorithm. It allows users to load graphs from files, generate random graphs, and watch the algorithm optimize the partition in real-time.

## Features

- **Interactive Visualization**: Dual-panel view showing initial state and algorithm progress
- **Step-by-Step Execution**: Navigate through each swap operation with visual highlighting
- **Animation Controls**: Play, pause, step forward, and adjust playback speed
- **Graph Generation**: Create random graphs with configurable vertex count and edge density
- **File I/O**: Load graphs from files and save partitioning results
- **Algorithm Logging**: Detailed text log of all operations and improvements
- **Multi-Partition Support**: Partition graphs into 2 or more subsets

## Algorithm Details

The Kernighan-Lin algorithm is a heuristic method for graph partitioning that:

1. Starts with a random initial partition
2. Iteratively swaps vertex pairs between partitions to reduce cut edges
3. Uses a gain function: `gain = external_edges - internal_edges`
4. Locks swapped vertices to prevent re-swapping in the same pass
5. Selects the best prefix of swaps (classic KL optimization)
6. Repeats until no improvement is found

**Time Complexity**: O(n² × iterations) per pass, where n is the number of vertices.

## Data Structures

### Graph Representation (CSR Format)

The application uses Compressed Sparse Row (CSR) format for efficient graph storage:

```java
class Graph {
    int numVertices;
    int[] adjacency;   // All neighbors concatenated
    int[] pointers;    // Start index for each vertex's neighbors
}
```

This format is memory-efficient for sparse graphs and provides O(degree) neighbor lookup.

### Algorithm Step Tracking

```java
class AlgorithmStep {
    int[] partitionMap;  // Current partition assignment
    int cutEdges;        // Number of edges crossing partitions
    String description;  // Human-readable log message
    int vertex1, vertex2; // Highlighted vertices for this step
}
```

## Requirements

- Java 8 or higher
- Java Swing (included in JDK)

## Usage

### Running the Application

```bash
javac GraphPartitionGUI.java
java GraphPartitionGUI
```

### Loading a Graph

**Option 1: Load from File**

Click "Load File" and select a graph file with the following format:

```
<number_of_vertices>
<adjacency_array_semicolon_separated>
<pointers_array_semicolon_separated>
```

Example (triangle graph with 3 vertices):
```
3
1;2;0;2;0;1
0;2;4;6
```

**Option 2: Generate Random Graph**

1. Click "Random Graph"
2. Enter number of vertices (3-100)
3. Enter edge density (0-1, where 1 = complete graph)
4. Click OK

### Running the Algorithm

1. Set number of partitions (default: 2)
2. Set margin percentage (currently not enforced in the algorithm)
3. Click "Start" to run the algorithm
4. Use controls to navigate:
   - **Step**: Advance one step forward
   - **Play/Pause**: Auto-play animation
   - **Reset**: Return to first step
   - **Speed Slider**: Adjust animation speed (100-2000ms per step)

### Saving Results

Click "Save" after the algorithm completes to export the final partition to a file:

```
<number_of_partitions>
<cut_edges>
<partition_0_size> <vertex_ids...>
<partition_1_size> <vertex_ids...>
...
```

## Visualization Features

- **Circular Layout**: Vertices arranged in a circle for clarity
- **Color Coding**: Each partition gets a distinct color
- **Cut Edge Highlighting**: Edges crossing partitions are shown in red
- **Vertex Highlighting**: Currently swapped vertices are outlined in yellow
- **Real-time Metrics**: Cut edge count and step number displayed continuously

## Configuration

Modify these constants in the code to adjust behavior:

```java
private static final int NODE_SIZE = 20;      // Vertex display size
private static final int MAX_PASSES = 10;     // Maximum refinement passes
```

## Known Limitations

1. The `margin` parameter is parsed but not currently enforced in partition balancing
2. Graph layout is fixed to circular arrangement
3. Maximum of 100 vertices recommended for performance and visibility
4. No undo functionality (use Reset to return to start)

## Example Use Cases

- Educational: Teaching graph algorithms and optimization
- Research: Analyzing KL algorithm behavior on different graph topologies
- Prototyping: Testing partition quality for distributed computing scenarios

## Technical Notes

### Why CSR Format?

CSR (Compressed Sparse Row) is ideal for:
- Sparse graphs where |E| << |V|²
- Read-heavy workloads (algorithm iterates over neighbors frequently)
- Memory efficiency (no storage for non-existent edges)

### Algorithm Enhancements

The implementation includes several optimizations:
- Early termination when no improvement is found
- Maximum pass limit to prevent infinite loops
- Efficient gain computation with locked vertex tracking
- Best-prefix selection (standard KL approach)

## Future Enhancements

- Implement margin/balance constraint enforcement
- Add force-directed graph layout
- Support for weighted graphs
- Export visualization as animation
- Parallel partition refinement
- Comparison with other algorithms (spectral partitioning, METIS, etc.)

