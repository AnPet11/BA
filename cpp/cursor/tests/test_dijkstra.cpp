#include "dijkstra.hpp"

#include <algorithm>
#include <gtest/gtest.h>
#include <vector>

namespace {

// Väikese graafi jaoks: kõikide tippude paaride lühimad kaugused (Dijkstra on samaväärne negatiivse kaaluta).
// Kasutab Bellman-Fordi stiili relaksatsioone kordusi n-1 (ei negatiivseid tsükleid).
void floyd_bellman_like(int n,
    const std::vector<std::vector<std::pair<int, long long>>>& adj,
    int source,
    std::vector<long long>& out) {
    const long long kInf = DijkstraResult::kInf;
    out.assign(static_cast<std::size_t>(n), kInf);
    if (n <= 0 || source < 0 || source >= n) {
        return;
    }
    out[static_cast<std::size_t>(source)] = 0;
    for (int it = 0; it < n - 1; ++it) {
        bool any = false;
        for (int u = 0; u < n; ++u) {
            for (const auto& e : adj[static_cast<std::size_t>(u)]) {
                const int v = e.first;
                const long long w = e.second;
                if (v < 0 || v >= n || w < 0) {
                    continue;
                }
                const long long& du = out[static_cast<std::size_t>(u)];
                if (du >= kInf) {
                    continue;
                }
                if (out[static_cast<std::size_t>(v)] > du + w) {
                    out[static_cast<std::size_t>(v)] = du + w;
                    any = true;
                }
            }
        }
        if (!any) {
            break;
        }
    }
}

}  // namespace

TEST(Dijkstra, SingleNode) {
    const int n = 1;
    std::vector<std::vector<std::pair<int, long long>>> adj(n);
    const auto r = dijkstra(n, 0, adj);
    EXPECT_EQ(r.dist[0], 0);
    EXPECT_EQ(r.parent[0], -1);
    EXPECT_EQ(reconstruct_path(r, 0, 0), std::vector<int>({0}));
}

TEST(Dijkstra, TwoNodesDirectEdge) {
    const int n = 2;
    std::vector<std::vector<std::pair<int, long long>>> adj(n);
    adj[0].push_back({1, 7});
    const auto r = dijkstra(n, 0, adj);
    EXPECT_EQ(r.dist[0], 0);
    EXPECT_EQ(r.dist[1], 7);
    EXPECT_EQ(r.parent[1], 0);
    EXPECT_EQ(reconstruct_path(r, 0, 1), (std::vector<int>{0, 1}));
}

TEST(Dijkstra, ShortestPathPrefersShorterRoute) {
    // 0 -> 1 (10), 0 -> 2 (1), 2 -> 1 (2) => 0-2-1 = 3
    const int n = 3;
    std::vector<std::vector<std::pair<int, long long>>> adj(n);
    adj[0].push_back({1, 10});
    adj[0].push_back({2, 1});
    adj[2].push_back({1, 2});
    const auto r = dijkstra(n, 0, adj);
    EXPECT_EQ(r.dist[1], 3);
    const auto path = reconstruct_path(r, 0, 1);
    EXPECT_EQ(path, (std::vector<int>{0, 2, 1}));
}

TEST(Dijkstra, Unreachable) {
    const int n = 3;
    std::vector<std::vector<std::pair<int, long long>>> adj(n);
    adj[0].push_back({1, 1});
    // 2 eraldi komponent
    const auto r = dijkstra(n, 0, adj);
    EXPECT_EQ(r.dist[0], 0);
    EXPECT_EQ(r.dist[1], 1);
    EXPECT_GE(r.dist[2], DijkstraResult::kInf);
    EXPECT_TRUE(reconstruct_path(r, 0, 2).empty());
}

TEST(Dijkstra, MultiEdgePickMinimum) {
    // Kaks rööbitist serva 0->1: vali väiksema kaaluga pärast relaksatsioone.
    const int n = 2;
    std::vector<std::vector<std::pair<int, long long>>> adj(n);
    adj[0].push_back({1, 9});
    adj[0].push_back({1, 3});
    const auto r = dijkstra(n, 0, adj);
    EXPECT_EQ(r.dist[1], 3);
}

TEST(Dijkstra, AgreesWithBruteOnSmallGraph) {
    // Juhuslik, kuid fikseeritud väike graaf; võrdlusalgoritm kinnitab.
    const int n = 5;
    std::vector<std::vector<std::pair<int, long long>>> adj(n);
    adj[0] = {{1, 2}, {2, 6}};
    adj[1] = {{2, 1}, {3, 3}};
    adj[2] = {{3, 1}};
    adj[3] = {{4, 1}};
    adj[4] = {};
    for (int s = 0; s < n; ++s) {
        const auto r = dijkstra(n, s, adj);
        std::vector<long long> expected;
        floyd_bellman_like(n, adj, s, expected);
        for (int t = 0; t < n; ++t) {
            const long long d = r.dist[static_cast<std::size_t>(t)];
            const long long e = expected[static_cast<std::size_t>(t)];
            // Ühtib "ei kättesaadav" puhul (mõlemad kInf) või väärtus
            if (d >= DijkstraResult::kInf) {
                EXPECT_GE(e, DijkstraResult::kInf) << "s=" << s << " t=" << t;
            } else {
                EXPECT_EQ(d, e) << "s=" << s << " t=" << t;
            }
        }
    }
}

TEST(Dijkstra, LargerGraphRandomLikeFixed) {
    // Suurem, kuid deterministlik "juhuslik" graaf: ~50 tippu, hajutatud servad.
    const int n = 50;
    const long long p = 1103515245;  // LCG
    std::vector<std::vector<std::pair<int, long long>>> adj(
        static_cast<std::size_t>(n));
    long long seed = 1;
    for (int u = 0; u < n; ++u) {
        for (int k = 0; k < 6; ++k) {
            seed = (seed * p + 12345) & 0x7fffffff;
            const int v = static_cast<int>(seed % n);
            seed = (seed * p + 12345) & 0x7fffffff;
            const long long w = 1 + (seed % 20);
            if (u != v) {
                adj[static_cast<std::size_t>(u)].push_back({v, w});
            }
        }
    }
    for (int s = 0; s < 8; ++s) {
        const auto r = dijkstra(n, s, adj);
        std::vector<long long> expected;
        floyd_bellman_like(n, adj, s, expected);
        for (int t = 0; t < n; ++t) {
            const long long d = r.dist[static_cast<std::size_t>(t)];
            const long long e = expected[static_cast<std::size_t>(t)];
            if (d >= DijkstraResult::kInf) {
                EXPECT_GE(e, DijkstraResult::kInf) << "s=" << s << " t=" << t;
            } else {
                EXPECT_EQ(d, e) << "s=" << s << " t=" << t;
            }
        }
    }
}

TEST(Dijkstra, PathReconstructionConsistent) {
    const int n = 4;
    std::vector<std::vector<std::pair<int, long long>>> adj(n);
    adj[0] = {{1, 1}, {2, 10}};
    adj[1] = {{2, 1}, {3, 1}};
    adj[2] = {{3, 1}};
    const int source = 0;
    const int target = 3;
    const auto r = dijkstra(n, source, adj);
    const auto path = reconstruct_path(r, source, target);
    ASSERT_EQ(path.size(), 4u);
    EXPECT_EQ(path[0], 0);
    EXPECT_EQ(path[3], 3);
    long long sum = 0;
    for (std::size_t i = 0; i + 1 < path.size(); ++i) {
        const int a = path[i];
        const int b = path[i + 1];
        long long w = DijkstraResult::kInf;
        for (const auto& e : adj[static_cast<std::size_t>(a)]) {
            if (e.first == b) {
                w = std::min(w, e.second);
            }
        }
        ASSERT_LT(w, DijkstraResult::kInf);
        sum += w;
    }
    EXPECT_EQ(sum, r.dist[static_cast<std::size_t>(target)]);
}

TEST(Dijkstra, InvalidSourceReturnsInfDistances) {
    const int n = 2;
    std::vector<std::vector<std::pair<int, long long>>> adj(n);
    adj[0] = {{1, 1}};
    const auto r = dijkstra(n, 5, adj);
    EXPECT_EQ(r.dist[0], DijkstraResult::kInf);
    EXPECT_EQ(r.dist[1], DijkstraResult::kInf);
}
