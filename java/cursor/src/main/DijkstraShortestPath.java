package ee.tlu.ba.dijkstra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DijkstraShortestPath {
    private DijkstraShortestPath() {}

    /**
     * Finds shortest path in a directed graph represented as adjacency matrix.
     * Convention: weight == -1 means "no edge". All other weights must be >= 0.
     */
    public static DijkstraResult findShortestPath(long[][] matrix, int start, int target) {
        validateMatrix(matrix);
        int n = matrix.length;
        if (n == 0) {
            throw new IllegalArgumentException("Matrix must have at least 1 vertex.");
        }
        if (start < 0 || start >= n) {
            throw new IllegalArgumentException("Start vertex is out of range.");
        }
        if (target < 0 || target >= n) {
            throw new IllegalArgumentException("Target vertex is out of range.");
        }

        long[] dist = new long[n];
        int[] prev = new int[n];
        boolean[] visited = new boolean[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[start] = 0;

        for (int iter = 0; iter < n; iter++) {
            int u = pickMinUnvisited(dist, visited);
            if (u == -1) break;            // remaining vertices unreachable
            if (u == target) break;        // target finalized

            visited[u] = true;
            long du = dist[u];
            if (du == Long.MAX_VALUE) continue;

            for (int v = 0; v < n; v++) {
                long w = matrix[u][v];
                if (w == -1) continue;
                // w is validated non-negative
                if (visited[v]) continue;
                long alt = safeAdd(du, w);
                if (alt < dist[v]) {
                    dist[v] = alt;
                    prev[v] = u;
                }
            }
        }

        if (dist[target] == Long.MAX_VALUE) {
            return new DijkstraResult(false, List.of(), -1);
        }

        List<Integer> path = reconstructPath(prev, start, target);
        return new DijkstraResult(true, path, dist[target]);
    }

    private static long safeAdd(long a, long b) {
        if (a > Long.MAX_VALUE - b) return Long.MAX_VALUE;
        return a + b;
    }

    private static int pickMinUnvisited(long[] dist, boolean[] visited) {
        long best = Long.MAX_VALUE;
        int bestIdx = -1;
        for (int i = 0; i < dist.length; i++) {
            if (visited[i]) continue;
            long d = dist[i];
            if (d < best) {
                best = d;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private static List<Integer> reconstructPath(int[] prev, int start, int target) {
        List<Integer> path = new ArrayList<>();
        int cur = target;
        while (cur != -1) {
            path.add(cur);
            if (cur == start) break;
            cur = prev[cur];
        }
        if (path.get(path.size() - 1) != start) {
            // should not happen if dist[target] is finite, but keep it safe
            return List.of();
        }
        Collections.reverse(path);
        return path;
    }

    private static void validateMatrix(long[][] matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix is null.");
        }
        int n = matrix.length;
        if (n == 0) {
            throw new IllegalArgumentException("Matrix has zero size.");
        }
        for (int i = 0; i < n; i++) {
            if (matrix[i] == null) {
                throw new IllegalArgumentException("Matrix row " + i + " is null.");
            }
            if (matrix[i].length != n) {
                throw new IllegalArgumentException("Matrix must be square. Row " + i + " has length " + matrix[i].length + ", expected " + n + ".");
            }
            for (int j = 0; j < n; j++) {
                long w = matrix[i][j];
                if (w < -1) {
                    throw new IllegalArgumentException("Negative weight detected at [" + i + "][" + j + "]. Only -1 is allowed as 'no edge'.");
                }
            }
        }
    }
}

