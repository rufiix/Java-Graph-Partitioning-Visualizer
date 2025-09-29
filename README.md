
# Graph Partitioning Visualizer – Kernighan–Lin Algorithm

A desktop application written in **Java (Swing)** that visualizes the **Kernighan–Lin algorithm**,  
a heuristic method for partitioning graphs into balanced subsets while minimizing the number of edge cuts.

---

## 📌 Features

- **Interactive GUI (Swing)**
  - Load graph from file or generate a random one
  - Step-by-step visualization of the algorithm
  - Play / Pause / Reset controls for automatic animation
  - Adjustable animation speed (slider)

- **Graph Partitioning**
  - Implementation of the **Kernighan–Lin algorithm**
  - Random initial partitioning into multiple subsets
  - Iterative refinement to minimize edge cuts
  - Visualization of cut edges (highlighted in red)

- **Visualization**
  - Graph nodes displayed in a circular layout
  - Edges highlighted when crossing partitions
  - Swapped vertices emphasized during algorithm steps
  - Algorithm log window with textual description of each step

- **Graph Input/Output**
  - Load graphs from file (CSR format: number of vertices, adjacency, pointers)
  - Save final partitions and cut edges result to file
  - Random graph generator with configurable size and density

---

## 🖥️ Screenshots

*(You can add screenshots here by saving PNGs of the running application and placing them in a `screenshots/` folder, e.g.)*

![Initial Graph](screenshots/initial.png)  
![Algorithm Progress](screenshots/progress.png)  

---

## ⚙️ Installation & Running

### 1. Clone repository
```bash
git clone https://github.com/yourusername/Java-Graph-Partitioning-Visualizer.git
cd Java-Graph-Partitioning-Visualizer
````

### 2. Compile

```bash
javac GraphPartitionGUI.java
```

### 3. Run

```bash
java GraphPartitionGUI
```

---

## 📂 Graph File Format

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

* **numVertices** – number of vertices in the graph
* **adjacency** – concatenation of adjacency lists for all vertices
* **pointers** – index in adjacency array where each vertex’s neighbor list starts

---

## 📊 Algorithm Overview (Kernighan–Lin)

1. Start with an initial **random partition** of the graph.
2. Iteratively **select pairs of vertices** from different partitions.
3. Compute **gain** in edge cut reduction for swapping.
4. Perform the swap if it reduces the cut size.
5. Continue until no further improvement is possible.
6. Output the partitioning with the minimal number of cut edges.

---

## 🛠️ Technologies

* **Java 8+**
* **Swing** (GUI)
* **CSR Graph Representation** (Compressed Sparse Row)

---

