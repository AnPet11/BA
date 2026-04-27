import java.util.*;

public class DijkstraAlgorithm {

    public static class Result {
        public final List<Integer> path;
        public final int distance;

        public Result(List<Integer> path, int distance) {
            this.path = path;
            this.distance = distance;
        }
    }

    /**
     * Leiab lühima tee suunatud graafis.
     * @param matrix Naabrusmaatriks (0 tähendab ühenduse puudumist, positiivne arv on kaal).
     * @param start Algussõlm.
     * @param end Sihtsõlm.
     * @return Result objekt tee ja pikkusega.
     */
    public static Result findShortestPath(int[][] matrix, int start, int end) {
        validateInput(matrix, start, end);

        int n = matrix.length;
        int[] distances = new int[n];
        int[] previous = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(distances, Integer.MAX_VALUE);
        Arrays.fill(previous, -1);
        distances[start] = 0;

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));
        pq.add(new Node(start, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            int u = current.id;

            if (visited[u]) continue;
            visited[u] = true;

            if (u == end) break;

            for (int v = 0; v < n; v++) {
                if (matrix[u][v] > 0 && !visited[v]) {
                    int newDist = distances[u] + matrix[u][v];
                    if (newDist < distances[v]) {
                        distances[v] = newDist;
                        previous[v] = u;
                        pq.add(new Node(v, newDist));
                    }
                }
            }
        }

        if (distances[end] == Integer.MAX_VALUE) {
            throw new NoSuchElementException("Teed sõlmest " + start + " sõlme " + end + " ei eksisteeri.");
        }

        return new Result(reconstructPath(previous, end), distances[end]);
    }

    private static void validateInput(int[][] matrix, int start, int end) {
        if (matrix == null || matrix.length == 0 || matrix.length != matrix[0].length) {
            throw new IllegalArgumentException("Vigane naabrusmaatriks.");
        }
        if (start < 0 || start >= matrix.length || end < 0 || end >= matrix.length) {
            throw new IndexOutOfBoundsException("Algus- või lõpp-punkt on väljaspool graafi piire.");
        }
    }

    private static List<Integer> reconstructPath(int[] previous, int end) {
        LinkedList<Integer> path = new LinkedList<>();
        for (int at = end; at != -1; at = previous[at]) {
            path.addFirst(at);
        }
        return path;
    }

    private static class Node {
        int id, distance;
        Node(int id, int distance) {
            this.id = id;
            this.distance = distance;
        }
    }
}
