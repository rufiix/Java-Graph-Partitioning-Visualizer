import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A graphical user interface for visualizing and performing graph partitioning.
 *
 * This application allows users to load a graph from a specified file format,
 * partition it into a given number of parts using a heuristic based on the
 * Kernighan-Lin algorithm, and visualize the result. The partitioning aims
 * to minimize the number of "cut edges" (edges between different partitions)
 * while respecting a balance margin for the size of the partitions.
 *
 * The application presents a "before" and "after" view of the graph.
 *

 */
public class GraphPartitionGUI extends JFrame {

    // --- Constants ---
    private static final int NODE_DIAMETER = 20;
    private static final int MAX_REFINEMENT_PASSES = 10; // Prevents infinite loops

    // --- GUI Components ---
    private final GraphPanel beforePanel;
    private final GraphPanel afterPanel;
    private final JButton partitionButton;
    private final JButton saveButton;
    private final JLabel cutEdgesLabel;

    // --- State Variables ---
    private Graph graph;
    private Partition[] partitions;

    // =================================================================================
    // Data Models (Static Nested Classes)
    // =================================================================================

    /**
     * Represents a graph using the Compressed Sparse Row (CSR) format.
     */
    static class Graph {
        int numVertices;
        int[] vertexAdjacency; // Concatenated list of neighbors for all vertices
        int[] adjacencyPointers; // Pointers to the start of each vertex's neighbors in the adjacency list
    }

    /**
     * Represents a single partition (subset) of the graph's vertices.
     */
    static class Partition {
        int[] vertices;
    }

    // =================================================================================
    // GUI Panel for Drawing
    // =================================================================================

    /**
     * A custom JPanel for rendering the graph structure.
     */
    class GraphPanel extends JPanel {
        private Graph displayedGraph;
        private Partition[] displayedPartitions;
        private final boolean isAfterPartitionView;

        public GraphPanel(boolean isAfterPartitionView) {
            this.isAfterPartitionView = isAfterPartitionView;
            this.setBorder(new TitledBorder(isAfterPartitionView ? "After Partitioning" : "Before Partitioning"));
        }

        public void setGraph(Graph graph) {
            this.displayedGraph = graph;
            this.displayedPartitions = null; // Reset partitions when a new graph is set
            repaint();
        }

        public void setPartitions(Partition[] partitions) {
            this.displayedPartitions = partitions;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (displayedGraph == null) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int radius = (Math.min(width, height) / 2) - (NODE_DIAMETER * 2);
            int centerX = width / 2;
            int centerY = height / 2;

            Point[] positions = new Point[displayedGraph.numVertices];
            for (int i = 0; i < displayedGraph.numVertices; i++) {
                double angle = 2 * Math.PI * i / displayedGraph.numVertices;
                positions[i] = new Point(
                    (int) (centerX + radius * Math.cos(angle)),
                    (int) (centerY + radius * Math.sin(angle))
                );
            }

            // Map each vertex to its partition index for coloring
            int[] vertexToPartMap = new int[displayedGraph.numVertices];
            Arrays.fill(vertexToPartMap, -1);
            if (isAfterPartitionView && displayedPartitions != null) {
                for (int i = 0; i < displayedPartitions.length; i++) {
                    for (int vertex : displayedPartitions[i].vertices) {
                        vertexToPartMap[vertex] = i;
                    }
                }
            }

            drawEdges(g2d, positions, vertexToPartMap);
            drawVertices(g2d, positions, vertexToPartMap);
        }

        private void drawEdges(Graphics2D g2d, Point[] positions, int[] vertexToPartMap) {
            for (int u = 0; u < displayedGraph.numVertices; u++) {
                for (int i = displayedGraph.adjacencyPointers[u]; i < displayedGraph.adjacencyPointers[u + 1]; i++) {
                    int v = displayedGraph.vertexAdjacency[i];
                    if (u < v) { // Draw each edge only once
                        // Highlight cut edges in the "after" panel
                        if (isAfterPartitionView && displayedPartitions != null && vertexToPartMap[u] != vertexToPartMap[v]) {
                            g2d.setColor(Color.RED);
                            g2d.setStroke(new BasicStroke(2.0f));
                        } else {
                            g2d.setColor(Color.DARK_GRAY);
                            g2d.setStroke(new BasicStroke(1.0f));
                        }
                        g2d.drawLine(positions[u].x, positions[u].y, positions[v].x, positions[v].y);
                    }
                }
            }
            g2d.setStroke(new BasicStroke(1.0f)); // Reset stroke
        }

        private void drawVertices(Graphics2D g2d, Point[] positions, int[] vertexToPartMap) {
            Color[] colors = {Color.ORANGE, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW};
            for (int v = 0; v < displayedGraph.numVertices; v++) {
                int x = positions[v].x - NODE_DIAMETER / 2;
                int y = positions[v].y - NODE_DIAMETER / 2;

                if (isAfterPartitionView && vertexToPartMap[v] != -1) {
                    g2d.setColor(colors[vertexToPartMap[v] % colors.length]);
                } else {
                    g2d.setColor(Color.GRAY);
                }
                g2d.fillOval(x, y, NODE_DIAMETER, NODE_DIAMETER);

                g2d.setColor(Color.BLACK);
                g2d.drawOval(x, y, NODE_DIAMETER, NODE_DIAMETER);

                g2d.setColor(Color.BLACK);
                String label = String.valueOf(v);
                FontMetrics fm = g2d.getFontMetrics();
                int labelWidth = fm.stringWidth(label);
                g2d.drawString(label, x + (NODE_DIAMETER - labelWidth) / 2, y + fm.getAscent() + (NODE_DIAMETER - fm.getHeight()) / 2);
            }
        }
    }

    // =================================================================================
    // Main Application Constructor
    // =================================================================================

    public GraphPartitionGUI() {
        setTitle("Graph Partitioning Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Control Panel ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JTextField partsField = new JTextField("2", 4);
        JTextField marginField = new JTextField("10.0", 5);
        JButton loadButton = new JButton("Load Graph");
        partitionButton = new JButton("Partition Graph");
        saveButton = new JButton("Save Result");
        cutEdgesLabel = new JLabel("Cut Edges: -");

        controlPanel.add(new JLabel("Parts:"));
        controlPanel.add(partsField);
        controlPanel.add(new JLabel("Balance Margin (%):"));
        controlPanel.add(marginField);
        controlPanel.add(loadButton);
        controlPanel.add(partitionButton);
        controlPanel.add(saveButton);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(cutEdgesLabel);

        // --- Visualization Panels ---
        beforePanel = new GraphPanel(false);
        afterPanel = new GraphPanel(true);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, beforePanel, afterPanel);
        splitPane.setResizeWeight(0.5); // Distribute space evenly
        splitPane.setDividerLocation(0.5);

        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // --- Initial GUI State ---
        partitionButton.setEnabled(false);
        saveButton.setEnabled(false);

        // --- Button Actions ---
        loadButton.addActionListener(e -> handleLoadGraph());
        partitionButton.addActionListener(e -> handlePartitionGraph(partsField.getText(), marginField.getText()));
        saveButton.addActionListener(e -> handleSaveResult());
    }

    // =================================================================================
    // Event Handlers
    // =================================================================================

    private void handleLoadGraph() {
        JFileChooser fileChooser = new JFileChooser(".");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                this.graph = loadGraphFromFile(fileChooser.getSelectedFile().getPath());
                beforePanel.setGraph(graph);
                afterPanel.setGraph(graph); // Show graph structure on both sides
                partitionButton.setEnabled(true);
                saveButton.setEnabled(false);
                cutEdgesLabel.setText("Cut Edges: -");
                JOptionPane.showMessageDialog(this, "Graph loaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Error loading graph: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handlePartitionGraph(String partsText, String marginText) {
        try {
            int numParts = Integer.parseInt(partsText);
            float margin = Float.parseFloat(marginText);

            if (numParts < 2 || numParts > graph.numVertices) {
                throw new NumberFormatException("Number of parts must be between 2 and the number of vertices.");
            }
            if (margin < 0) {
                throw new NumberFormatException("Margin cannot be negative.");
            }

            this.partitions = partitionGraph(graph, numParts, margin);
            afterPanel.setPartitions(this.partitions);
            
            int cutEdges = calculateCutEdges(graph, partitions);
            cutEdgesLabel.setText(String.format("Cut Edges: %d", cutEdges));

            saveButton.setEnabled(true);
            JOptionPane.showMessageDialog(this, "Graph partitioned successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Partitioning failed: " + ex.getMessage(), "Partitioning Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSaveResult() {
        JFileChooser fileChooser = new JFileChooser(".");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                saveResultToFile(fileChooser.getSelectedFile().getPath(), partitions);
                JOptionPane.showMessageDialog(this, "Result saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving result: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =================================================================================
    // File I/O Methods
    // =================================================================================

    /**
     * Loads a graph from a text file.
     * Expected format:
     * Line 1: Number of vertices (N)
     * Line 2: Semicolon-separated list of neighbors for all vertices (adjacency list).
     * Line 3: Semicolon-separated list of N+1 pointers into the adjacency list.
     */
    private Graph loadGraphFromFile(String filename) throws IOException, IllegalArgumentException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            Graph newGraph = new Graph();
            newGraph.numVertices = Integer.parseInt(reader.readLine().trim());

            String[] adjTokens = reader.readLine().trim().split(";");
            newGraph.vertexAdjacency = Arrays.stream(adjTokens).mapToInt(Integer::parseInt).toArray();

            String[] ptrTokens = reader.readLine().trim().split(";");
            newGraph.adjacencyPointers = Arrays.stream(ptrTokens).mapToInt(Integer::parseInt).toArray();

            if (newGraph.adjacencyPointers.length != newGraph.numVertices + 1) {
                throw new IllegalArgumentException("Invalid number of row pointers.");
            }
            return newGraph;
        }
    }
    
    /**
     * Saves the partitioning result to a text file.
     */
    private void saveResultToFile(String filename, Partition[] resultPartitions) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(resultPartitions.length); // Number of parts
            writer.println(calculateCutEdges(graph, resultPartitions)); // Number of cut edges
            for (Partition part : resultPartitions) {
                writer.print(part.vertices.length);
                for (int vertex : part.vertices) {
                    writer.print(" " + vertex);
                }
                writer.println();
            }
        }
    }

    // =================================================================================
    // Core Partitioning Logic
    // =================================================================================

    /**
     * Partitions the graph into a specified number of parts.
     *
     * @param graphToPartition The graph to partition.
     * @param numParts The target number of partitions.
     * @param margin The allowed percentage deviation from the ideal partition size.
     * @return An array of Partition objects.
     */
    private Partition[] partitionGraph(Graph graphToPartition, int numParts, float margin) {
        // 1. Create a random initial partition.
        int[] vertexToPartMap = createRandomInitialPartition(graphToPartition.numVertices, numParts);

        // 2. Iteratively refine the partition by applying Kernighan-Lin to pairs.
        boolean improvementMade = true;
        int passes = 0;
        while (improvementMade && passes < MAX_REFINEMENT_PASSES) {
            improvementMade = false;
            for (int i = 0; i < numParts; i++) {
                for (int j = i + 1; j < numParts; j++) {
                    if (refinePartitionPair(graphToPartition, vertexToPartMap, i, j)) {
                        improvementMade = true;
                    }
                }
            }
            passes++;
        }

        // 3. Verify balance constraints.
        checkBalance(graphToPartition.numVertices, numParts, margin, vertexToPartMap);

        // 4. Convert the final map into Partition objects.
        return createPartitionsFromMap(vertexToPartMap, numParts);
    }
    
    /**
     * Creates an initial random assignment of vertices to partitions.
     */
    private int[] createRandomInitialPartition(int numVertices, int numParts) {
        int[] mapping = new int[numVertices];
        List<Integer> vertexIndices = IntStream.range(0, numVertices).boxed().collect(Collectors.toList());
        Collections.shuffle(vertexIndices);

        int currentPart = 0;
        for (Integer vertexIndex : vertexIndices) {
            mapping[vertexIndex] = currentPart;
            currentPart = (currentPart + 1) % numParts;
        }
        return mapping;
    }

    /**
     * Applies a simplified, greedy Kernighan-Lin heuristic to a pair of partitions
     * to reduce the number of cut edges between them.
     *
     * @return true if any vertices were swapped, false otherwise.
     */
    private boolean refinePartitionPair(Graph graphToRefine, int[] vertexToPartMap, int partIdx1, int partIdx2) {
        boolean overallImprovement = false;
        
        List<Integer> part1Vertices = new ArrayList<>();
        List<Integer> part2Vertices = new ArrayList<>();
        for(int v=0; v < graphToRefine.numVertices; ++v) {
            if(vertexToPartMap[v] == partIdx1) part1Vertices.add(v);
            if(vertexToPartMap[v] == partIdx2) part2Vertices.add(v);
        }
        
        boolean[] locked = new boolean[graphToRefine.numVertices];
        int iterations = Math.min(part1Vertices.size(), part2Vertices.size());

        for (int iter = 0; iter < iterations; iter++) {
            // Calculate D values (gains) for all unlocked vertices in the two partitions.
            int[] gains = new int[graphToRefine.numVertices];
            computeGains(graphToRefine, vertexToPartMap, partIdx1, partIdx2, gains, locked);
            
            // Find the best pair of vertices to swap.
            int bestV1 = -1, bestV2 = -1;
            int maxGain = Integer.MIN_VALUE;

            for (int v1 : part1Vertices) {
                if (locked[v1]) continue;
                for (int v2 : part2Vertices) {
                    if (locked[v2]) continue;
                    
                    int edgeCost = isConnected(graphToRefine, v1, v2) ? 2 : 0;
                    int currentGain = gains[v1] + gains[v2] - edgeCost;

                    if (currentGain > maxGain) {
                        maxGain = currentGain;
                        bestV1 = v1;
                        bestV2 = v2;
                    }
                }
            }

            if (maxGain > 0) {
                // Perform the swap and lock the vertices.
                vertexToPartMap[bestV1] = partIdx2;
                vertexToPartMap[bestV2] = partIdx1;
                locked[bestV1] = true;
                locked[bestV2] = true;
                overallImprovement = true;
            } else {
                // No positive gain swap found, stop refining this pair.
                break;
            }
        }
        return overallImprovement;
    }

    /**
     * Computes the gain (D-value) for each vertex in the specified partitions.
     * Gain = (Number of external edges) - (Number of internal edges).
     */
    private void computeGains(Graph graph, int[] vertexToPartMap, int partIdx1, int partIdx2, int[] gains, boolean[] locked) {
        for (int v = 0; v < graph.numVertices; v++) {
            if (locked[v] || (vertexToPartMap[v] != partIdx1 && vertexToPartMap[v] != partIdx2)) {
                continue;
            }

            int internalCost = 0;
            int externalCost = 0;
            for (int i = graph.adjacencyPointers[v]; i < graph.adjacencyPointers[v + 1]; i++) {
                int neighbor = graph.vertexAdjacency[i];
                if (vertexToPartMap[v] == vertexToPartMap[neighbor]) {
                    internalCost++;
                } else {
                    externalCost++;
                }
            }
            gains[v] = externalCost - internalCost;
        }
    }

    /**
     * Checks if the final partition respects the balance margin.
     * Throws a RuntimeException if the constraint is violated.
     */
    private void checkBalance(int numVertices, int numParts, float margin, int[] vertexToPartMap) {
        int[] partSizes = new int[numParts];
        for (int partIndex : vertexToPartMap) {
            partSizes[partIndex]++;
        }

        double idealSize = (double) numVertices / numParts;
        int maxSize = (int) Math.ceil(idealSize * (1.0 + margin / 100.0));

        for (int size : partSizes) {
            if (size > maxSize) {
                throw new RuntimeException(String.format("Balance constraint failed. Part size %d exceeds max allowed size %d.", size, maxSize));
            }
        }
    }
    
    // =================================================================================
    // Utility Methods
    // =================================================================================

    /**
     * Calculates the total number of cut edges in a partitioned graph.
     */
    private int calculateCutEdges(Graph graph, Partition[] resultPartitions) {
        if (resultPartitions == null) return 0;
        int[] vertexToPartMap = new int[graph.numVertices];
        for (int i = 0; i < resultPartitions.length; i++) {
            for (int vertex : resultPartitions[i].vertices) {
                vertexToPartMap[vertex] = i;
            }
        }

        int cutEdges = 0;
        for (int u = 0; u < graph.numVertices; u++) {
            for (int i = graph.adjacencyPointers[u]; i < graph.adjacencyPointers[u + 1]; i++) {
                int v = graph.vertexAdjacency[i];
                if (u < v && vertexToPartMap[u] != vertexToPartMap[v]) {
                    cutEdges++;
                }
            }
        }
        return cutEdges;
    }

    /**
     * Checks if two vertices are connected by an edge.
     */
    private boolean isConnected(Graph graph, int u, int v) {
        for (int i = graph.adjacencyPointers[u]; i < graph.adjacencyPointers[u + 1]; i++) {
            if (graph.vertexAdjacency[i] == v) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Converts the vertex-to-partition map into an array of Partition objects.
     */
    private Partition[] createPartitionsFromMap(int[] vertexToPartMap, int numParts) {
        List<List<Integer>> tempPartitions = new ArrayList<>();
        for (int i = 0; i < numParts; i++) {
            tempPartitions.add(new ArrayList<>());
        }

        for (int v = 0; v < vertexToPartMap.length; v++) {
            tempPartitions.get(vertexToPartMap[v]).add(v);
        }

        Partition[] finalPartitions = new Partition[numParts];
        for (int i = 0; i < numParts; i++) {
            finalPartitions[i] = new Partition();
            finalPartitions[i].vertices = tempPartitions.get(i).stream().mapToInt(Integer::intValue).toArray();
        }
        return finalPartitions;
    }

    // =================================================================================
    // Application Entry Point
    // =================================================================================

    public static void main(String[] args) {
        // Ensure GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new GraphPartitionGUI().setVisible(true));
    }
}
