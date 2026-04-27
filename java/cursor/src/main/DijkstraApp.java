package ee.tlu.ba.dijkstra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class DijkstraApp {
    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            GraphInput input = GraphInputParser.parse(br);
            DijkstraResult result = DijkstraShortestPath.findShortestPath(input.adjacencyMatrix(), input.start(), input.target());

            if (!result.reachable()) {
                System.out.println("NO PATH");
                System.out.printf("No path exists from %d to %d.%n", input.start(), input.target());
                return;
            }

            List<Integer> path = result.path();
            System.out.println("PATH");
            System.out.println(pathToString(path));
            System.out.println("TOTAL_LENGTH");
            System.out.println(result.totalLength());
        } catch (IllegalArgumentException e) {
            System.err.println("INPUT ERROR: " + e.getMessage());
            System.exit(2);
        } catch (IOException e) {
            System.err.println("IO ERROR: " + e.getMessage());
            System.exit(3);
        }
    }

    private static String pathToString(List<Integer> path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(path.get(i));
        }
        return sb.toString();
    }
}

