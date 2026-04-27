#include <iostream>
#include <vector>
#include <queue>
#include <limits>
#include <algorithm>
#include <stdexcept>
#include <cassert>

using namespace std;

const int INF = numeric_limits<int>::max();

struct Result {
    vector<int> path;
    int distance;
    bool exists;
};

class DijkstraSolver {
public:
    /**
     * Arvutab lühima tee alguspunktist kõigisse teistesse tippudesse.
     * @param matrix Naabrusmaatriks (0 tähendab kaare puudumist, positiivne arv kaare kaalu).
     * @param startNode Algustipp (0-indekseeritud).
     * @param endNode Sihttipp (0-indekseeritud).
     */
    static Result findShortestPath(const vector<vector<int>>& matrix, int startNode, int endNode) {
        int n = matrix.size();

        // 1. Sisendi valideerimine
        if (n == 0) throw invalid_argument("Graaf on tühi.");
        if (startNode < 0 || startNode >= n || endNode < 0 || endNode >= n) {
            throw out_of_range("Algus- või sihtpunkt on väljaspool graafi piire.");
        }
        for (const auto& row : matrix) {
            if (row.size() != n) throw invalid_argument("Maatriks peab olema ruutmaatriks.");
        }

        vector<int> dist(n, INF);
        vector<int> parent(n, -1);
        // Prioriteedijärjekord: paarid (kaugus, tipp), väikseim kaugus eesotsas
        priority_queue<pair<int, int>, vector<pair<int, int>>, greater<pair<int, int>>> pq;

        dist[startNode] = 0;
        pq.push({0, startNode});

        while (!pq.empty()) {
            int d = pq.top().first;
            int u = pq.top().second;
            pq.pop();

            if (d > dist[u]) continue;

            for (int v = 0; v < n; ++v) {
                // Kui kaar eksisteerib (väärtus > 0)
                if (matrix[u][v] > 0) {
                    int weight = matrix[u][v];
                    if (dist[u] + weight < dist[v]) {
                        dist[v] = dist[u] + weight;
                        parent[v] = u;
                        pq.push({dist[v], v});
                    }
                }
            }
        }

        // 2. Tee rekonstrueerimine
        if (dist[endNode] == INF) {
            return {{}, INF, false};
        }

        vector<int> path;
        for (int v = endNode; v != -1; v = parent[v]) {
            path.push_back(v);
        }
        reverse(path.begin(), path.end());

        return {path, dist[endNode], true};
    }
};

/**
 * Funktsioon tulemuste viisakaks väljastamiseks
 */
void printResult(int start, int end, const Result& res) {
    if (!res.exists) {
        cout << "Tee punktist " << start << " punkti " << end << " puudub." << endl;
    } else {
        cout << "Lühim tee (" << start << " -> " << end << "): ";
        for (size_t i = 0; i < res.path.size(); ++i) {
            cout << res.path[i] << (i == res.path.size() - 1 ? "" : " -> ");
        }
        cout << "\nKogupikkus: " << res.distance << endl;
    }
    cout << "-----------------------------------" << endl;
}

/**
 * ÜHIKTESTID
 */
void runTests() {
    cout << "Käivitan ühiktestid...\n" << endl;

    // Test 1: Tavaline graaf
    vector<vector<int>> g1 = {
        {0, 10, 0, 5},
        {0, 0, 1, 2},
        {0, 0, 0, 0},
        {0, 3, 9, 0}
    };
    Result r1 = DijkstraSolver::findShortestPath(g1, 0, 2);
    assert(r1.exists == true);
    assert(r1.distance == 9); // 0 -> 3 -> 1 -> 2 (5+3+1)
    cout << "Test 1 läbitud: Standardne lühim tee." << endl;

    // Test 2: Tee puudub
    vector<vector<int>> g2 = {
        {0, 5, 0},
        {0, 0, 0},
        {1, 0, 0}
    };
    Result r2 = DijkstraSolver::findShortestPath(g2, 0, 2);
    assert(r2.exists == false);
    cout << "Test 2 läbitud: Tee puudumise tuvastus." << endl;

    // Test 3: Üks tipp (algus == lõpp)
    vector<vector<int>> g3 = {{0}};
    Result r3 = DijkstraSolver::findShortestPath(g3, 0, 0);
    assert(r3.distance == 0);
    assert(r3.path.size() == 1);
    cout << "Test 3 läbitud: Alguspunkt on sihtpunkt." << endl;

    // Test 4: Veakäsitlus (vigane sisend)
    try {
        DijkstraSolver::findShortestPath(g1, -1, 2);
        assert(false); // Ei tohiks siia jõuda
    } catch (const out_of_range& e) {
        cout << "Test 4 läbitud: Vigase indeksi püüdmine." << endl;
    }

    cout << "\nKõik testid edukalt läbitud!" << endl;
    cout << "===================================\n" << endl;
}

int main() {
    // Käivita testid
    runTests();

    // Näidisjuhtum kasutajale
    vector<vector<int>> graph = {
        {0, 4, 0, 0, 0, 0, 0, 8, 0},
        {4, 0, 8, 0, 0, 0, 0, 11, 0},
        {0, 8, 0, 7, 0, 4, 0, 0, 2},
        {0, 0, 7, 0, 9, 14, 0, 0, 0},
        {0, 0, 0, 9, 0, 10, 0, 0, 0},
        {0, 0, 4, 14, 10, 0, 2, 0, 0},
        {0, 0, 0, 0, 0, 2, 0, 1, 6},
        {8, 11, 0, 0, 0, 0, 1, 0, 7},
        {0, 0, 2, 0, 0, 0, 6, 7, 0}
    };

    try {
        int start = 0, end = 4;
        Result res = DijkstraSolver::findShortestPath(graph, start, end);
        printResult(start, end, res);
        
        start = 0, end = 8;
        res = DijkstraSolver::findShortestPath(graph, start, end);
        printResult(start, end, res);
    } catch (const exception& e) {
        cerr << "Viga: " << e.what() << endl;
    }

    return 0;
}
