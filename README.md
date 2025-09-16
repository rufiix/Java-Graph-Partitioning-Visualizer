# Java Graph Partitioning Visualizer

## Overview

This project is a Java Swing application designed to visualize the process of graph partitioning. It implements the Kernighan-Lin algorithm, a heuristic approach for partitioning an undirected graph into a specified number of balanced subsets while minimizing the number of "cut" edges (edges connecting vertices in different subsets).

The application provides a user-friendly graphical interface (GUI) to:
* Load graph data from a text file.
* Visualize the graph before and after the partitioning process.
* Configure partitioning parameters, such as the number of desired parts and the balance tolerance (margin).
* Save the results of the partition to a file.

---

## Features

* **Graph Visualization**: Displays the graph in a clear, circular layout, showing the initial state and the final partitioned state side-by-side.
* **Kernighan-Lin Algorithm**: Employs the Kernighan-Lin heuristic to iteratively optimize the graph partition and reduce cut edges.
* **Interactive Controls**: An intuitive GUI allows users to load graph files, specify the number of partitions, define a size margin for balance, and trigger the partitioning process.
* **File I/O**:
    * Loads graph structures from a custom-formatted text file.
    * Saves the partitioning results, including the number of parts, cut edges, and vertex assignments for each subset.
* **Color-Coded Output**: Vertices in the "after" panel are color-coded based on their assigned partition, making the results easy to interpret visually.

---

## How to Run

### Prerequisites
* Java Development Kit (JDK) 8 or higher.

### Compilation & Execution

1.  **Navigate to the source directory**:
    Open a terminal and change the directory to `src/`.

    ```bash
    cd path/to/your/project/src
    ```

2.  **Compile the Java code**:
    Use `javac` to compile the source file.

    ```bash
    javac GraphPartitionGUI.java
    ```

3.  **Run the application**:
    Use the `java` command to launch the GUI.

    ```bash
    java GraphPartitionGUI
    ```

### Usage Instructions

1.  **Load Graph**: Click the "Load Graph" button to open a file chooser and select a valid input file.
2.  **Set Parameters**:
    * **Parts**: Enter the desired number of partitions in the "Parts" text field.
    * **Margin (%)**: Specify the allowed size deviation for each partition. For example, a 10% margin on a graph of 100 vertices partitioned into 2 parts would allow each partition to have between 45 and 55 vertices.
3.  **Partition**: Click "Partition Graph" to execute the algorithm. The right-hand panel will update to show the partitioned graph.
4.  **Save Result**: Click "Save Result" to save the outcome to a text file.

---
