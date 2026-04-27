class GraphError extends Error {
  constructor(message) {
    super(message);
    this.name = "GraphError";
  }
}

/**
 * Validates adjacency matrix
 */
function validateMatrix(matrix) {
  if (!Array.isArray(matrix) || matrix.length === 0) {
    throw new GraphError("Matrix must be a non-empty 2D array.");
  }

  const n = matrix.length;

  for (let i = 0; i < n; i++) {
    if (!Array.isArray(matrix[i]) || matrix[i].length !== n) {
      throw new GraphError("Matrix must be square (NxN).");
    }

    for (let j = 0; j < n; j++) {
      const val = matrix[i][j];

      if (typeof val !== "number") {
        throw new GraphError("Matrix values must be numbers.");
      }

      if (val < 0) {
        throw new GraphError("Dijkstra does not support negative weights.");
      }
    }
  }
}

/**
 * Dijkstra algorithm using adjacency matrix
 * @param {number[][]} matrix
 * @param {number} start
 * @param {number} end
 */
function dijkstra(matrix, start, end) {
  validateMatrix(matrix);

  const n = matrix.length;

  if (
    !Number.isInteger(start) ||
    !Number.isInteger(end) ||
    start < 0 ||
    end < 0 ||
    start >= n ||
    end >= n
  ) {
    throw new GraphError("Invalid start or end node index.");
  }

  const distances = new Array(n).fill(Infinity);
  const visited = new Array(n).fill(false);
  const previous = new Array(n).fill(null);

  distances[start] = 0;

  for (let i = 0; i < n; i++) {
    let minDist = Infinity;
    let u = -1;

    for (let j = 0; j < n; j++) {
      if (!visited[j] && distances[j] < minDist) {
        minDist = distances[j];
        u = j;
      }
    }

    if (u === -1) break; // unreachable nodes remain

    visited[u] = true;

    for (let v = 0; v < n; v++) {
      const weight = matrix[u][v];

      if (weight > 0 && !visited[v]) {
        const alt = distances[u] + weight;

        if (alt < distances[v]) {
          distances[v] = alt;
          previous[v] = u;
        }
      }
    }
  }

  if (distances[end] === Infinity) {
    return {
      path: null,
      distance: Infinity,
      message: "No path exists between the given nodes."
    };
  }

  // reconstruct path
  const path = [];
  let current = end;

  while (current !== null) {
    path.unshift(current);
    current = previous[current];
  }

  return {
    path,
    distance: distances[end]
  };
}


const assert = require("assert");

// Test 1: simple graph
(function testSimpleGraph() {
  const graph = [
    [0, 2, 0, 1],
    [0, 0, 3, 0],
    [0, 0, 0, 4],
    [0, 1, 0, 0]
  ];

  const result = dijkstra(graph, 0, 2);

  assert.deepStrictEqual(result.path, [0, 1, 2]);
  assert.strictEqual(result.distance, 5);
})();

// Test 2: no path
(function testNoPath() {
  const graph = [
    [0, 1, 0],
    [0, 0, 0],
    [0, 0, 0]
  ];

  const result = dijkstra(graph, 0, 2);

  assert.strictEqual(result.path, null);
  assert.strictEqual(result.distance, Infinity);
})();

// Test 3: larger graph
(function testLargerGraph() {
  const graph = [
    [0, 4, 0, 0, 0, 0],
    [0, 0, 8, 0, 0, 0],
    [0, 0, 0, 7, 0, 4],
    [0, 0, 0, 0, 9, 14],
    [0, 0, 0, 0, 0, 10],
    [0, 0, 0, 0, 0, 0]
  ];

  const result = dijkstra(graph, 0, 5);

  assert.deepStrictEqual(result.path, [0, 1, 2, 5]);
  assert.strictEqual(result.distance, 16);
})();

// Test 4: invalid matrix
(function testInvalidMatrix() {
  try {
    dijkstra([[1, 2], [3]], 0, 1);
    assert.fail("Should throw error");
  } catch (e) {
    assert.ok(e instanceof GraphError);
  }
})();

// Test 5: negative weights
(function testNegativeWeights() {
  try {
    dijkstra([[0, -1], [0, 0]], 0, 1);
    assert.fail("Should throw error");
  } catch (e) {
    assert.ok(e instanceof GraphError);
  }
})();

console.log("All tests passed!");
