/**
 * Dijkstra algoritm suunatud graafis
 * Sisend: naabrusmaatriksina esitatud graaf
 * Väljund: lühim tee ja selle kogupikkus
 */

// ── Andmestruktuurid ───────────────────────────────────────────────
class MinHeap {
  constructor() { this.heap = []; }

  push(item) {
    this.heap.push(item);
    this.bubbleUp(this.heap.length - 1);
  }

  pop() {
    if (this.heap.length === 0) return null;
    const top = this.heap[0];
    const last = this.heap.pop();
    if (this.heap.length > 0) {
      this.heap[0] = last;
      this.sinkDown(0);
    }
    return top;
  }

  bubbleUp(i) {
    while (i > 0) {
      const parent = Math.floor((i - 1) / 2);
      if (this.heap[parent].dist <= this.heap[i].dist) break;
      [this.heap[parent], this.heap[i]] = [this.heap[i], this.heap[parent]];
      i = parent;
    }
  }

  sinkDown(i) {
    const n = this.heap.length;
    while (true) {
      let min = i;
      const l = 2 * i + 1, r = 2 * i + 2;
      if (l < n && this.heap[l].dist < this.heap[min].dist) min = l;
      if (r < n && this.heap[r].dist < this.heap[min].dist) min = r;
      if (min === i) break;
      [this.heap[min], this.heap[i]] = [this.heap[i], this.heap[min]];
      i = min;
    }
  }

  get size() { return this.heap.length; }
}

// ── Sisendi kontrollimine ──────────────────────────────────────────
function validateInput(matrix, start, end) {
  if (!Array.isArray(matrix) || matrix.length === 0)
    throw new Error("Maatriksit ei leitud või on tühi.");
  const n = matrix.length;
  for (const row of matrix) {
    if (!Array.isArray(row) || row.length !== n)
      throw new Error("Maatriks ei ole ruutmaatriks.");
    for (const v of row)
      if (typeof v !== "number" || v < 0)
        throw new Error("Kaalud peavad olema mittenegatiivsed arvud.");
  }
  if (!Number.isInteger(start) || start < 0 || start >= n)
    throw new Error(`Algsõlm ${start} on väljaspool vahemikku [0, ${n-1}].`);
  if (!Number.isInteger(end) || end < 0 || end >= n)
    throw new Error(`Sihtpunkt ${end} on väljaspool vahemikku [0, ${n-1}].`);
}

// ── Põhialgoritm ──────────────────────────────────────────────────
function dijkstra(matrix, start, end) {
  validateInput(matrix, start, end);
  const n = matrix.length;
  const dist = new Array(n).fill(Infinity);
  const prev = new Array(n).fill(null);
  const visited = new Set();
  const pq = new MinHeap();

  dist[start] = 0;
  pq.push({ node: start, dist: 0 });

  while (pq.size > 0) {
    const { node: u } = pq.pop();
    if (visited.has(u)) continue;
    visited.add(u);
    if (u === end) break;

    for (let v = 0; v < n; v++) {
      if (matrix[u][v] > 0 && !visited.has(v)) {
        const alt = dist[u] + matrix[u][v];
        if (alt < dist[v]) {
          dist[v] = alt;
          prev[v] = u;
          pq.push({ node: v, dist: alt });
        }
      }
    }
  }

  if (dist[end] === Infinity)
    return { path: null, cost: Infinity, error: `Tee puudub: sõlmedest ${start} → ${end}` };

  const path = [];
  let cur = end;
  while (cur !== null) { path.unshift(cur); cur = prev[cur]; }

  return { path, cost: dist[end], error: null };
}
