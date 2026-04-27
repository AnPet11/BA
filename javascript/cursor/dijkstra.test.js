import assert from "node:assert/strict";
import test from "node:test";
import { dijkstraAll, shortestPath } from "./dijkstra.js";

test("lihtne kahe sõlme graaf", () => {
  const g = {
    A: [{ to: "B", weight: 5 }],
  };
  const r = shortestPath(g, "A", "B");
  assert.deepEqual(r, { path: ["A", "B"], distance: 5 });
});

test("klassikaline näide: lühem tee läbi vahesõlme", () => {
  const g = {
    A: [
      { to: "B", weight: 1 },
      { to: "C", weight: 4 },
    ],
    B: [{ to: "C", weight: 2 }],
    C: [],
  };
  assert.deepEqual(shortestPath(g, "A", "C"), { path: ["A", "B", "C"], distance: 3 });
});

test("sihtpole kättesaadav", () => {
  const g = {
    A: [{ to: "B", weight: 1 }],
    C: [{ to: "D", weight: 1 }],
  };
  assert.equal(shortestPath(g, "A", "D"), null);
});

test("lähtesõlm == sihtsõlm", () => {
  const g = { X: [] };
  assert.deepEqual(shortestPath(g, "X", "X"), { path: ["X"], distance: 0 });
});

test("paralleelsed servad: valitakse lühim", () => {
  const g = {
    A: [
      { to: "B", weight: 10 },
      { to: "B", weight: 3 },
    ],
    B: [],
  };
  assert.deepEqual(shortestPath(g, "A", "B"), { path: ["A", "B"], distance: 3 });
});

test("nullkaal on lubatud", () => {
  const g = {
    A: [{ to: "B", weight: 0 }],
    B: [{ to: "C", weight: 1 }],
  };
  assert.deepEqual(shortestPath(g, "A", "C"), { path: ["A", "B", "C"], distance: 1 });
});

test("negatiivne kaal viskab vea", () => {
  const g = {
    A: [{ to: "B", weight: -1 }],
  };
  assert.throws(() => shortestPath(g, "A", "B"), /mittenegatiivseid/);
});

test("pikk ahel (N sõlme): kaugus N-1", () => {
  const N = 100;
  const g = {};
  for (let i = 0; i < N - 1; i++) {
    const u = `v${i}`;
    const v = `v${i + 1}`;
    g[u] = [{ to: v, weight: 1 }];
  }
  g[`v${N - 1}`] = [];
  const r = shortestPath(g, "v0", `v${N - 1}`);
  assert.equal(r.distance, N - 1);
  assert.equal(r.path.length, N);
});

test("suurem võrk: teadaolev lühim tee (ruudustik)", () => {
  const rows = 15;
  const cols = 15;
  const id = (r, c) => `${r},${c}`;
  const g = {};

  for (let r = 0; r < rows; r++) {
    for (let c = 0; c < cols; c++) {
      const edges = [];
      if (c + 1 < cols) edges.push({ to: id(r, c + 1), weight: 1 });
      if (r + 1 < rows) edges.push({ to: id(r + 1, c), weight: 1 });
      g[id(r, c)] = edges;
    }
  }

  const start = id(0, 0);
  const end = id(rows - 1, cols - 1);
  const r = shortestPath(g, start, end);
  assert.equal(r.distance, (rows - 1) + (cols - 1));
  assert.equal(r.path[0], start);
  assert.equal(r.path[r.path.length - 1], end);
});

test("täielik mini-graaf: kõik kaalud 1, lühim on üks samm", () => {
  const g = {
    hub: [
      { to: "a", weight: 1 },
      { to: "b", weight: 1 },
      { to: "c", weight: 1 },
    ],
    a: [],
    b: [],
    c: [],
  };
  assert.deepEqual(shortestPath(g, "hub", "a"), { path: ["hub", "a"], distance: 1 });
});

test("dijkstraAll: kõik kaugused väiksel graafil", () => {
  const g = {
    S: [
      { to: "A", weight: 7 },
      { to: "B", weight: 2 },
    ],
    A: [{ to: "C", weight: 1 }],
    B: [
      { to: "A", weight: 3 },
      { to: "C", weight: 5 },
    ],
    C: [],
  };
  const { distances } = dijkstraAll(g, "S");
  assert.equal(distances.get("S"), 0);
  assert.equal(distances.get("A"), 5);
  assert.equal(distances.get("B"), 2);
  assert.equal(distances.get("C"), 6);
});

test("üksik sõlm graafis, teine puudub lähtest", () => {
  const g = { only: [] };
  const { distances } = dijkstraAll(g, "only");
  assert.equal(distances.get("only"), 0);
});
