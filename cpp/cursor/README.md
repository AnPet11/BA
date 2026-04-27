# Dijkstra shortest paths (directed graph)

This project implements **Dijkstra’s algorithm** for single-source shortest paths in a **directed graph** with **non-negative** edge weights. It includes a small demo program and **unit tests** (GoogleTest) that check correctness on small graphs and on a larger fixed synthetic graph.

## What was implemented

- **`dijkstra(n, source, adj)`** — returns shortest distances from `source` and a `parent` array for path reconstruction.
- **`reconstruct_path(result, source, target)`** — rebuilds one shortest path as a list of vertex indices (empty if `target` is unreachable).
- **Graph representation** — adjacency list: `adj[u]` is a list of pairs `(v, weight)` for edges `u → v`.
- **Unit tests** — trivial cases, unreachable vertices, multiple edges between the same pair, comparison against a reference relaxation algorithm on small graphs, a ~50-vertex deterministic “random-like” graph, path–distance consistency, and invalid source handling.

## Requirements

- **CMake** 3.16 or newer  
- A **C++17** compiler (MSVC, Clang, or GCC)  
- **Internet** on the first configure step (CMake downloads Google Test 1.14.0 via `FetchContent`)

## Project layout

| Path | Description |
|------|-------------|
| `include/dijkstra.hpp` | Public API |
| `src/dijkstra.cpp` | Algorithm implementation |
| `src/main.cpp` | Demo executable |
| `tests/test_dijkstra.cpp` | Unit tests |
| `CMakeLists.txt` | Build configuration |

## How to build

From the project root:

```bash
cmake -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build
```

On Windows with Visual Studio (multi-config generator):

```powershell
cmake -B build
cmake --build build --config Release
```

## How to run

### Demo program

After a successful build, run the `dijkstra_demo` executable from the build directory (exact path depends on your generator):

- **Single-config** (Ninja, Unix Makefiles): `build/dijkstra_demo`
- **Visual Studio multi-config**: `build/Release/dijkstra_demo` or `build/Debug/dijkstra_demo`

It prints the shortest distance and path from vertex `0` to `3` for a built-in example graph.

### Unit tests

From the project root:

```bash
ctest --test-dir build --output-on-failure
```

With Visual Studio multi-config, you may need:

```powershell
ctest --test-dir build -C Release --output-on-failure
```

Alternatively, run the `dijkstra_tests` binary directly from the corresponding build output folder.

## Notes

- Edge weights must be **≥ 0**; negative weights are not valid for Dijkstra and are skipped if present in the adjacency list.
- Vertices are numbered **`0` … `n - 1`**.
