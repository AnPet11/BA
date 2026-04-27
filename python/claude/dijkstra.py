"""
Dijkstra algoritm suunatud graafis lühima tee leidmiseks.
Sisend: naabrusmaatriks ja alguspunkt.
Väljund: lühimad teed kõikidesse tippudesse koos kogupikkusega.
"""

import heapq
import unittest
from typing import Optional


# ─────────────────────────────────────────────
# Sisendi valideerimine
# ─────────────────────────────────────────────

def _validate_matrix(matrix: list[list[float]], source: int) -> None:
    """Kontrollib naabrusmaatriks ja alguspunkti korrektsust."""
    if not isinstance(matrix, list) or not matrix:
        raise ValueError("Naabrusmaatriks ei tohi olla tühi.")

    n = len(matrix)

    for i, row in enumerate(matrix):
        if not isinstance(row, list):
            raise TypeError(f"Rida {i} peab olema list, mitte {type(row).__name__}.")
        if len(row) != n:
            raise ValueError(
                f"Maatriks peab olema ruudukujuline. "
                f"Rida {i} pikkus {len(row)} != {n}."
            )
        for j, val in enumerate(row):
            if not isinstance(val, (int, float)):
                raise TypeError(
                    f"Kaal [{i}][{j}] peab olema arv, mitte {type(val).__name__}."
                )
            if val < 0:
                raise ValueError(
                    f"Dijkstra algoritm ei toeta negatiivseid kaale. "
                    f"Kaal [{i}][{j}] = {val}."
                )

    if not isinstance(source, int):
        raise TypeError(f"Alguspunkt peab olema täisarv, mitte {type(source).__name__}.")
    if not (0 <= source < n):
        raise IndexError(
            f"Alguspunkt {source} on väljaspool vahemikku [0, {n - 1}]."
        )


# ─────────────────────────────────────────────
# Dijkstra põhialgoritm
# ─────────────────────────────────────────────

def dijkstra(
    matrix: list[list[float]],
    source: int,
) -> tuple[list[float], list[Optional[int]]]:
    """
    Leiab lühimad teed allikast kõikidesse tippudesse.

    Args:
        matrix: Naabrusmaatriks kaalutud suunatud graafiga.
                0 tähendab puuduvat serva (v.a diagonaal).
        source: Algtipp (0-indekseeritud).

    Returns:
        dist:   Kauguste list. dist[v] = lühim tee source → v.
                float('inf') tähistab, et tipp pole saavutatav.
        prev:   Eelkäijate list. prev[v] = eelmine tipp optimaalsel teel.
                None, kui tipp pole saavutatav või on allikas ise.
    """
    _validate_matrix(matrix, source)

    n = len(matrix)
    INF = float("inf")
    dist = [INF] * n
    prev: list[Optional[int]] = [None] * n
    dist[source] = 0

    # (kaugus, tipp) – miinimumilist
    heap: list[tuple[float, int]] = [(0, source)]

    while heap:
        d, u = heapq.heappop(heap)

        # Aegunud kirje – ignoreeri
        if d > dist[u]:
            continue

        for v in range(n):
            weight = matrix[u][v]
            # 0 = serv puudub (diagonaal eiratakse automaatselt, kuna dist[u]+0 = dist[u])
            if weight == 0 or u == v:
                continue
            new_dist = dist[u] + weight
            if new_dist < dist[v]:
                dist[v] = new_dist
                prev[v] = u
                heapq.heappush(heap, (new_dist, v))

    return dist, prev


# ─────────────────────────────────────────────
# Tee rekonstrueerimine
# ─────────────────────────────────────────────

def reconstruct_path(
    prev: list[Optional[int]],
    source: int,
    target: int,
) -> Optional[list[int]]:
    """
    Rekonstrueerib lühima tee allikast sihtpunkti.

    Returns:
        Tippude list (kaasa arvatud source ja target),
        või None kui teed ei eksisteeri.
    """
    if prev[target] is None and target != source:
        return None  # tee puudub

    path: list[int] = []
    node: Optional[int] = target
    while node is not None:
        path.append(node)
        node = prev[node]

    path.reverse()

    # Kontrolli, et tee algab allikast (ühendamata komponent)
    if path[0] != source:
        return None

    return path


# ─────────────────────────────────────────────
# Kõrgetasemeline kasutajaliides
# ─────────────────────────────────────────────

def find_shortest_path(
    matrix: list[list[float]],
    source: int,
    target: int,
) -> dict:
    """
    Leiab ja väljastab lühima tee allikast sihtpunkti.

    Returns:
        Sõnastik väljadega:
          - 'path'   : tippude järjend või None
          - 'length' : kogupikkus või None
          - 'found'  : True kui tee on olemas
          - 'message': inimloetav selgitus
    """
    _validate_matrix(matrix, source)

    n = len(matrix)
    if not isinstance(target, int):
        raise TypeError(f"Sihtpunkt peab olema täisarv, mitte {type(target).__name__}.")
    if not (0 <= target < n):
        raise IndexError(
            f"Sihtpunkt {target} on väljaspool vahemikku [0, {n - 1}]."
        )

    dist, prev = dijkstra(matrix, source)

    if dist[target] == float("inf"):
        return {
            "path": None,
            "length": None,
            "found": False,
            "message": (
                f"Teed tipust {source} tippu {target} ei eksisteeri."
            ),
        }

    path = reconstruct_path(prev, source, target)
    arrow = " → ".join(map(str, path))

    return {
        "path": path,
        "length": dist[target],
        "found": True,
        "message": (
            f"Lühim tee: {arrow}\n"
            f"Kogupikkus: {dist[target]}"
        ),
    }


# ─────────────────────────────────────────────
# Ühiktestid
# ─────────────────────────────────────────────

class TestDijkstra(unittest.TestCase):

    # --- Lihtsad positiivsed testid ---

    def test_direct_edge(self):
        """Otsene ühendus kahe tipu vahel."""
        matrix = [
            [0, 5, 0],
            [0, 0, 3],
            [0, 0, 0],
        ]
        result = find_shortest_path(matrix, 0, 1)
        self.assertTrue(result["found"])
        self.assertEqual(result["path"], [0, 1])
        self.assertEqual(result["length"], 5)

    def test_two_hop_path(self):
        """Parim tee läbi vahepunkti."""
        matrix = [
            [0, 1, 10],
            [0, 0, 2],
            [0, 0, 0],
        ]
        result = find_shortest_path(matrix, 0, 2)
        self.assertTrue(result["found"])
        self.assertEqual(result["path"], [0, 1, 2])
        self.assertEqual(result["length"], 3)

    def test_source_equals_target(self):
        """Allikas on sihtpunkt – tee pikkus 0."""
        matrix = [[0, 4], [0, 0]]
        result = find_shortest_path(matrix, 0, 0)
        self.assertTrue(result["found"])
        self.assertEqual(result["path"], [0])
        self.assertEqual(result["length"], 0)

    def test_no_path_exists(self):
        """Teed ei eksisteeri (ühepoolne graaf)."""
        matrix = [
            [0, 7],
            [0, 0],
        ]
        result = find_shortest_path(matrix, 1, 0)
        self.assertFalse(result["found"])
        self.assertIsNone(result["path"])

    # --- Keerulisemad graafistruktuurid ---

    def test_five_node_graph(self):
        """Klassikaline 5-tipu näide."""
        matrix = [
            [0, 10,  0,  0, 5],
            [0,  0,  1,  0, 2],
            [0,  0,  0,  4, 0],
            [0,  0,  6,  0, 0],
            [0,  3,  9,  2, 0],
        ]
        result = find_shortest_path(matrix, 0, 2)
        self.assertTrue(result["found"])
        # Optimaalne: 0→4→1→2 pikkusega 5+3+1=9
        self.assertEqual(result["length"], 9)
        self.assertEqual(result["path"], [0, 4, 1, 2])

    def test_single_node(self):
        """Ühe tipuga graaf."""
        result = find_shortest_path([[0]], 0, 0)
        self.assertTrue(result["found"])
        self.assertEqual(result["length"], 0)

    def test_disconnected_components(self):
        """Eraldi komponendid – tee puudub."""
        matrix = [
            [0, 1, 0, 0],
            [0, 0, 1, 0],
            [0, 0, 0, 0],
            [0, 0, 0, 0],
        ]
        result = find_shortest_path(matrix, 0, 3)
        self.assertFalse(result["found"])

    def test_multiple_paths_chooses_shortest(self):
        """Mitu teed – valitakse lüheim."""
        matrix = [
            [0, 1, 10, 0],
            [0, 0,  5, 0],
            [0, 0,  0, 1],
            [0, 0,  0, 0],
        ]
        result = find_shortest_path(matrix, 0, 2)
        self.assertTrue(result["found"])
        self.assertEqual(result["length"], 6)   # 0→1→2
        self.assertEqual(result["path"], [0, 1, 2])

    def test_float_weights(self):
        """Ujukomaarvulised kaalud."""
        matrix = [
            [0, 1.5, 0],
            [0, 0,   2.3],
            [0, 0,   0],
        ]
        result = find_shortest_path(matrix, 0, 2)
        self.assertAlmostEqual(result["length"], 3.8)

    # --- Veakäsitluse testid ---

    def test_negative_weight_raises(self):
        """Negatiivsed kaalud peavad andma vea."""
        matrix = [[0, -1], [0, 0]]
        with self.assertRaises(ValueError):
            find_shortest_path(matrix, 0, 1)

    def test_non_square_matrix_raises(self):
        """Mitte-ruudukujuline maatriks."""
        with self.assertRaises(ValueError):
            find_shortest_path([[0, 1], [0, 0], [0, 0]], 0, 1)

    def test_empty_matrix_raises(self):
        """Tühi maatriks."""
        with self.assertRaises(ValueError):
            find_shortest_path([], 0, 1)

    def test_source_out_of_range(self):
        """Alguspunkt väljaspool graafi."""
        with self.assertRaises(IndexError):
            find_shortest_path([[0]], 5, 0)

    def test_target_out_of_range(self):
        """Sihtpunkt väljaspool graafi."""
        with self.assertRaises(IndexError):
            find_shortest_path([[0]], 0, 5)

    def test_non_integer_source(self):
        """Mittearv alguspunktina."""
        with self.assertRaises(TypeError):
            find_shortest_path([[0]], "a", 0)

    def test_invalid_cell_type(self):
        """Mittearv maatriksi lahtris."""
        with self.assertRaises(TypeError):
            find_shortest_path([[0, "x"], [0, 0]], 0, 1)


# ─────────────────────────────────────────────
# Käivitamine käsurealt
# ─────────────────────────────────────────────

def _demo() -> None:
    print("=" * 55)
    print("  Dijkstra algoritmi demo")
    print("=" * 55)

    matrix = [
        [0, 10,  0,  0, 5],
        [0,  0,  1,  0, 2],
        [0,  0,  0,  4, 0],
        [0,  0,  6,  0, 0],
        [0,  3,  9,  2, 0],
    ]

    print("\nNaabrusmaatriks (5 tippu):")
    for row in matrix:
        print(" ", row)

    for target in range(5):
        result = find_shortest_path(matrix, source=0, target=target)
        print(f"\n  Tipp 0 → {target}: {result['message']}")

    # Puuduva tee näide
    print("\n" + "-" * 55)
    disconnected = [
        [0, 1, 0],
        [0, 0, 1],
        [0, 0, 0],
    ]
    result = find_shortest_path(disconnected, source=2, target=0)
    print(f"\n  {result['message']}")
    print("=" * 55)


if __name__ == "__main__":
    import sys

    if "--test" in sys.argv or "-t" in sys.argv:
        # Käivita ainult ühiktestid
        sys.argv = [sys.argv[0]]
        unittest.main(verbosity=2)
    else:
        _demo()
        print("\nÜhiktestide käivitamiseks lisa lipp:  python dijkstra.py --test\n")
