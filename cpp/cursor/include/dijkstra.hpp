#ifndef DIJKSTRA_HPP
#define DIJKSTRA_HPP

#include <limits>
#include <utility>
#include <vector>

// Suunatud graaf: n tippu, nummerdus 0..n-1;
// adj[u] = naabrid (v, kaal), kaal >= 0.
struct DijkstraResult {
    static constexpr long long kInf = std::numeric_limits<long long>::max() / 4;

    std::vector<long long> dist;   // lühim kaugus lähte tipust
    std::vector<int> parent;        // eelkäija tee taastamiseks, -1 puudub
};

DijkstraResult dijkstra(int n, int source,
    const std::vector<std::vector<std::pair<int, long long>>>& adj);

// Taastab tee source -> target (k.a. otsed tipud); tühi kui targeti pole.
std::vector<int> reconstruct_path(const DijkstraResult& res, int source, int target);

#endif
