import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class GraphPartitionGUI extends JFrame {
    private Graph graph;
    private Part[] parts;
    private int numParts;
    private float margin;
    private int cutEdges;
    private GraphPanel beforePanel;
    private GraphPanel afterPanel;

    // Struktury danych odpowiadające Graf i CzescGrafu
    static class Graph {
        int numVertices;
        int numEdges;
        int[] vertexIndices; // Lista sąsiadów
        int[] rowPointers;  // Wskaźniki wierszy
        int[] vertexGroups; // Grupy wierzchołków
        int[] groupPointers; // Wskaźniki grup
    }

    static class Part {
        int[] vertices;
        int numVertices;
    }

    // Panel do rysowania grafu
    class GraphPanel extends JPanel {
        private Graph graph;
        private Part[] parts;
        private boolean isAfterPartition;

        public GraphPanel(Graph graph, Part[] parts, boolean isAfterPartition) {
            this.graph = graph;
            this.parts = parts;
            this.isAfterPartition = isAfterPartition;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (graph == null) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int radius = Math.min(width, height) / 3;
            int centerX = width / 2;
            int centerY = height / 2;

            // Mapowanie wierzchołków na części (dla kolorów po podziale)
            int[] vertexToPart = new int[graph.numVertices];
            Arrays.fill(vertexToPart, -1);
            if (isAfterPartition && parts != null) {
                for (int i = 0; i < parts.length; i++) {
                    for (int j = 0; j < parts[i].numVertices; j++) {
                        vertexToPart[parts[i].vertices[j]] = i;
                    }
                }
            }

            // Rysowanie krawędzi
            g2d.setColor(Color.BLACK);
            for (int v = 0; v < graph.numVertices; v++) {
                double angleV = 2 * Math.PI * v / graph.numVertices;
                int xV = (int) (centerX + radius * Math.cos(angleV));
                int yV = (int) (centerY + radius * Math.sin(angleV));
                for (int i = graph.rowPointers[v]; i < graph.rowPointers[v + 1]; i++) {
                    int neighbor = graph.vertexIndices[i];
                    if (v < neighbor) { // Unikamy podwójnego rysowania
                        double angleN = 2 * Math.PI * neighbor / graph.numVertices;
                        int xN = (int) (centerX + radius * Math.cos(angleN));
                        int yN = (int) (centerY + radius * Math.sin(angleN));
                        g2d.drawLine(xV, yV, xN, yN);
                    }
                }
            }

            // Rysowanie wierzchołków
            Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.CYAN};
            for (int v = 0; v < graph.numVertices; v++) {
                double angle = 2 * Math.PI * v / graph.numVertices;
                int x = (int) (centerX + radius * Math.cos(angle)) - 10;
                int y = (int) (centerY + radius * Math.sin(angle)) - 10;
                if (isAfterPartition && vertexToPart[v] != -1) {
                    g2d.setColor(colors[vertexToPart[v] % colors.length]);
                } else {
                    g2d.setColor(Color.BLACK);
                }
                g2d.fillOval(x, y, 20, 20);
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(v), x + 5, y + 15);
            }
        }
    }

    public GraphPartitionGUI() {
        setTitle("Graph Partitioning");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLayout(new BorderLayout());

        // Panel sterowania
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        JButton loadButton = new JButton("Load Graph");
        JTextField partsField = new JTextField("2", 5);
        JTextField marginField = new JTextField("10.0", 5);
        JButton partitionButton = new JButton("Partition Graph");
        JButton saveButton = new JButton("Save Result");

        controlPanel.add(new JLabel("Parts:"));
        controlPanel.add(partsField);
        controlPanel.add(new JLabel("Margin (%):"));
        controlPanel.add(marginField);
        controlPanel.add(loadButton);
        controlPanel.add(partitionButton);
        controlPanel.add(saveButton);

        // Panele wizualizacji
        beforePanel = new GraphPanel(null, null, false);
        afterPanel = new GraphPanel(null, null, true);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, beforePanel, afterPanel);
        splitPane.setDividerLocation(500);

        add(controlPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Akcje przycisków
        loadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                graph = loadGraph(fileChooser.getSelectedFile().getPath());
                if (graph != null) {
                    beforePanel.graph = graph;
                    afterPanel.graph = graph;
                    beforePanel.repaint();
                    afterPanel.repaint();
                    JOptionPane.showMessageDialog(this, "Graph loaded successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error loading graph.");
                }
            }
        });

        partitionButton.addActionListener(e -> {
            try {
                numParts = Integer.parseInt(partsField.getText());
                margin = Float.parseFloat(marginField.getText());
                if (numParts < 1 || margin < 0) {
                    JOptionPane.showMessageDialog(this, "Invalid number of parts or margin.");
                    return;
                }
                if (graph == null) {
                    JOptionPane.showMessageDialog(this, "No graph loaded.");
                    return;
                }
                if (numParts > graph.numVertices) {
                    JOptionPane.showMessageDialog(this, "Number of parts exceeds number of vertices.");
                    return;
                }
                parts = partitionGraph(graph, numParts, margin);
                if (parts != null) {
                    cutEdges = calculateCutEdges(graph, parts, numParts);
                    afterPanel.parts = parts;
                    afterPanel.repaint();
                    JOptionPane.showMessageDialog(this, "Graph partitioned. Cut edges: " + cutEdges);
                } else {
                    JOptionPane.showMessageDialog(this, "Partitioning failed.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input format.");
            }
        });

        saveButton.addActionListener(e -> {
            if (parts == null) {
                JOptionPane.showMessageDialog(this, "No partition to save.");
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                saveResult(fileChooser.getSelectedFile().getPath(), parts, numParts, cutEdges);
                JOptionPane.showMessageDialog(this, "Result saved successfully!");
            }
        });
    }

    // Wczytywanie grafu z pliku
    private Graph loadGraph(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            Graph graph = new Graph();

            // Wczytanie liczby wierzchołków
            graph.numVertices = Integer.parseInt(reader.readLine().trim());

            // Wczytanie indeksów wierzchołków
            String[] tokens = reader.readLine().trim().split(";");
            graph.vertexIndices = new int[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                graph.vertexIndices[i] = Integer.parseInt(tokens[i].trim());
            }
            graph.numEdges = graph.vertexIndices.length;

            // Wczytanie wskaźników wierszy
            tokens = reader.readLine().trim().split(";");
            graph.rowPointers = new int[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                graph.rowPointers[i] = Integer.parseInt(tokens[i].trim());
            }
            if (graph.rowPointers.length != graph.numVertices + 1) {
                return null;
            }

            // Wczytanie grup wierzchołków
            tokens = reader.readLine().trim().split(";");
            graph.vertexGroups = new int[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                graph.vertexGroups[i] = Integer.parseInt(tokens[i].trim());
            }

            // Wczytanie wskaźników grup
            tokens = reader.readLine().trim().split(";");
            graph.groupPointers = new int[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                graph.groupPointers[i] = Integer.parseInt(tokens[i].trim());
            }

            return graph;
        } catch (IOException | NumberFormatException e) {
            return null;
        }
    }

    // Obliczanie przeciętych krawędzi
    private int calculateCutEdges(Graph graph, Part[] parts, int numParts) {
        int cutEdges = 0;
        int[] vertexToPart = new int[graph.numVertices];
        Arrays.fill(vertexToPart, -1);
        for (int i = 0; i < numParts; i++) {
            for (int j = 0; j < parts[i].numVertices; j++) {
                vertexToPart[parts[i].vertices[j]] = i;
            }
        }
        for (int v = 0; v < graph.numVertices; v++) {
            for (int i = graph.rowPointers[v]; i < graph.rowPointers[v + 1]; i++) {
                int neighbor = graph.vertexIndices[i];
                if (vertexToPart[v] != vertexToPart[neighbor] && v < neighbor) {
                    cutEdges++;
                }
            }
        }
        return cutEdges;
    }

    // Algorytm Kernighana-Lina
    private void kernighanLin(Graph graph, Part[] parts, int[] vertexToPart, int part1, int part2) {
        int n = graph.numVertices;
        int[] gains = new int[n];
        boolean[] locked = new boolean[n];
        int[] tempVertexToPart = vertexToPart.clone();
        int iterations = n / 10;

        for (int iter = 0; iter < iterations; iter++) {
            // Obliczanie zysków
            for (int v = 0; v < n; v++) {
                if (locked[v]) continue;
                int externalCost = 0, internalCost = 0;
                for (int i = graph.rowPointers[v]; i < graph.rowPointers[v + 1]; i++) {
                    int neighbor = graph.vertexIndices[i];
                    if (tempVertexToPart[v] == tempVertexToPart[neighbor]) {
                        internalCost++;
                    } else {
                        externalCost++;
                    }
                }
                gains[v] = externalCost - internalCost;
            }

            // Znajdowanie najlepszej wymiany
            int bestV1 = -1, bestV2 = -1, bestGain = -1;
            for (int v1 = 0; v1 < n; v1++) {
                if (locked[v1] || tempVertexToPart[v1] != part1) continue;
                for (int v2 = 0; v2 < n; v2++) {
                    if (locked[v2] || tempVertexToPart[v2] != part2) continue;
                    int gain = gains[v1] + gains[v2];
                    for (int i = graph.rowPointers[v1]; i < graph.rowPointers[v1 + 1]; i++) {
                        if (graph.vertexIndices[i] == v2) {
                            gain -= 2;
                            break;
                        }
                    }
                    if (gain > bestGain) {
                        bestGain = gain;
                        bestV1 = v1;
                        bestV2 = v2;
                    }
                }
            }

            if (bestGain <= 0 || bestV1 == -1 || bestV2 == -1) break;

            // Wykonanie wymiany
            tempVertexToPart[bestV1] = part2;
            tempVertexToPart[bestV2] = part1;
            locked[bestV1] = true;
            locked[bestV2] = true;
        }

        // Aktualizacja części
        ArrayList<Integer> newPart1 = new ArrayList<>();
        ArrayList<Integer> newPart2 = new ArrayList<>();
        for (int i = 0; i < parts[part1].numVertices; i++) {
            int v = parts[part1].vertices[i];
            if (tempVertexToPart[v] == part1) {
                newPart1.add(v);
            } else {
                newPart2.add(v);
            }
        }
        for (int i = 0; i < parts[part2].numVertices; i++) {
            int v = parts[part2].vertices[i];
            if (tempVertexToPart[v] == part2) {
                newPart2.add(v);
            } else {
                newPart1.add(v);
            }
        }

        parts[part1].vertices = newPart1.stream().mapToInt(i -> i).toArray();
        parts[part1].numVertices = newPart1.size();
        parts[part2].vertices = newPart2.stream().mapToInt(i -> i).toArray();
        parts[part2].numVertices = newPart2.size();
    }

    // Podział grafu
    private Part[] partitionGraph(Graph graph, int numParts, float margin) {
        Part[] parts = new Part[numParts];
        for (int i = 0; i < numParts; i++) {
            parts[i] = new Part();
            parts[i].vertices = new int[graph.numVertices];
            parts[i].numVertices = 0;
        }

        // Losowy początkowy podział
        int[] vertexToPart = new int[graph.numVertices];
        int verticesPerPart = graph.numVertices / numParts;
        int remainder = graph.numVertices % numParts;
        int k = 0;
        for (int i = 0; i < numParts; i++) {
            int size = verticesPerPart + (i < remainder ? 1 : 0);
            for (int j = 0; j < size; j++) {
                parts[i].vertices[j] = k;
                vertexToPart[k] = i;
                parts[i].numVertices++;
                k++;
            }
        }

        // Iteracyjny podział z Kernighan-Lin
        for (int iter = 0; iter < numParts - 1; iter++) {
            for (int i = 0; i < numParts; i++) {
                for (int j = i + 1; j < numParts; j++) {
                    kernighanLin(graph, parts, vertexToPart, i, j);
                    // Aktualizacja vertexToPart po każdej wymianie
                    Arrays.fill(vertexToPart, -1);
                    for (int p = 0; p < numParts; p++) {
                        for (int v = 0; v < parts[p].numVertices; v++) {
                            vertexToPart[parts[p].vertices[v]] = p;
                        }
                    }
                }
            }
        }

        // Weryfikacja marginesu
        int minVertices = graph.numVertices / numParts;
        int maxVertices = (int) (minVertices * (1.0 + margin / 100.0));
        for (int i = 0; i < numParts; i++) {
            if (parts[i].numVertices < minVertices || parts[i].numVertices > maxVertices) {
                return null;
            }
        }

        return parts;
    }

    // Zapis wyniku
    private void saveResult(String filename, Part[] parts, int numParts, int cutEdges) {
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.println(numParts);
            writer.println(cutEdges);
            for (int i = 0; i < numParts; i++) {
                writer.print(parts[i].numVertices);
                for (int j = 0; j < parts[i].numVertices; j++) {
                    writer.print(" " + parts[i].vertices[j]);
                }
                writer.println();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving result.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GraphPartitionGUI().setVisible(true);
        });
    }
}