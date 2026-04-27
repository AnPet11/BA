#include "dijkstra.hpp"

#include <queue>
#include <utility>

DijkstraResult dijkstra(int n, int source,
    const std::vector<std::vector<std::pair<int, long long>>>& adj) {
    DijkstraResult r;
    r.dist.assign(n, DijkstraResult::kInf);
    r.parent.assign(n, -1);
    if (n <= 0 || source < 0 || source >= n) {
        return r;
    }
    r.dist[source] = 0;
    using Node = std::pair<long long, int>;
    std::priority_queue<Node, std::vector<Node>, std::greater<Node>> pq;
    pq.push({0, source});
    while (!pq.empty()) {
        const long long d = pq.top().first;
        const int u = pq.top().second;
        pq.pop();
        if (d != r.dist[u]) {
            continue;
        }
        for (const auto& edge : adj[static_cast<std::size_t>(u)]) {
            const int v = edge.first;
            const long long w = edge.second;
            if (v < 0 || v >= n || w < 0) {
                continue;
            }
            if (r.dist[v] > d + w) {
                r.dist[v] = d + w;
                r.parent[v] = u;
                pq.push({r.dist[v], v});
            }
        }
    }
    return r;
}

std::vector<int> reconstruct_path(const DijkstraResult& res, int source, int target) {
    std::vector<int> path;
    if (res.dist.empty() || target < 0
        || target >= static_cast<int>(res.dist.size())) {
        return path;
    }
    if (res.dist[static_cast<std::size_t>(target)] >= DijkstraResult::kInf) {
        return path;
    }
    for (int v = target; v != -1; v = res.parent[static_cast<std::size_t>(v)]) {
        path.push_back(v);
    }
    if (path.back() != source) {
        path.clear();
        return path;
    }
    for (std::size_t i = 0, j = path.size() - 1; i < j; ++i, --j) {
        std::swap(path[i], path[j]);
    }
    return path;
}
