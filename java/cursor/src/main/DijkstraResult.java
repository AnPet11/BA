package ee.tlu.ba.dijkstra;

import java.util.List;

public record DijkstraResult(boolean reachable, List<Integer> path, long totalLength) {}

