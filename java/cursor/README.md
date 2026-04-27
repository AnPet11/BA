# Dijkstra (adjacency matrix) — Java

## Input

Whitespace-separated tokens (lines are not significant; `#` comments allowed):

1. `n` — number of vertices
2. `n*n` matrix values (long)
   - `-1` means "no edge"
   - any other value must be `>= 0` (Dijkstra requirement)
3. `start target` — vertex indices (auto-detected as 0-based or 1-based)

## Output

- If reachable:
  - `PATH` then the path like `0 -> 1 -> 2`
  - `TOTAL_LENGTH` then the total length
- If unreachable:
  - `NO PATH` and a short explanation

## Run

```bash
mvn -q test
```

Example run:

```bash
printf "3\n0 3 10\n-1 0 4\n-1 -1 0\n0 2\n" | mvn -q -DskipTests exec:java
```

Tip: you can run the main class from your IDE: `ee.tlu.ba.dijkstra.DijkstraApp`.

