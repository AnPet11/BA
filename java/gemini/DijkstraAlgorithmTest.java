import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.NoSuchElementException;

class DijkstraAlgorithmTest {

    @Test
    void testStandardPath() {
        int[][] graph = {
            {0, 10, 0, 0, 5},
            {0, 0, 1, 0, 2},
            {0, 0, 0, 4, 0},
            {7, 0, 6, 0, 0},
            {0, 3, 9, 2, 0}
        };
        
        DijkstraAlgorithm.Result result = DijkstraAlgorithm.findShortestPath(graph, 0, 3);
        
        assertEquals(7, result.distance); // 0 -> 4 -> 3 (5 + 2)
        assertEquals(List.of(0, 4, 3), result.path);
    }

    @Test
    void testNoPathExists() {
        int[][] graph = {
            {0, 5, 0},
            {0, 0, 0},
            {1, 0, 0}
        };
        // Sõlmest 0 ei saa sõlme 2
        assertThrows(NoSuchElementException.class, () -> {
            DijkstraAlgorithm.findShortestPath(graph, 0, 2);
        });
    }

    @Test
    void testInvalidInput() {
        int[][] graph = {{0, 1}, {1, 0}};
        assertThrows(IndexOutOfBoundsException.class, () -> {
            DijkstraAlgorithm.findShortestPath(graph, 0, 5);
        });
    }

    @Test
    void testSingleNodeGraph() {
        int[][] graph = {{0}};
        DijkstraAlgorithm.Result result = DijkstraAlgorithm.findShortestPath(graph, 0, 0);
        assertEquals(0, result.distance);
        assertEquals(List.of(0), result.path);
    }
}
