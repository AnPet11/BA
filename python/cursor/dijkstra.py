from __future__ import annotations


import argparse
import json
import math
import sys
from dataclasses import dataclass
from heapq import heappop, heappush
from typing import Iterable, List, Optional, Sequence, Tuple




Number = float
Matrix = Sequence[Sequence[object]]




class GraphInputError(ValueError):
    """Raised when adjacency matrix or parameters are invalid."""




@dataclass(frozen=True)
class ShortestPathResult:
    path: List[int]
    distance: Number




def _is_finite_non_negative_number(x: object) -> bool:
    if isinstance(x, bool):
        return False
    if isinstance(x, (int, float)):
        return math.isfinite(float(x)) and float(x) >= 0.0
    return False




def _cell_to_weight(cell: object, *, i: int, j: int) -> Optional[Number]:
    """
    Convert adjacency-matrix cell to edge weight.


    Supported encodings:
    - None / "inf" / "INF" / ""  -> no edge
    - 0 on diagonal (i==j)       -> ok (self distance)
    - positive finite number     -> edge weight
    - 0 off-diagonal             -> treated as no edge (common adjacency-matrix convention)
    """
    if cell is None:
        return None


    if isinstance(cell, str):
        s = cell.strip().lower()
        if s in {"", "inf", "infinity", "none", "null", "nan"}:
            return None
        raise GraphInputError(
            f"Invalid string at matrix[{i}][{j}]={cell!r}. "
            "Use a non-negative number or one of: null/None/inf/'' to indicate no edge."
        )


    if not _is_finite_non_negative_number(cell):
        raise GraphInputError(
            f"Invalid weight at matrix[{i}][{j}]={cell!r}. "
            "Weights must be finite non-negative numbers, or null/None/inf to indicate no edge."
        )


    w = float(cell)
    if i != j and w == 0.0:
        return None
    return w




def _validate_and_normalize_matrix(matrix: Matrix) -> List[List[Optional[Number]]]:
    if not isinstance(matrix, Sequence) or isinstance(matrix, (str, bytes)):
        raise GraphInputError("Adjacency matrix must be a sequence of rows (e.g., list of lists).")
    if len(matrix) == 0:
        raise GraphInputError("Adjacency matrix must be non-empty.")


    n = len(matrix)
    normalized: List[List[Optional[Number]]] = []


    for i, row in enumerate(matrix):
        if not isinstance(row, Sequence) or isinstance(row, (str, bytes)):
            raise GraphInputError(f"Row {i} must be a sequence (e.g., list).")
        if len(row) != n:
            raise GraphInputError(
                f"Adjacency matrix must be square. Row {i} has length {len(row)} but expected {n}."
            )
        out_row: List[Optional[Number]] = []
        for j, cell in enumerate(row):
            w = _cell_to_weight(cell, i=i, j=j)
            if i == j and w is None:
                # Allow None on diagonal, interpret as 0.
                w = 0.0
            out_row.append(w)
        normalized.append(out_row)


    return normalized




def _validate_vertex(v: int, *, n: int, name: str) -> None:
    if isinstance(v, bool) or not isinstance(v, int):
        raise GraphInputError(f"{name} must be an integer in range [0, {n - 1}]. Got: {v!r}")
    if not (0 <= v < n):
        raise GraphInputError(f"{name} must be in range [0, {n - 1}]. Got: {v}")




def dijkstra_shortest_path(
    matrix: Matrix,
    start: int,
    target: int,
) -> ShortestPathResult:
    """
    Find shortest path in a directed graph (adjacency matrix) using Dijkstra.


    Args:
        matrix: NxN adjacency matrix. Cells encode directed edge i->j weight.
                - non-negative number: edge exists with given weight
                - None / "inf" / ""  : no edge
                - 0 off-diagonal     : treated as no edge
        start: start vertex index [0..N-1]
        target: target vertex index [0..N-1]


    Returns:
        ShortestPathResult(path=[start..target], distance=total_weight)


    Raises:
        GraphInputError: invalid matrix or parameters
        ValueError: if no path exists from start to target
    """
    g = _validate_and_normalize_matrix(matrix)
    n = len(g)
    _validate_vertex(start, n=n, name="start")
    _validate_vertex(target, n=n, name="target")


    if start == target:
        return ShortestPathResult(path=[start], distance=0.0)


    dist = [math.inf] * n
    prev: List[Optional[int]] = [None] * n
    dist[start] = 0.0


    pq: List[Tuple[Number, int]] = [(0.0, start)]
    visited = [False] * n


    while pq:
        d_u, u = heappop(pq)
        if visited[u]:
            continue
        visited[u] = True


        if u == target:
            break


        # Relax outgoing edges u -> v
        row = g[u]
        for v, w in enumerate(row):
            if w is None:
                continue
            if visited[v]:
                continue
            nd = d_u + w
            if nd < dist[v]:
                dist[v] = nd
                prev[v] = u
                heappush(pq, (nd, v))


    if not math.isfinite(dist[target]):
        raise ValueError(f"No path exists from {start} to {target}.")


    # Reconstruct path
    path: List[int] = []
    cur: Optional[int] = target
    while cur is not None:
        path.append(cur)
        cur = prev[cur]
    path.reverse()


    if not path or path[0] != start:
        # Defensive: should not happen if dist[target] is finite.
        raise ValueError(f"No path exists from {start} to {target}.")


    return ShortestPathResult(path=path, distance=float(dist[target]))




def _parse_matrix_json(text: str) -> object:
    try:
        return json.loads(text)
    except json.JSONDecodeError as e:
        raise GraphInputError(
            "Failed to parse matrix JSON. Expected JSON like: [[0, 7, null], [null, 0, 2], [null, null, 0]]."
        ) from e




def _build_arg_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        description="Dijkstra shortest path for a directed graph given as adjacency matrix (JSON)."
    )
    p.add_argument(
        "--matrix",
        required=True,
        help="Adjacency matrix as JSON string. Use null for no edge. Example: '[[0,7,null],[null,0,2],[null,null,0]]'",
    )
    p.add_argument("--start", type=int, required=True, help="Start vertex index (0-based).")
    p.add_argument("--target", type=int, required=True, help="Target vertex index (0-based).")
    return p




def main(argv: Optional[Sequence[str]] = None) -> int:
    parser = _build_arg_parser()
    args = parser.parse_args(argv)


    try:
        raw = _parse_matrix_json(args.matrix)
        res = dijkstra_shortest_path(raw, args.start, args.target)
    except GraphInputError as e:
        print(f"INPUT ERROR: {e}", file=sys.stderr)
        return 2
    except ValueError as e:
        print(f"NO PATH: {e}", file=sys.stderr)
        return 3


    print("Path:", " -> ".join(map(str, res.path)))
    print("Total length:", res.distance)
    return 0




if __name__ == "__main__":
    raise SystemExit(main())
import math
import unittest


from dijkstra_matrix import GraphInputError, dijkstra_shortest_path




class TestDijkstraMatrix(unittest.TestCase):
    def test_start_equals_target(self) -> None:
        m = [
            [0, 1],
            [None, 0],
        ]
        res = dijkstra_shortest_path(m, 0, 0)
        self.assertEqual(res.path, [0])
        self.assertEqual(res.distance, 0.0)


    def test_simple_directed_graph(self) -> None:
        # 0->1 (1), 1->2 (2), 0->2 (10) => best 0-1-2 = 3
        m = [
            [0, 1, 10],
            [None, 0, 2],
            [None, None, 0],
        ]
        res = dijkstra_shortest_path(m, 0, 2)
        self.assertEqual(res.path, [0, 1, 2])
        self.assertEqual(res.distance, 3.0)


    def test_zero_off_diagonal_is_no_edge(self) -> None:
        # 0->1 is encoded as 0 (no edge), so no path to 1.
        m = [
            [0, 0],
            [None, 0],
        ]
        with self.assertRaises(ValueError):
            dijkstra_shortest_path(m, 0, 1)


    def test_unreachable(self) -> None:
        m = [
            [0, 5, None],
            [None, 0, None],
            [None, None, 0],
        ]
        with self.assertRaises(ValueError) as ctx:
            dijkstra_shortest_path(m, 0, 2)
        self.assertIn("No path exists", str(ctx.exception))


    def test_direction_matters(self) -> None:
        # Edge only 1->0, not 0->1
        m = [
            [0, None],
            [7, 0],
        ]
        with self.assertRaises(ValueError):
            dijkstra_shortest_path(m, 0, 1)
        res = dijkstra_shortest_path(m, 1, 0)
        self.assertEqual(res.path, [1, 0])
        self.assertEqual(res.distance, 7.0)


    def test_larger_graph_known_answer(self) -> None:
        # 5 nodes directed
        # 0->1 2, 0->2 9
        # 1->2 3, 1->3 1
        # 3->4 2
        # 2->4 10
        m = [
            [0, 2, 9, None, None],
            [None, 0, 3, 1, None],
            [None, None, 0, None, 10],
            [None, None, None, 0, 2],
            [None, None, None, None, 0],
        ]
        res = dijkstra_shortest_path(m, 0, 4)
        self.assertEqual(res.path, [0, 1, 3, 4])
        self.assertEqual(res.distance, 5.0)


    def test_accepts_inf_string_as_no_edge(self) -> None:
        m = [
            [0, "inf", 1],
            [None, 0, None],
            [None, None, 0],
        ]
        res = dijkstra_shortest_path(m, 0, 2)
        self.assertEqual(res.path, [0, 2])
        self.assertEqual(res.distance, 1.0)


    def test_rejects_negative_weight(self) -> None:
        m = [
            [0, -1],
            [None, 0],
        ]
        with self.assertRaises(GraphInputError):
            dijkstra_shortest_path(m, 0, 1)


    def test_rejects_non_square(self) -> None:
        m = [
            [0, 1, 2],
            [None, 0, 3],
        ]
        with self.assertRaises(GraphInputError):
            dijkstra_shortest_path(m, 0, 1)


    def test_rejects_invalid_vertex(self) -> None:
        m = [
            [0, 1],
            [None, 0],
        ]
        with self.assertRaises(GraphInputError):
            dijkstra_shortest_path(m, -1, 1)
        with self.assertRaises(GraphInputError):
            dijkstra_shortest_path(m, 0, 2)


    def test_diagonal_none_is_treated_as_zero(self) -> None:
        m = [
            [None, 2],
            [None, None],
        ]
        res = dijkstra_shortest_path(m, 0, 1)
        self.assertEqual(res.path, [0, 1])
        self.assertEqual(res.distance, 2.0)




if __name__ == "__main__":
    unittest.main()
