import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Graph Partitioning Visualizer using Kernighan-Lin Algorithm
 * Shows step-by-step visualization of the partitioning process
 */
public class GraphPartitionGUI extends JFrame {

    private static final int NODE_SIZE = 20;
    private static final int MAX_PASSES = 10;

    // GUI components
    private GraphPanel beforePanel;
    private GraphPanel afterPanel;
    private JButton partitionButton;
    private JButton saveButton;
    private JButton stepButton;
    private JButton playButton;
    private JButton resetButton;
    private JLabel cutEdgesLabel;
    private JLabel stepLabel;
    private JTextArea logArea;
    private JSlider speedSlider;

    // Data
    private Graph graph;
    private Partition[] partitions;
    private List<AlgorithmStep> steps;
    private int currentStep;
    private javax.swing.Timer animationTimer;
    private boolean isPlaying;

    // Graph data structure
    static class Graph {
        int numVertices;
        int[] adjacency;      // all neighbors concatenated
        int[] pointers;       // start index for each vertex's neighbors
    }

    static class Partition {
        int[] vertices;
    }

    // Single step in the algorithm
    static class AlgorithmStep {
        int[] partitionMap;
        int cutEdges;
        String description;
        int vertex1;  // highlighted vertex
        int vertex2;  // highlighted vertex

        AlgorithmStep(int[] map, int cuts, String desc) {
            this.partitionMap = map.clone();
            this.cutEdges = cuts;
            this.description = desc;
            this.vertex1 = -1;
            this.vertex2 = -1;
        }

        void highlight(int v1, int v2) {
            this.vertex1 = v1;
            this.vertex2 = v2;
        }
    }

    // Panel for drawing the graph
    class GraphPanel extends JPanel {
        private Graph g;
        private int[] currentMap;
        private boolean showAfter;
        private int highlight1 = -1;
        private int highlight2 = -1;

        GraphPanel(boolean after) {
            this.showAfter = after;
            setBorder(new TitledBorder(after ? "Algorithm Progress" : "Initial State"));
        }

        void setGraph(Graph graph) {
            this.g = graph;
            this.currentMap = null;
            repaint();
        }

        void updateView(int[] map, int v1, int v2) {
            this.currentMap = map;
            this.highlight1 = v1;
            this.highlight2 = v2;
            repaint();
        }

        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (g == null) return;

            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int radius = (Math.min(w, h) / 2) - NODE_SIZE * 2;
            int cx = w / 2;
            int cy = h / 2;

            // Calculate positions
            Point[] positions = new Point[g.numVertices];
            for (int i = 0; i < g.numVertices; i++) {
                double angle = 2 * Math.PI * i / g.numVertices;
                int x = (int) (cx + radius * Math.cos(angle));
                int y = (int) (cy + radius * Math.sin(angle));
                positions[i] = new Point(x, y);
            }

            // Draw edges
            for (int u = 0; u < g.numVertices; u++) {
                for (int i = g.pointers[u]; i < g.pointers[u + 1]; i++) {
                    int v = g.adjacency[i];
                    if (u < v) {
                        boolean isCut = false;
                        if (showAfter && currentMap != null) {
                            isCut = (currentMap[u] != currentMap[v]);
                        }

                        if (isCut) {
                            g2.setColor(Color.RED);
                            g2.setStroke(new BasicStroke(2.5f));
                        } else {
                            g2.setColor(Color.GRAY);
                            g2.setStroke(new BasicStroke(1.0f));
                        }
                        g2.drawLine(positions[u].x, positions[u].y, positions[v].x, positions[v].y);
                    }
                }
            }

            // Draw vertices
            Color[] colors = {Color.ORANGE, Color.BLUE, Color.GREEN, Color.MAGENTA,
                    Color.CYAN, Color.PINK, Color.YELLOW};

            for (int v = 0; v < g.numVertices; v++) {
                int x = positions[v].x - NODE_SIZE / 2;
                int y = positions[v].y - NODE_SIZE / 2;

                // Highlight swapped vertices
                if (showAfter && (v == highlight1 || v == highlight2)) {
                    g2.setColor(Color.YELLOW);
                    g2.setStroke(new BasicStroke(3.0f));
                    g2.drawOval(x - 3, y - 3, NODE_SIZE + 6, NODE_SIZE + 6);
                    g2.setStroke(new BasicStroke(1.0f));
                }

                // Color by partition
                if (showAfter && currentMap != null) {
                    g2.setColor(colors[currentMap[v] % colors.length]);
                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                }
                g2.fillOval(x, y, NODE_SIZE, NODE_SIZE);
                g2.setColor(Color.BLACK);
                g2.drawOval(x, y, NODE_SIZE, NODE_SIZE);

                // Draw label
                String label = String.valueOf(v);
                FontMetrics fm = g2.getFontMetrics();
                int labelW = fm.stringWidth(label);
                int labelX = x + (NODE_SIZE - labelW) / 2;
                int labelY = y + (NODE_SIZE + fm.getAscent()) / 2 - 2;
                g2.drawString(label, labelX, labelY);
            }
        }
    }

    // Constructor
    public GraphPartitionGUI() {
        setTitle("Graph Partitioning - Kernighan-Lin Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        // Control panel
        JPanel controls = new JPanel(new FlowLayout());
        JTextField partsField = new JTextField("2", 3);
        JTextField marginField = new JTextField("10.0", 4);
        JButton loadBtn = new JButton("Load File");
        JButton randomBtn = new JButton("Random Graph");
        partitionButton = new JButton("Start");
        saveButton = new JButton("Save");
        stepButton = new JButton("Step");
        playButton = new JButton("Play");
        resetButton = new JButton("Reset");
        cutEdgesLabel = new JLabel("Cuts: -");
        stepLabel = new JLabel("Step: -");
        speedSlider = new JSlider(100, 2000, 500);
        speedSlider.setPreferredSize(new Dimension(100, 25));

        controls.add(new JLabel("Parts:"));
        controls.add(partsField);
        controls.add(new JLabel("Margin(%):"));
        controls.add(marginField);
        controls.add(loadBtn);
        controls.add(randomBtn);
        controls.add(partitionButton);
        controls.add(new JSeparator(SwingConstants.VERTICAL));
        controls.add(resetButton);
        controls.add(stepButton);
        controls.add(playButton);
        controls.add(new JLabel("Speed:"));
        controls.add(speedSlider);
        controls.add(new JSeparator(SwingConstants.VERTICAL));
        controls.add(stepLabel);
        controls.add(cutEdgesLabel);
        controls.add(saveButton);

        // Graph panels
        beforePanel = new GraphPanel(false);
        afterPanel = new GraphPanel(true);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, beforePanel, afterPanel);
        split.setResizeWeight(0.5);

        // Log area
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(new TitledBorder("Algorithm Log"));

        JPanel center = new JPanel(new BorderLayout());
        center.add(split, BorderLayout.CENTER);
        center.add(scroll, BorderLayout.SOUTH);

        add(controls, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        // Initial state
        partitionButton.setEnabled(false);
        saveButton.setEnabled(false);
        stepButton.setEnabled(false);
        playButton.setEnabled(false);
        resetButton.setEnabled(false);

        // Actions
        loadBtn.addActionListener(e -> loadGraph());
        randomBtn.addActionListener(e -> generateRandomGraph());
        partitionButton.addActionListener(e -> startAlgorithm(partsField.getText(), marginField.getText()));
        stepButton.addActionListener(e -> nextStep());
        playButton.addActionListener(e -> togglePlay());
        resetButton.addActionListener(e -> resetView());
        saveButton.addActionListener(e -> saveResult());

        // Animation timer
        animationTimer = new javax.swing.Timer(500, e -> {
            if (currentStep < steps.size() - 1) {
                nextStep();
            } else {
                stopPlay();
            }
        });
    }

    // Load graph from file
    private void loadGraph() {
        JFileChooser fc = new JFileChooser(".");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                graph = loadGraphFile(fc.getSelectedFile().getPath());
                initializeGraphView();
                JOptionPane.showMessageDialog(this,
                        "Loaded graph with " + graph.numVertices + " vertices");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Generate random graph
    private void generateRandomGraph() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField verticesField = new JTextField("10", 5);
        JTextField densityField = new JTextField("0.3", 5);

        panel.add(new JLabel("Number of vertices:"));
        panel.add(verticesField);
        panel.add(new JLabel("Edge density (0-1):"));
        panel.add(densityField);
        panel.add(new JLabel("Higher = more edges"));
        panel.add(new JLabel(""));

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Generate Random Graph", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int n = Integer.parseInt(verticesField.getText());
                double density = Double.parseDouble(densityField.getText());

                if (n < 3 || n > 100) {
                    throw new Exception("Vertices must be between 3 and 100");
                }
                if (density < 0 || density > 1) {
                    throw new Exception("Density must be between 0 and 1");
                }

                graph = createRandomGraph(n, density);
                initializeGraphView();
                JOptionPane.showMessageDialog(this,
                        String.format("Generated random graph:\n%d vertices, %d edges",
                                graph.numVertices, countTotalEdges(graph)));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Initialize graph view after loading/generating
    private void initializeGraphView() {
        beforePanel.setGraph(graph);
        afterPanel.setGraph(graph);
        partitionButton.setEnabled(true);
        saveButton.setEnabled(false);
        stepButton.setEnabled(false);
        playButton.setEnabled(false);
        resetButton.setEnabled(false);
        cutEdgesLabel.setText("Cuts: -");
        stepLabel.setText("Step: -");
        logArea.setText("");
        steps = null;
    }

    // Create a random graph with given parameters
    private Graph createRandomGraph(int numVertices, double density) {
        Graph g = new Graph();
        g.numVertices = numVertices;

        // Build adjacency list
        List<List<Integer>> adjList = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            adjList.add(new ArrayList<>());
        }

        // Add random edges
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < numVertices; i++) {
            for (int j = i + 1; j < numVertices; j++) {
                if (rand.nextDouble() < density) {
                    adjList.get(i).add(j);
                    adjList.get(j).add(i);
                }
            }
        }

        // Ensure graph is connected
        ensureConnected(adjList, numVertices);

        // Convert to CSR format
        int totalEdges = 0;
        for (List<Integer> neighbors : adjList) {
            totalEdges += neighbors.size();
        }

        g.adjacency = new int[totalEdges];
        g.pointers = new int[numVertices + 1];

        int idx = 0;
        for (int i = 0; i < numVertices; i++) {
            g.pointers[i] = idx;
            Collections.sort(adjList.get(i));
            for (int neighbor : adjList.get(i)) {
                g.adjacency[idx++] = neighbor;
            }
        }
        g.pointers[numVertices] = idx;

        return g;
    }

    // Make sure graph is connected
    private void ensureConnected(List<List<Integer>> adjList, int n) {
        boolean[] visited = new boolean[n];
        dfs(0, adjList, visited);

        // Connect unvisited components
        for (int i = 1; i < n; i++) {
            if (!visited[i]) {
                // Connect to previous vertex
                adjList.get(i - 1).add(i);
                adjList.get(i).add(i - 1);
                dfs(i, adjList, visited);
            }
        }
    }

    // DFS for connectivity check
    private void dfs(int v, List<List<Integer>> adjList, boolean[] visited) {
        visited[v] = true;
        for (int neighbor : adjList.get(v)) {
            if (!visited[neighbor]) {
                dfs(neighbor, adjList, visited);
            }
        }
    }

    // Count total edges in graph
    private int countTotalEdges(Graph g) {
        int count = 0;
        for (int u = 0; u < g.numVertices; u++) {
            for (int i = g.pointers[u]; i < g.pointers[u + 1]; i++) {
                int v = g.adjacency[i];
                if (u < v) count++;
            }
        }
        return count;
    }

    private Graph loadGraphFile(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        Graph g = new Graph();
        g.numVertices = Integer.parseInt(br.readLine().trim());

        String[] adjTokens = br.readLine().trim().split(";");
        g.adjacency = new int[adjTokens.length];
        for (int i = 0; i < adjTokens.length; i++) {
            g.adjacency[i] = Integer.parseInt(adjTokens[i]);
        }

        String[] ptrTokens = br.readLine().trim().split(";");
        g.pointers = new int[ptrTokens.length];
        for (int i = 0; i < ptrTokens.length; i++) {
            g.pointers[i] = Integer.parseInt(ptrTokens[i]);
        }

        br.close();

        if (g.pointers.length != g.numVertices + 1) {
            throw new IOException("Invalid pointer count");
        }
        return g;
    }

    // Start the algorithm
    private void startAlgorithm(String partsStr, String marginStr) {
        try {
            int numParts = Integer.parseInt(partsStr);
            float margin = Float.parseFloat(marginStr);

            if (numParts < 2 || numParts > graph.numVertices) {
                throw new Exception("Parts must be 2-" + graph.numVertices);
            }

            logArea.setText("Starting Kernighan-Lin...\n");
            steps = runAlgorithm(graph, numParts, margin);
            currentStep = 0;

            stepButton.setEnabled(true);
            playButton.setEnabled(true);
            resetButton.setEnabled(true);
            saveButton.setEnabled(true);

            displayStep();
            logArea.append("Generated " + steps.size() + " steps\n");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Run KL algorithm with step recording
    private List<AlgorithmStep> runAlgorithm(Graph g, int numParts, float margin) {
        List<AlgorithmStep> stepList = new ArrayList<>();

        // Initial partition
        int[] map = randomPartition(g.numVertices, numParts);
        int cuts = countCuts(g, map);
        stepList.add(new AlgorithmStep(map, cuts, "Initial random partition"));

        // KL refinement
        boolean improved = true;
        int pass = 0;

        while (improved && pass < MAX_PASSES) {
            improved = false;
            pass++;
            stepList.add(new AlgorithmStep(map, cuts, "\n=== Pass " + pass + " ==="));

            for (int i = 0; i < numParts; i++) {
                for (int j = i + 1; j < numParts; j++) {
                    int[] newMap = refinePair(g, map, i, j, stepList, pass);
                    if (newMap != null) {
                        improved = true;
                        map = newMap;
                        cuts = countCuts(g, map);
                    }
                }
            }

            if (!improved) {
                stepList.add(new AlgorithmStep(map, cuts, "No improvement in pass " + pass));
            }
        }

        stepList.add(new AlgorithmStep(map, cuts, "\n=== Final: " + cuts + " cut edges ==="));
        return stepList;
    }

    // Refine a pair of partitions
    private int[] refinePair(Graph g, int[] map, int p1, int p2,
                             List<AlgorithmStep> stepList, int pass) {
        int[] working = map.clone();
        List<SwapInfo> swaps = new ArrayList<>();

        List<Integer> verts1 = new ArrayList<>();
        List<Integer> verts2 = new ArrayList<>();
        for (int v = 0; v < g.numVertices; v++) {
            if (map[v] == p1) verts1.add(v);
            if (map[v] == p2) verts2.add(v);
        }

        boolean[] locked = new boolean[g.numVertices];
        int iters = Math.min(verts1.size(), verts2.size());

        // Generate all swaps
        for (int iter = 0; iter < iters; iter++) {
            int[] gains = computeGains(g, working, p1, p2, locked);

            int bestV1 = -1, bestV2 = -1, maxGain = Integer.MIN_VALUE;

            for (int v1 : verts1) {
                if (locked[v1]) continue;
                for (int v2 : verts2) {
                    if (locked[v2]) continue;

                    int cost = hasEdge(g, v1, v2) ? 2 : 0;
                    int gain = gains[v1] + gains[v2] - cost;

                    if (gain > maxGain) {
                        maxGain = gain;
                        bestV1 = v1;
                        bestV2 = v2;
                    }
                }
            }

            if (bestV1 != -1) {
                working[bestV1] = p2;
                working[bestV2] = p1;
                locked[bestV1] = true;
                locked[bestV2] = true;

                int cuts = countCuts(g, working);
                swaps.add(new SwapInfo(working.clone(), bestV1, bestV2, maxGain, cuts, iter));
            } else {
                break;
            }
        }

        // Find best prefix (classic KL)
        int bestIdx = -1;
        int bestCuts = countCuts(g, map);

        for (int i = 0; i < swaps.size(); i++) {
            if (swaps.get(i).cuts < bestCuts) {
                bestCuts = swaps.get(i).cuts;
                bestIdx = i;
            }
        }

        // Record steps
        if (bestIdx >= 0) {
            for (int i = 0; i <= bestIdx; i++) {
                SwapInfo s = swaps.get(i);
                AlgorithmStep step = new AlgorithmStep(s.map, s.cuts,
                        String.format("Pass %d, pair(%d,%d), iter %d: swap %d<->%d gain=%d cuts=%d",
                                pass, p1, p2, s.iter, s.v1, s.v2, s.gain, s.cuts));
                step.highlight(s.v1, s.v2);
                stepList.add(step);
            }
            return swaps.get(bestIdx).map;
        }

        return null;
    }

    static class SwapInfo {
        int[] map;
        int v1, v2, gain, cuts, iter;

        SwapInfo(int[] m, int v1, int v2, int g, int c, int i) {
            this.map = m;
            this.v1 = v1;
            this.v2 = v2;
            this.gain = g;
            this.cuts = c;
            this.iter = i;
        }
    }

    // Compute gain for each vertex
    private int[] computeGains(Graph g, int[] map, int p1, int p2, boolean[] locked) {
        int[] gains = new int[g.numVertices];

        for (int v = 0; v < g.numVertices; v++) {
            if (locked[v] || (map[v] != p1 && map[v] != p2)) {
                continue;
            }

            int internal = 0, external = 0;
            for (int i = g.pointers[v]; i < g.pointers[v + 1]; i++) {
                int neighbor = g.adjacency[i];
                if (map[v] == map[neighbor]) {
                    internal++;
                } else {
                    external++;
                }
            }
            gains[v] = external - internal;
        }
        return gains;
    }

    // Helper methods
    private int[] randomPartition(int n, int parts) {
        int[] map = new int[n];
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < n; i++) indices.add(i);
        Collections.shuffle(indices);

        for (int i = 0; i < n; i++) {
            map[indices.get(i)] = i % parts;
        }
        return map;
    }

    private int countCuts(Graph g, int[] map) {
        int cuts = 0;
        for (int u = 0; u < g.numVertices; u++) {
            for (int i = g.pointers[u]; i < g.pointers[u + 1]; i++) {
                int v = g.adjacency[i];
                if (u < v && map[u] != map[v]) {
                    cuts++;
                }
            }
        }
        return cuts;
    }

    private boolean hasEdge(Graph g, int u, int v) {
        for (int i = g.pointers[u]; i < g.pointers[u + 1]; i++) {
            if (g.adjacency[i] == v) return true;
        }
        return false;
    }

    // UI control methods
    private void nextStep() {
        if (steps == null || currentStep >= steps.size() - 1) return;
        currentStep++;
        displayStep();
    }

    private void displayStep() {
        if (steps == null || currentStep >= steps.size()) return;

        AlgorithmStep step = steps.get(currentStep);
        afterPanel.updateView(step.partitionMap, step.vertex1, step.vertex2);
        cutEdgesLabel.setText("Cuts: " + step.cutEdges);
        stepLabel.setText("Step: " + (currentStep + 1) + "/" + steps.size());
        logArea.append(step.description + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());

        // Update partitions for saving
        if (currentStep == steps.size() - 1) {
            partitions = makePartitions(step.partitionMap);
        }
    }

    private void togglePlay() {
        if (isPlaying) {
            stopPlay();
        } else {
            isPlaying = true;
            playButton.setText("Pause");
            stepButton.setEnabled(false);
            animationTimer.setDelay(speedSlider.getValue());
            animationTimer.start();
        }
    }

    private void stopPlay() {
        isPlaying = false;
        playButton.setText("Play");
        stepButton.setEnabled(true);
        animationTimer.stop();
    }

    private void resetView() {
        if (steps == null) return;
        stopPlay();
        currentStep = 0;
        displayStep();
        logArea.append("\n--- Reset ---\n");
    }

    private Partition[] makePartitions(int[] map) {
        int numParts = 0;
        for (int p : map) {
            if (p + 1 > numParts) numParts = p + 1;
        }

        List<List<Integer>> temp = new ArrayList<>();
        for (int i = 0; i < numParts; i++) {
            temp.add(new ArrayList<>());
        }

        for (int v = 0; v < map.length; v++) {
            temp.get(map[v]).add(v);
        }

        Partition[] result = new Partition[numParts];
        for (int i = 0; i < numParts; i++) {
            result[i] = new Partition();
            List<Integer> list = temp.get(i);
            result[i].vertices = new int[list.size()];
            for (int j = 0; j < list.size(); j++) {
                result[i].vertices[j] = list.get(j);
            }
        }
        return result;
    }

    private void saveResult() {
        if (partitions == null) return;

        JFileChooser fc = new JFileChooser(".");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()));
                pw.println(partitions.length);
                pw.println(steps.get(steps.size() - 1).cutEdges);

                for (Partition p : partitions) {
                    pw.print(p.vertices.length);
                    for (int v : p.vertices) {
                        pw.print(" " + v);
                    }
                    pw.println();
                }
                pw.close();
                JOptionPane.showMessageDialog(this, "Saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GraphPartitionGUI().setVisible(true);
        });
    }
}
