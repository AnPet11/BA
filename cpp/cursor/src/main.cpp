#include "dijkstra.hpp"

#include <iostream>

int main() {
    // Näide: 4 tippu, suunatud servad
    // 0 -> 1 (4), 0 -> 2 (1), 2 -> 1 (2), 1 -> 3 (1)
    const int n = 4;
    const int source = 0;
    const int target = 3;
    std::vector<std::vector<std::pair<int, long long>>> adj(n);
    adj[0].push_back({1, 4});
    adj[0].push_back({2, 1});
    adj[2].push_back({1, 2});
    adj[1].push_back({3, 1});

    const DijkstraResult r = dijkstra(n, source, adj);
    std::cout << "Lühim kaugus " << source << " -> " << target << ": ";
    if (r.dist[static_cast<std::size_t>(target)] >= DijkstraResult::kInf) {
        std::cout << "puudub (INF)\n";
    } else {
        std::cout << r.dist[static_cast<std::size_t>(target)] << "\n";
        const std::vector<int> path = reconstruct_path(r, source, target);
        std::cout << "Tee: ";
        for (std::size_t i = 0; i < path.size(); ++i) {
            if (i) {
                std::cout << " -> ";
            }
            std::cout << path[i];
        }
        std::cout << "\n";
    }
    return 0;
}
