````markdown
# Java Graph Partitioning Visualizer

## Overview

This project is a **Java Swing desktop application** that visualizes the process of **graph partitioning** using the **Kernighan–Lin algorithm**.  
The algorithm is a heuristic method for dividing an undirected graph into balanced subsets while minimizing the number of **cut edges** (edges connecting vertices in different subsets).

The application provides an **interactive graphical interface (GUI)** that allows you to:
- Load a graph from file or generate a random graph.
- Visualize the graph before and during the partitioning process.
- Configure partitioning parameters (number of partitions, balance margin).
- Step through the algorithm manually or watch an animated visualization.
- Save the final partitioning results to a file.

---

## Features

- **Graph Visualization**
  - Circular layout for vertices.
  - Side-by-side display of the initial and partitioned graphs.
  - Highlighted cut edges (in red) for clarity.
  - Swapped vertices emphasized during algorithm steps.

- **Kernighan–Lin Algorithm**
  - Iterative heuristic for reducing cut edges.
  - Random initial partitioning into configurable subsets.
  - Pass-by-pass refinement until no further improvements.

- **Interactive Controls**
  - Load graphs from files or generate random graphs.
  - Adjust number of partitions and allowed margin.
  - Step, Play/Pause, and Reset controls for visualization.
  - Adjustable animation speed with a slider.

- **File I/O**
  - **Input**: Reads graphs from a text file in CSR format (Compressed Sparse Row).
  - **Output**: Saves the number of parts, cut edges, and vertex assignments per partition.

- **Color-Coded Partitions**
  - Vertices are color-coded in the partitioned graph view.
  - Easy identification of subsets after algorithm execution.

---

## How to Run

### Prerequisites
- **Java Development Kit (JDK) 8 or higher** installed on your system.

### Compilation & Execution

1. **Navigate to the source directory**:
   ```bash
   cd path/to/your/project/src
````

2. **Compile the program**:

   ```bash
   javac GraphPartitionGUI.java
   ```

3. **Run the application**:

   ```bash
   java GraphPartitionGUI
   ```

---

## Usage Instructions

1. **Load Graph**

   * Click **"Load File"** to select a graph file in CSR format.
   * Or click **"Random Graph"** to generate one based on vertex count and edge density.

2. **Set Parameters**

   * **Parts**: Number of desired partitions (minimum 2).
   * **Margin (%)**: Allowed deviation in partition sizes.

3. **Start Algorithm**

   * Click **"Start"** to run the algorithm.
   * Use **"Step"** to move step-by-step or **"Play"** to animate automatically.
   * Use **"Reset"** to restart from the initial partition.

4. **Save Results**

   * Click **"Save"** to write the final partitions and number of cut edges to a file.

---

## Graph File Format

When loading a graph from file, the format is:

```
<numVertices>
<adjacency array separated by ';'>
<pointers array separated by ';'>
```

### Example

```
5
1;2;0;2;0;1;3;4;2;3;2;4
0;2;4;7;9;12
```

* `numVertices` → number of vertices in the graph
* `adjacency` → concatenated adjacency lists of all vertices
* `pointers` → indices showing where each vertex’s neighbor list starts in the adjacency array

---

## Algorithm Overview (Kernighan–Lin)

1. Generate an initial **random partition** of the graph.
2. Iteratively **choose pairs of vertices** from different partitions.
3. Compute the **gain** in reducing cut edges for swapping.
4. Perform the swap if beneficial.
5. Repeat until no further improvements are found.
6. Output the final partitioning with minimal cut edges.

---

## Technologies

* **Java 8+**
* **Swing (GUI)**
* **Compressed Sparse Row (CSR)** graph representation


```
```
