import java.util.*;

// Main class containing Dijkstra implementation
public class DijkstraGraph {

    public static class Result {
        public final List<Integer> path;
        public final int distance;

        public Result(List<Integer> path, int distance) {
            this.path = path;
            this.distance = distance;
        }
    }

    public static Result dijkstra(int[][] graph, int start, int end) {
        validateInput(graph, start, end);

        int n = graph.length;
        int[] dist = new int[n];
        int[] prev = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);

        dist[start] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.add(new int[]{start, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int u = current[0];

            if (visited[u]) continue;
            visited[u] = true;

            for (int v = 0; v < n; v++) {
                if (graph[u][v] > 0 && !visited[v]) {
                    int newDist = dist[u] + graph[u][v];
                    if (newDist < dist[v]) {
                        dist[v] = newDist;
                        prev[v] = u;
                        pq.add(new int[]{v, dist[v]});
                    }
                }
            }
        }

        if (dist[end] == Integer.MAX_VALUE) {
            throw new NoSuchElementException("No path exists from start to end node.");
        }

        return new Result(reconstructPath(prev, start, end), dist[end]);
    }

    private static List<Integer> reconstructPath(int[] prev, int start, int end) {
        List<Integer> path = new ArrayList<>();
        for (int at = end; at != -1; at = prev[at]) {
            path.add(at);
        }
        Collections.reverse(path);

        if (path.get(0) != start) {
            throw new NoSuchElementException("Path reconstruction failed.");
        }

        return path;
    }

    private static void validateInput(int[][] graph, int start, int end) {
        if (graph == null || graph.length == 0) {
            throw new IllegalArgumentException("Graph must not be null or empty.");
        }

        int n = graph.length;

        for (int[] row : graph) {
            if (row == null || row.length != n) {
                throw new IllegalArgumentException("Graph must be a square adjacency matrix.");
            }
            for (int weight : row) {
                if (weight < 0) {
                    throw new IllegalArgumentException("Graph must not contain negative weights.");
                }
            }
        }

        if (start < 0 || start >= n || end < 0 || end >= n) {
            throw new IndexOutOfBoundsException("Start or end node is out of bounds.");
        }
    }

    // Demo main
    public static void main(String[] args) {
        int[][] graph = {
            {0, 4, 0, 0, 0, 0},
            {0, 0, 8, 0, 0, 0},
            {0, 0, 0, 7, 0, 4},
            {0, 0, 0, 0, 9, 0},
            {0, 0, 0, 0, 0, 10},
            {0, 0, 0, 0, 0, 0}
        };

        try {
            Result result = dijkstra(graph, 0, 5);
            System.out.println("Path: " + result.path);
            System.out.println("Distance: " + result.distance);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

// Unit Tests using JUnit 5
// Make sure to include JUnit 5 dependency

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DijkstraGraphTest {

    @Test
    void testSimpleGraph() {
        int[][] graph = {
            {0, 1, 4},
            {0, 0, 2},
            {0, 0, 0}
        };

        DijkstraGraph.Result result = DijkstraGraph.dijkstra(graph, 0, 2);
        assertEquals(3, result.distance);
        assertEquals(Arrays.asList(0,1,2), result.path);
    }

    @Test
    void testNoPath() {
        int[][] graph = {
            {0, 1, 0},
            {0, 0, 0},
            {0, 0, 0}
        };

        assertThrows(NoSuchElementException.class, () -> {
            DijkstraGraph.dijkstra(graph, 0, 2);
        });
    }

    @Test
    void testSingleNode() {
        int[][] graph = {{0}};

        DijkstraGraph.Result result = DijkstraGraph.dijkstra(graph, 0, 0);
        assertEquals(0, result.distance);
        assertEquals(Collections.singletonList(0), result.path);
    }

    @Test
    void testInvalidGraph() {
        int[][] graph = {
            {0, -1},
            {0, 0}
        };

        assertThrows(IllegalArgumentException.class, () -> {
            DijkstraGraph.dijkstra(graph, 0, 1);
        });
    }

    @Test
    void testLargerGraph() {
        int[][] graph = new int[10][10];
        for (int i = 0; i < 9; i++) {
            graph[i][i+1] = 1;
        }

        DijkstraGraph.Result result = DijkstraGraph.dijkstra(graph, 0, 9);
        assertEquals(9, result.distance);
    }
}