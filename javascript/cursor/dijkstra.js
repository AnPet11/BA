/**
 * Suunatud kaalutud graaf: sõlm -> [{ to, weight }].
 * Kaalud peavad olema mittenegatiivsed.
 */

class MinHeap {
  constructor(compare = (a, b) => a[0] - b[0]) {
    this._heap = [];
    this._compare = compare;
  }

  get size() {
    return this._heap.length;
  }

  push(item) {
    this._heap.push(item);
    this._siftUp(this._heap.length - 1);
  }

  pop() {
    if (this._heap.length === 0) return undefined;
    const top = this._heap[0];
    const last = this._heap.pop();
    if (this._heap.length > 0 && last !== undefined) {
      this._heap[0] = last;
      this._siftDown(0);
    }
    return top;
  }

  _parent(i) {
    return (i - 1) >> 1;
  }

  _left(i) {
    return (i << 1) + 1;
  }

  _right(i) {
    return (i << 1) + 2;
  }

  _siftUp(i) {
    const heap = this._heap;
    const item = heap[i];
    while (i > 0) {
      const p = this._parent(i);
      if (this._compare(heap[p], item) <= 0) break;
      heap[i] = heap[p];
      i = p;
    }
    heap[i] = item;
  }

  _siftDown(i) {
    const heap = this._heap;
    const length = heap.length;
    const item = heap[i];
    while (true) {
      const l = this._left(i);
      const r = this._right(i);
      let smallest = i;
      if (l < length && this._compare(heap[l], heap[smallest]) < 0) smallest = l;
      if (r < length && this._compare(heap[r], heap[smallest]) < 0) smallest = r;
      if (smallest === i) break;
      heap[i] = heap[smallest];
      i = smallest;
    }
    heap[i] = item;
  }
}

function collectNodes(graph) {
  const nodes = new Set();
  for (const [from, edges] of Object.entries(graph)) {
    nodes.add(from);
    for (const e of edges) {
      nodes.add(e.to);
    }
  }
  return nodes;
}

/**
 * Dijkstra algoritm: lühimad kaugused lähtesõlmest kõikidele teistele.
 * @param {Record<string, Array<{ to: string, weight: number }>>} graph
 * @param {string} source
 * @returns {{ distances: Map<string, number>, previous: Map<string, string | null> }}
 */
export function dijkstraAll(graph, source) {
  const nodes = collectNodes(graph);
  const distances = new Map();
  const previous = new Map();

  for (const n of nodes) {
    distances.set(n, Infinity);
    previous.set(n, null);
  }
  if (!nodes.has(source)) {
    return { distances, previous };
  }
  distances.set(source, 0);

  const heap = new MinHeap((a, b) => a[0] - b[0]);
  heap.push([0, source]);
  const visited = new Set();

  while (heap.size > 0) {
    const pair = heap.pop();
    if (pair === undefined) break;
    const [d, u] = pair;
    if (visited.has(u)) continue;
    visited.add(u);
    if (d !== distances.get(u)) continue;

    const edges = graph[u] ?? [];
    for (const { to, weight } of edges) {
      if (weight < 0) {
        throw new Error("Dijkstra nõuab mittenegatiivseid kaalusid");
      }
      const alt = d + weight;
      if (alt < (distances.get(to) ?? Infinity)) {
        distances.set(to, alt);
        previous.set(to, u);
        heap.push([alt, to]);
      }
    }
  }

  return { distances, previous };
}

/**
 * Taastab lühima tee lähtest sihtkohta.
 * @returns {{ path: string[], distance: number } | null} null kui sihtpole kättesaadav
 */
export function shortestPath(graph, source, target) {
  const { distances, previous } = dijkstraAll(graph, source);
  if (!distances.has(target) || distances.get(target) === Infinity) {
    return null;
  }

  const path = [];
  let cur = target;
  while (cur !== null) {
    path.push(cur);
    cur = previous.get(cur) ?? null;
  }
  path.reverse();
  return { path, distance: distances.get(target) };
}
