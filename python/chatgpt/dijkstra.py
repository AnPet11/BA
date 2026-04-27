from typing import List, Tuple, Optional
import heapq
import unittest

class GraphError(Exception):
    pass


def validate_adjacency_matrix(matrix: List[List[float]]):
    if not isinstance(matrix, list) or not matrix:
        raise GraphError("Adjacency matrix must be a non-empty list.")

    n = len(matrix)
    for row in matrix:
        if not isinstance(row, list) or len(row) != n:
            raise GraphError("Adjacency matrix must be square.")
        for val in row:
            if val is None:
                continue
            if not isinstance(val, (int, float)):
                raise GraphError("Matrix values must be numbers or None.")
            if val < 0:
                raise GraphError("Dijkstra cannot handle negative weights.")


def dijkstra(matrix: List[List[float]], start: int, end: int) -> Tuple[List[int], float]:
    validate_adjacency_matrix(matrix)

    n = len(matrix)

    if not (0 <= start < n and 0 <= end < n):
        raise GraphError("Start or end node out of bounds.")

    distances = [float('inf')] * n
    previous = [None] * n
    distances[start] = 0

    pq = [(0, start)]

    while pq:
        current_dist, u = heapq.heappop(pq)

        if current_dist > distances[u]:
            continue

        if u == end:
            break

        for v, weight in enumerate(matrix[u]):
            if weight is None or u == v:
                continue

            alt = current_dist + weight
            if alt < distances[v]:
                distances[v] = alt
                previous[v] = u
                heapq.heappush(pq, (alt, v))

    if distances[end] == float('inf'):
        raise GraphError("No path exists between start and end nodes.")

    path = []
    current = end
    while current is not None:
        path.append(current)
        current = previous[current]
    path.reverse()

    return path, distances[end]


# ================= UNIT TESTS =================

class TestDijkstra(unittest.TestCase):

    def test_small_graph(self):
        matrix = [
            [0, 1, 4, None],
            [None, 0, 2, 6],
            [None, None, 0, 3],
            [None, None, None, 0]
        ]
        path, dist = dijkstra(matrix, 0, 3)
        self.assertEqual(path, [0, 1, 2, 3])
        self.assertEqual(dist, 6)

    def test_direct_path(self):
        matrix = [
            [0, 5],
            [None, 0]
        ]
        path, dist = dijkstra(matrix, 0, 1)
        self.assertEqual(path, [0, 1])
        self.assertEqual(dist, 5)

    def test_no_path(self):
        matrix = [
            [0, None],
            [None, 0]
        ]
        with self.assertRaises(GraphError):
            dijkstra(matrix, 0, 1)

    def test_invalid_matrix(self):
        with self.assertRaises(GraphError):
            dijkstra([[1, 2], [3]], 0, 1)

    def test_negative_weights(self):
        matrix = [
            [0, -1],
            [None, 0]
        ]
        with self.assertRaises(GraphError):
            dijkstra(matrix, 0, 1)

    def test_medium_graph(self):
        matrix = [
            [0, 2, None, 1, None],
            [None, 0, 3, None, None],
            [None, None, 0, None, 1],
            [None, None, 2, 0, 4],
            [None, None, None, None, 0]
        ]
        path, dist = dijkstra(matrix, 0, 4)
        self.assertEqual(path, [0, 3, 2, 4])
        self.assertEqual(dist, 4)


if __name__ == "__main__":
    unittest.main()
