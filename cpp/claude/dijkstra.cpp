/**
 * ============================================================
 *  Dijkstra algoritm suunatud kaalutud graafis
 *  Sisend : naabrusmaatriksina esitatud graaf
 *  Valjund: lyhim tee iga sihtpunkti kohta + kogupikkus
 * ============================================================
 */

#include <iostream>
#include <vector>
#include <queue>
#include <limits>
#include <stdexcept>
#include <string>
#include <sstream>
#include <cassert>
#include <iomanip>
#include <functional>

// Tyybid
using Weight = int;
using Matrix = std::vector<std::vector<Weight>>;
using Path   = std::vector<int>;

constexpr Weight INF = std::numeric_limits<Weight>::max();

// Tulemuse struktuurid
struct DijkstraResult {
    std::vector<Weight> dist;
    std::vector<int>    prev;
};

struct ShortestPath {
    Path   path;
    Weight cost;
    bool   exists;
};

// ── Sisendi valideerimine ──────────────────────────────────────────
void validateMatrix(const Matrix& matrix) {
    if (matrix.empty())
        throw std::invalid_argument("Viga: naabrusmaatriksit ei leitud (tyhi).");

    const size_t n = matrix.size();
    for (size_t i = 0; i < n; ++i) {
        if (matrix[i].size() != n)
            throw std::invalid_argument(
                "Viga: rida " + std::to_string(i) +
                " pikkus on " + std::to_string(matrix[i].size()) +
                ", oodati " + std::to_string(n) + " (ruutmaatriks).");
        for (size_t j = 0; j < n; ++j) {
            if (matrix[i][j] < 0)
                throw std::invalid_argument(
                    "Viga: negatiivne kaal [" + std::to_string(i) +
                    "][" + std::to_string(j) + "] = " +
                    std::to_string(matrix[i][j]) + ".");
        }
    }
}

void validateNode(int node, int n, const std::string& label) {
    if (node < 0 || node >= n)
        throw std::out_of_range(
            "Viga: " + label + " solm " + std::to_string(node) +
            " on valjaspool vahemikku [0, " + std::to_string(n - 1) + "].");
}

// ── Dijkstra pohialgoritm ──────────────────────────────────────────
// Ajakeerukus : O((V + E) * log V)
// Ruumikeerukus: O(V)
DijkstraResult dijkstra(const Matrix& matrix, int source) {
    validateMatrix(matrix);
    const int n = static_cast<int>(matrix.size());
    validateNode(source, n, "alg");

    std::vector<Weight> dist(n, INF);
    std::vector<int>    prev(n, -1);

    using PQEntry = std::pair<Weight, int>;
    std::priority_queue<PQEntry,
                        std::vector<PQEntry>,
                        std::greater<PQEntry>> pq;

    dist[source] = 0;
    pq.push({0, source});

    while (!pq.empty()) {
        auto [d, u] = pq.top();
        pq.pop();

        if (d > dist[u]) continue; // laisad kustutused

        for (int v = 0; v < n; ++v) {
            Weight w = matrix[u][v];
            if (w == 0) continue;

            if (dist[u] != INF && dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
                prev[v] = u;
                pq.push({dist[v], v});
            }
        }
    }

    return {dist, prev};
}

// ── Tee rekonstrueerimine ──────────────────────────────────────────
ShortestPath getPath(const DijkstraResult& result, int target) {
    const int n = static_cast<int>(result.dist.size());
    validateNode(target, n, "siht");

    if (result.dist[target] == INF)
        return {{}, INF, false};

    Path path;
    for (int cur = target; cur != -1; cur = result.prev[cur])
        path.insert(path.begin(), cur);

    return {path, result.dist[target], true};
}

// ── Vaaljund ──────────────────────────────────────────────────────
void printPath(const ShortestPath& sp, int source, int target,
               const std::vector<std::string>& labels = {}) {
    auto name = [&](int i) -> std::string {
        if (!labels.empty() && i < (int)labels.size()) return labels[i];
        return std::to_string(i);
    };
    std::cout << "Algsolm  : " << name(source) << "\n";
    std::cout << "Sihtpunkt: " << name(target) << "\n";
    if (!sp.exists) {
        std::cout << "Tulemus  : tee PUUDUB\n";
        return;
    }
    std::cout << "Tee      : ";
    for (size_t i = 0; i < sp.path.size(); ++i) {
        if (i > 0) std::cout << " -> ";
        std::cout << name(sp.path[i]);
    }
    std::cout << "\nKogupikkus: " << sp.cost << "\n";
}

void printAllDistances(const DijkstraResult& result, int source,
                       const std::vector<std::string>& labels = {}) {
    const int n = static_cast<int>(result.dist.size());
    auto name = [&](int i) -> std::string {
        if (!labels.empty() && i < (int)labels.size()) return labels[i];
        return std::to_string(i);
    };
    std::cout << "\nKoik kaugused algssolmest " << name(source) << ":\n";
    std::cout << std::string(35, '-') << "\n";
    for (int i = 0; i < n; ++i) {
        std::cout << "  " << std::setw(3) << name(source)
                  << " -> " << std::setw(3) << name(i) << " : ";
        if (result.dist[i] == INF)
            std::cout << "INF  (ligipaasmatu)\n";
        else
            std::cout << result.dist[i] << "\n";
    }
}

// ================================================================
//  YHIKTESTID
// ================================================================

static int g_pass = 0, g_fail = 0;

void runTest(const std::string& name, std::function<void()> fn) {
    try {
        fn();
        std::cout << "  LABIS  " << name << "\n";
        ++g_pass;
    } catch (const std::exception& e) {
        std::cout << "  KUKUS  " << name << "\n"
                  << "          -> " << e.what() << "\n";
        ++g_fail;
    }
}

void assertEqual(Weight actual, Weight expected, const std::string& msg) {
    if (actual != expected) {
        std::ostringstream ss;
        ss << msg << ": oodati " << expected << ", saadi " << actual;
        throw std::runtime_error(ss.str());
    }
}
void assertTrue(bool cond, const std::string& msg) {
    if (!cond) throw std::runtime_error(msg);
}

void runAllTests() {
    std::cout << "\n" << std::string(50, '=') << "\n";
    std::cout << "  YHIKTESTID\n";
    std::cout << std::string(50, '=') << "\n\n";

    // Test 1 — lihtne 5-sõlmeline graaf
    runTest("Lihtne graaf A->E, kulu 6", []() {
        Matrix m = {
            {0, 4, 2, 0, 0},
            {4, 0, 5,10, 0},
            {2, 5, 0, 3, 8},
            {0,10, 3, 0, 1},
            {0, 0, 8, 1, 0}
        };
        auto res = dijkstra(m, 0);
        auto sp  = getPath(res, 4);
        // Lyhim: A->C->D->E = 2+3+1 = 6
        assertTrue(sp.exists, "Tee peaks eksisteerima");
        assertEqual(sp.cost, 6, "kulu");
        assertTrue(sp.path.front() == 0 && sp.path.back() == 4, "Tee otspunktid");
    });

    // Test 2 — kaudtee lühem kui otsetee
    runTest("Kaudtee lyhem kui otsetee", []() {
        Matrix m = {
            {0,10, 1, 0},
            {0, 0, 0, 1},
            {0, 1, 0, 5},
            {0, 0, 0, 0}
        };
        auto res = dijkstra(m, 0);
        auto sp  = getPath(res, 3);
        // 0->2->1->3 = 1+1+1 = 3, otse 0->1->3 = 10+1 = 11
        assertTrue(sp.exists, "Tee peaks eksisteerima");
        assertEqual(sp.cost, 3, "kulu");
    });

    // Test 3 — lahutatud graaf
    runTest("Lahutatud graaf: tee puudub", []() {
        Matrix m = {
            {0, 1, 0, 0},
            {1, 0, 0, 0},
            {0, 0, 0, 1},
            {0, 0, 1, 0}
        };
        auto res = dijkstra(m, 0);
        auto sp  = getPath(res, 3);
        assertTrue(!sp.exists, "Tee ei peaks eksisteerima");
        assertEqual(sp.cost, INF, "kulu peaks olema INF");
    });

    // Test 4 — algsolm === sihtpunkt
    runTest("Algsolm === sihtpunkt, kulu 0", []() {
        Matrix m = {{0, 5}, {5, 0}};
        auto res = dijkstra(m, 0);
        auto sp  = getPath(res, 0);
        assertTrue(sp.exists, "Tee peaks eksisteerima");
        assertEqual(sp.cost, 0, "kulu");
        assertTrue(sp.path.size() == 1 && sp.path[0] == 0, "Ainult algssolm");
    });

    // Test 5 — yhe sõlmega graaf
    runTest("Yhe solmega graaf", []() {
        Matrix m = {{0}};
        auto res = dijkstra(m, 0);
        auto sp  = getPath(res, 0);
        assertTrue(sp.exists, "Tee peaks eksisteerima");
        assertEqual(sp.cost, 0, "kulu");
    });

    // Test 6 — lineaarne ahel
    runTest("Lineaarne ahel A->E, kulu 11", []() {
        Matrix m = {
            {0, 3, 0, 0, 0},
            {0, 0, 2, 0, 0},
            {0, 0, 0, 5, 0},
            {0, 0, 0, 0, 1},
            {0, 0, 0, 0, 0}
        };
        auto res = dijkstra(m, 0);
        auto sp  = getPath(res, 4);
        assertTrue(sp.exists, "Tee peaks eksisteerima");
        assertEqual(sp.cost, 11, "kulu");
        assertTrue((int)sp.path.size() == 5, "Tee laabib 5 solme");
    });

    // Test 7 — 7-sõlmeline graaf
    runTest("7-solmeline graaf A->E, kulu 20", []() {
        Matrix m = {
            { 0,  7,  9,  0,  0, 14,  0},
            { 7,  0, 10, 15,  0,  0,  0},
            { 9, 10,  0, 11,  0,  2,  0},
            { 0, 15, 11,  0,  6,  0,  0},
            { 0,  0,  0,  6,  0,  9,  0},
            {14,  0,  2,  0,  9,  0,  0},
            { 0,  0,  0,  0,  0,  0,  0}
        };
        auto res = dijkstra(m, 0);
        auto sp  = getPath(res, 4);
        // A->C->F->E = 9+2+9 = 20
        assertTrue(sp.exists, "Tee peaks eksisteerima");
        assertEqual(sp.cost, 20, "kulu");
    });

    // Test 8 — koik kaugused
    runTest("Koik kaugused 5-solmelises graafis", []() {
        Matrix m = {
            {0, 4, 2, 0, 0},
            {4, 0, 5,10, 0},
            {2, 5, 0, 3, 8},
            {0,10, 3, 0, 1},
            {0, 0, 8, 1, 0}
        };
        auto res = dijkstra(m, 0);
        assertEqual(res.dist[0], 0, "A->A");
        assertEqual(res.dist[1], 4, "A->B");
        assertEqual(res.dist[2], 2, "A->C");
        assertEqual(res.dist[3], 5, "A->D");
        assertEqual(res.dist[4], 6, "A->E");
    });

    // Test 9 — vigane sisend: tyhi maatriks
    runTest("Veakasitlus: tyhi maatriks", []() {
        bool caught = false;
        try { dijkstra({}, 0); }
        catch (const std::invalid_argument&) { caught = true; }
        assertTrue(caught, "Peab viskama std::invalid_argument");
    });

    // Test 10 — vigane sisend: negatiivne kaal
    runTest("Veakasitlus: negatiivne kaal", []() {
        Matrix m = {{0, -3}, {-3, 0}};
        bool caught = false;
        try { dijkstra(m, 0); }
        catch (const std::invalid_argument&) { caught = true; }
        assertTrue(caught, "Peab viskama std::invalid_argument");
    });

    // Test 11 — vigane sisend: solm valjaspool vahemikku
    runTest("Veakasitlus: algsolm valjaspool vahemikku", []() {
        Matrix m = {{0, 1}, {1, 0}};
        bool caught = false;
        try { dijkstra(m, 5); }
        catch (const std::out_of_range&) { caught = true; }
        assertTrue(caught, "Peab viskama std::out_of_range");
    });

    // Test 12 — mitte-ruutmaatriks
    runTest("Veakasitlus: mitte-ruutmaatriks", []() {
        Matrix m = {{0, 1, 2}, {1, 0}};
        bool caught = false;
        try { dijkstra(m, 0); }
        catch (const std::invalid_argument&) { caught = true; }
        assertTrue(caught, "Peab viskama std::invalid_argument");
    });

    // Kokkuvote
    std::cout << "\n" << std::string(50, '-') << "\n";
    std::cout << "Tulemus: " << g_pass << "/" << (g_pass + g_fail)
              << " testi labis";
    if (g_fail > 0)
        std::cout << "  (" << g_fail << " kukus labi)";
    std::cout << "\n" << std::string(50, '=') << "\n";
}

// ── Demo ──────────────────────────────────────────────────────────
void runDemo() {
    std::cout << "\n" << std::string(50, '=') << "\n";
    std::cout << "  DEMO — 7-solmeline graaf\n";
    std::cout << std::string(50, '=') << "\n";

    std::vector<std::string> labels = {"A","B","C","D","E","F","G"};
    Matrix m = {
        { 0,  7,  9,  0,  0, 14,  0},
        { 7,  0, 10, 15,  0,  0,  0},
        { 9, 10,  0, 11,  0,  2,  0},
        { 0, 15, 11,  0,  6,  0,  0},
        { 0,  0,  0,  6,  0,  9,  0},
        {14,  0,  2,  0,  9,  0,  0},
        { 0,  0,  0,  0,  0,  0,  0}
    };

    auto result = dijkstra(m, 0);
    printAllDistances(result, 0, labels);

    for (int t : {1, 4, 6}) {
        std::cout << "\n" << std::string(35, '-') << "\n";
        auto sp = getPath(result, t);
        printPath(sp, 0, t, labels);
    }
}

int main() {
    runAllTests();
    runDemo();
    return 0;
}
