Graph Partitioning Application
Overview
This Java application provides a graphical user interface (GUI) for partitioning an undirected graph into a specified number of parts using the Kernighan-Lin algorithm. The application visualizes the graph before and after partitioning, allows users to load graph data from a file, specify the number of parts and a margin for partition sizes, and save the partitioning results. It is built using Java's Swing library for the GUI and implements graph visualization and partitioning logic.
Features

Graph Loading: Load a graph from a text file with a specific format (number of vertices, vertex indices, row pointers, vertex groups, and group pointers).
Graph Visualization: Display the graph in a circular layout with vertices and edges, using two panels to show the graph before and after partitioning.
Partitioning: Divide the graph into a user-specified number of parts using the Kernighan-Lin algorithm, ensuring balanced partitions within a user-defined margin.
Cut Edge Calculation: Compute the number of edges cut between partitions.
Result Saving: Save the partitioning results (number of parts, cut edges, and vertex assignments) to a file.
User Interface: Includes controls for loading graphs, setting the number of parts and margin, initiating partitioning, and saving results.

Code Structure
The application is implemented in a single Java class, GraphPartitionGUI, with the following key components:

Graph Class: A static inner class representing the graph using an adjacency list format with fields for the number of vertices (numVertices), number of edges (numEdges), vertex indices (vertexIndices), row pointers (rowPointers), vertex groups (vertexGroups), and group pointers (groupPointers).
Part Class: A static inner class representing a partition, containing an array of vertices (vertices) and the number of vertices (numVertices).
GraphPanel Class: A custom JPanel for rendering the graph. It draws vertices in a circular layout and edges as lines, with color-coded partitions after processing.
GUI Setup: The main GraphPartitionGUI class extends JFrame and sets up a control panel (with buttons and text fields) and two GraphPanel instances for visualization, using a JSplitPane for side-by-side display.
Core Methods:
loadGraph: Reads graph data from a file.
partitionGraph: Implements the Kernighan-Lin algorithm to partition the graph, ensuring partition sizes are within the specified margin.
kernighanLin: Performs iterative vertex swaps between two partitions to minimize cut edges.
calculateCutEdges: Counts edges crossing partition boundaries.
saveResult: Saves the partitioning results to a file.


Event Handling: Action listeners for buttons to load graphs, partition, and save results, with input validation and error messages.

How to Run

Prerequisites: Ensure you have Java Development Kit (JDK) installed (version 8 or higher recommended).
Compile and Run:javac GraphPartitionGUI.java
java GraphPartitionGUI


Input File Format:
First line: Number of vertices (numVertices).
Second line: Semicolon-separated list of vertex indices representing edges (vertexIndices).
Third line: Semicolon-separated list of row pointers (rowPointers, length = numVertices + 1).
Fourth line: Semicolon-separated list of vertex groups (vertexGroups).
Fifth line: Semicolon-separated list of group pointers (groupPointers).
Example:6
1;2;0;2;0;1;3;4;5;3;4;5
0;2;4;6;7;9;12
0;0;1;1;2;2
0;2;4




Usage:
Click "Load Graph" to select an input file.
Enter the number of parts and margin percentage in the text fields.
Click "Partition Graph" to process the graph and view the partitioned result.
Click "Save Result" to save the partitioning output to a file.



Output File Format
The output file contains:

First line: Number of parts.
Second line: Number of cut edges.
Subsequent lines: For each part, the number of vertices followed by the vertex indices.
Example:2
3
3 0 1 2
3 3 4 5



Dependencies

Java Standard Library (Swing, AWT, IO)
No external libraries required.

Notes

The Kernighan-Lin algorithm iteratively improves an initial random partition by swapping vertices to minimize cut edges while respecting the margin constraint.
The margin specifies the maximum allowable deviation (in percentage) from the ideal partition size (total vertices divided by number of parts).
The visualization uses a circular layout for simplicity, with vertices color-coded by partition in the "after" panel.
Error handling includes checks for invalid inputs, file errors, and partition size constraints.

Limitations

The application assumes the input graph is undirected and properly formatted.
The Kernighan-Lin algorithm may not always find the global minimum for cut edges, as it is a heuristic.
Large graphs may impact performance due to the iterative nature of the algorithm.

License
This project is for educational purposes and does not include a specific license. Use and modify at your own discretion.
