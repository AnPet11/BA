import heapq

# --- Erindite (Exceptions) definitsioonid ---
class GraphError(Exception):
    """Baasklass graafiga seotud vigadele."""
    pass

class NoPathError(GraphError):
    """Tõstatatakse, kui algus- ja sihtpunkti vahel puudub tee."""
    pass

class InvalidInputError(GraphError):
    """Tõstatatakse, kui sisendandmed on vigased või loogikavastased."""
    pass

# --- Põhialgoritm ---
def find_shortest_path(adj_matrix, start_node, target_node):
    """
    Leiab lühima tee suunatud graafis kasutades Dijkstra algoritmi.
    
    Parameetrid:
        adj_matrix (list of lists): Graafi naabrusmaatriks.
        start_node (int): Alguspunkti indeks (0-põhine).
        target_node (int): Sihtpunkti indeks (0-põhine).
        
    Tagastab:
        dict: Sisaldab leitud teed (list) ja selle kogupikkust (int/float).
    """
    # 1. Sisendi kontroll (Error Handling)
    if not isinstance(adj_matrix, list) or not all(isinstance(row, list) for row in adj_matrix):
        raise InvalidInputError("Naabrusmaatriks peab olema kahemõõtmeline järjend (list of lists).")

    num_nodes = len(adj_matrix)
    if num_nodes == 0:
        raise InvalidInputError("Graaf on tühi.")

    for row in adj_matrix:
        if len(row) != num_nodes:
            raise InvalidInputError("Naabrusmaatriks peab olema ruutmaatriks.")

    if not (0 <= start_node < num_nodes) or not (0 <= target_node < num_nodes):
        raise InvalidInputError(f"Algus- või sihtpunkt on väljaspool graafi piire (0 kuni {num_nodes - 1}).")

    for i in range(num_nodes):
        for j in range(num_nodes):
            if adj_matrix[i][j] < 0:
                raise InvalidInputError("Dijkstra algoritm ei toeta negatiivsete kaaludega servi.")

    # 2. Andmestruktuuride initsialiseerimine
    # Vahemaad kõikidesse tippudesse (alguses lõpmatus)
    distances = {node: float('inf') for node in range(num_nodes)}
    distances[start_node] = 0
    
    # Eelmised tipud tee taastamiseks
    previous_nodes = {node: None for node in range(num_nodes)}
    
    # Prioriteedijärjekord: hoiab tuplesid (hetke_kaugus, tipu_indeks)
    priority_queue = [(0, start_node)]

    # 3. Dijkstra algoritmi põhitsükkel
    while priority_queue:
        current_distance, current_node = heapq.heappop(priority_queue)

        # Optimeerimine: kui jõudsime sihtpunkti, võime otsingu peatada
        if current_node == target_node:
            break

        # Kui oleme leidnud juba lühema tee sellesse tippu, ignoreerime
        if current_distance > distances[current_node]:
            continue

        # Vaatame läbi kõik naabrid
        for neighbor in range(num_nodes):
            weight = adj_matrix[current_node][neighbor]
            
            # Eeldame, et 0 tähendab serva puudumist
            if weight > 0:
                distance = current_distance + weight

                # Kui uus leitud tee on lühem kui varem teadaolev
                if distance < distances[neighbor]:
                    distances[neighbor] = distance
                    previous_nodes[neighbor] = current_node
                    heapq.heappush(priority_queue, (distance, neighbor))

    # 4. Tee olemasolu kontroll ja tulemuse taastamine
    if distances[target_node] == float('inf'):
        raise NoPathError(f"Teed alguspunktist {start_node} sihtpunkti {target_node} ei eksisteeri.")

    path = []
    current = target_node
    while current is not None:
        path.append(current)
        current = previous_nodes[current]
        
    path.reverse() # Kuna taastasime tee lõpust algusesse, pöörame selle ümber

    return {
        "path": path,
        "total_distance": distances[target_node]
    }
import unittest

class TestDijkstraShortestPath(unittest.TestCase):

    def setUp(self):
        # Näidisgraaf 1: Standardne graaf
        # 0 -> 1 (4), 0 -> 2 (1)
        # 2 -> 1 (2), 1 -> 3 (1), 2 -> 3 (5)
        self.standard_matrix = [
            [0, 4, 1, 0],
            [0, 0, 0, 1],
            [0, 2, 0, 5],
            [0, 0, 0, 0]
        ]
        
        # Näidisgraaf 2: Eraldatud komponentidega graaf
        self.disconnected_matrix = [
            [0, 1, 0, 0],
            [0, 0, 0, 0],
            [0, 0, 0, 1],
            [0, 0, 0, 0]
        ]

    def test_shortest_path_success(self):
        """Testib edukat lühima tee leidmist."""
        result = find_shortest_path(self.standard_matrix, 0, 3)
        self.assertEqual(result["total_distance"], 4)
        self.assertEqual(result["path"], [0, 2, 1, 3])

    def test_no_path_exists(self):
        """Testib erindit, kui teed sihtpunkti ei eksisteeri."""
        with self.assertRaises(NoPathError):
            # Tipp 3 ei oma ühtegi väljuvat serva
            find_shortest_path(self.standard_matrix, 3, 0)
            
        with self.assertRaises(NoPathError):
            # Graaf on lahti ühendatud 0->1 ja 2->3
            find_shortest_path(self.disconnected_matrix, 0, 3)

    def test_start_equals_target(self):
        """Testib olukorda, kus algus- ja sihtpunkt on samad."""
        result = find_shortest_path(self.standard_matrix, 2, 2)
        self.assertEqual(result["total_distance"], 0)
        self.assertEqual(result["path"], [2])

    def test_invalid_matrix_shape(self):
        """Testib sisendikontrolli vigase maatriksi korral (mitte-ruut)."""
        invalid_matrix = [
            [0, 1],
            [1, 0, 1]
        ]
        with self.assertRaises(InvalidInputError):
            find_shortest_path(invalid_matrix, 0, 1)

    def test_out_of_bounds_nodes(self):
        """Testib olukorda, kus küsitakse tippu, mida ei eksisteeri."""
        with self.assertRaises(InvalidInputError):
            find_shortest_path(self.standard_matrix, 0, 10)
        
        with self.assertRaises(InvalidInputError):
            find_shortest_path(self.standard_matrix, -1, 2)

    def test_negative_weights(self):
        """Testib negatiivsete kaalude tuvastamist."""
        matrix_with_negatives = [
            [0, -5],
            [0, 0]
        ]
        with self.assertRaises(InvalidInputError):
            find_shortest_path(matrix_with_negatives, 0, 1)

if __name__ == '__main__':
    unittest.main(argv=['first-arg-is-ignored'], exit=False)
