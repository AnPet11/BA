#include <iostream>
#include <vector>
#include <limits>
#include <stdexcept>
#include <sstream>
#include <cassert>

using namespace std;

const int INF = numeric_limits<int>::max();

class GraphError : public runtime_error {
public:
    GraphError(const string& msg) : runtime_error(msg) {}
};

// ---------------------- VALIDATION ----------------------
void validateMatrix(const vector<vector<int>>& matrix) {
    if (matrix.empty()) {
        throw GraphError("Matrix must not be empty.");
    }

    size_t n = matrix.size();

    for (const auto& row : matrix) {
        if (row.size() != n) {
            throw GraphError("Matrix must be NxN.");
        }

        for (int val : row) {
            if (val < 0) {
                throw GraphError("Negative weights are not allowed.");
            }
        }
    }
}

// ---------------------- DIJKSTRA ----------------------
struct Result {
    vector<int> path;
    int distance;
    bool reachable;
};

Result dijkstra(const vector<vector<int>>& matrix, int start, int end) {
    validateMatrix(matrix);

    int n = matrix.size();

    if (start < 0 || end < 0 || start >= n || end >= n) {
        throw GraphError("Invalid start or end index.");
    }

    vector<int> dist(n, INF);
    vector<bool> visited(n, false);
    vector<int> prev(n, -1);

    dist[start] = 0;

    for (int i = 0; i < n; i++) {
        int u = -1;
        int minDist = INF;

        for (int j = 0; j < n; j++) {
            if (!visited[j] && dist[j] < minDist) {
                minDist = dist[j];
                u = j;
            }
        }

        if (u == -1) break;

        visited[u] = true;

        for (int v = 0; v < n; v++) {
            if (matrix[u][v] > 0 && !visited[v]) {
                int alt = dist[u] + matrix[u][v];

                if (alt < dist[v]) {
                    dist[v] = alt;
                    prev[v] = u;
                }
            }
        }
    }

    if (dist[end] == INF) {
        return {{}, INF, false};
    }

    vector<int> path;
    for (int at = end; at != -1; at = prev[at]) {
        path.insert(path.begin(), at);
    }

    return {path, dist[end], true};
}

// ---------------------- OUTPUT ----------------------
void printResult(const Result& res) {
    if (!res.reachable) {
        cout << "No path exists.\n";
        return;
    }

    cout << "Path: ";
    for (int node : res.path) {
        cout << node << " ";
    }
    cout << "\nDistance: " << res.distance << endl;
}

// ---------------------- UNIT TESTS ----------------------
void runTests() {
    // Test 1: simple graph
    {
        vector<vector<int>> g = {
            {0, 2, 0, 1},
            {0, 0, 3, 0},
            {0, 0, 0, 4},
            {0, 1, 0, 0}
        };

        auto res = dijkstra(g, 0, 2);
        assert(res.reachable);
        assert(res.distance == 5);
        assert((res.path == vector<int>{0,1,2}));
    }

    // Test 2: no path
    {
        vector<vector<int>> g = {
            {0, 1, 0},
            {0, 0, 0},
            {0, 0, 0}
        };

        auto res = dijkstra(g, 0, 2);
        assert(!res.reachable);
    }

    // Test 3: larger graph
    {
        vector<vector<int>> g = {
            {0, 4, 0, 0, 0, 0},
            {0, 0, 8, 0, 0, 0},
            {0, 0, 0, 7, 0, 4},
            {0, 0, 0, 0, 9, 14},
            {0, 0, 0, 0, 0, 10},
            {0, 0, 0, 0, 0, 0}
        };

        auto res = dijkstra(g, 0, 5);
        assert(res.reachable);
        assert(res.distance == 16);
        assert((res.path == vector<int>{0,1,2,5}));
    }

    // Test 4: invalid matrix
    {
        try {
            vector<vector<int>> g = {{1,2},{3}};
            dijkstra(g, 0, 1);
            assert(false);
        } catch (const GraphError&) {}
    }

    // Test 5: negative weights
    {
        try {
            vector<vector<int>> g = {{0,-1},{0,0}};
            dijkstra(g, 0, 1);
            assert(false);
        } catch (const GraphError&) {}
    }

    cout << "All tests passed!\n";
}

// ---------------------- MAIN ----------------------
int main() {
    try {
        runTests();

        int n;
        cout << "Enter number of nodes: ";
        cin >> n;

        if (n <= 0) throw GraphError("Invalid graph size.");

        vector<vector<int>> matrix(n, vector<int>(n));

        cout << "Enter adjacency matrix:\n";
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                cin >> matrix[i][j];
            }
        }

        int start, end;
        cout << "Start node: ";
        cin >> start;
        cout << "End node: ";
        cin >> end;

        auto result = dijkstra(matrix, start, end);
        printResult(result);

    } catch (const GraphError& e) {
        cerr << "Graph error: " << e.what() << endl;
    } catch (const exception& e) {
        cerr << "Unexpected error: " << e.what() << endl;
    }

    return 0;
}
