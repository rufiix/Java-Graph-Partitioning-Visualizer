# Java Graph Partitioning Tool with Kernighan-Lin Algorithm

## Overview

This project is a Java Swing application that provides a graphical user interface (GUI) for partitioning undirected graphs. It implements the Kernighan-Lin heuristic algorithm to partition a graph into a specified number of balanced parts while minimizing the number of "cut" edges (edges connecting vertices in different partitions).

The application visualizes the graph before and after the partitioning process, allowing for a clear comparison of the results. Users can load graph data from a text file, configure partitioning parameters, and save the output.

---

## Features

* **Interactive GUI**: A user-friendly interface built with Java Swing for loading, partitioning, and saving graphs.
* **Graph Visualization**: Renders the graph in a circular layout, with two distinct panels showing the state *before* and *after* partitioning. Partitions are color-coded for easy identification.
* **Kernighan-Lin Algorithm**: Employs the iterative Kernighan-Lin algorithm to optimize the graph partition and reduce edge cuts.
* **Configurable Partitioning**: Users can specify the desired number of partitions and a balance tolerance margin (in percent).
* **File I/O**:
    * Load graph structures from formatted text files.
    * Save the partitioning results, including the number of cut edges and vertex assignments for each partition.
* **Cut Edge Calculation**: Automatically calculates and displays the number of edges that cross partition boundaries after the algorithm is run.

---

## How to Compile and Run

### Prerequisites

* Java Development Kit (JDK) 8 or higher.

### Compilation & Execution

1.  Navigate to the `src` directory in your terminal.
2.  Compile the Java source file:
    ```bash
    javac GraphPartitionGUI.java
    ```
3.  Run the application:
    ```bash
    java GraphPartitionGUI
    ```
The application window should now appear.

---

## Usage Guide

1.  **Load Graph**: Click the **"Load Graph"** button and select a valid input file. The graph will be displayed in the "before" panel.
2.  **Set Parameters**:
    * Enter the desired number of partitions in the **"Parts"** text field.
    * Specify the balance tolerance in the **"Margin (%)"** field. This controls the maximum allowed size difference between partitions.
3.  **Partition Graph**: Click the **"Partition Graph"** button. The algorithm will run, and the result will be displayed in the "after" panel with color-coded partitions. A dialog box will show the final number of cut edges.
4.  **Save Result**: Click **"Save Result"** to save the output to a text file.

---

### Input & Output File Formats

**Input File Format:**
The graph data must be structured as follows:
* **Line 1**: Total number of vertices.
* **Line 2**: A semicolon-separated list of vertex indices (adjacency list).
* **Line 3**: A semicolon-separated list of row pointers for the adjacency list (length must be `numVertices + 1`).
* **Line 4**: A semicolon-separated list of vertex groups.
* **Line 5**: A semicolon-separated list of group pointers.

**Example Input:**
