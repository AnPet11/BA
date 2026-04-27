# Dijkstra algoritm — lühim tee suunatud graafis

## Mis on tehtud

- **`dijkstra.js`** — JavaScripti implementatsioon **Dijkstra algoritmist** suunatud kaalutud graafile. Kasutatakse **min-kuhi** prioriteetjärjekorda; keerukus on ligikaudu **O((V + E) log V)** (V = sõlmede arv, E = servade arv).
- Eksporditud funktsioonid:
  - **`dijkstraAll(graph, source)`** — arvutab lühimad kaugused lähtesõlmest kõikidele sõlmedele (`distances`, `previous`).
  - **`shortestPath(graph, source, target)`** — tagastab ühe konkreetse sihtkoha jaoks tee ja kogukaalu, või **`null`**, kui sihtpole kättesaadav.
- Graaf on kujul: `sõlm → [{ to: 'teine_sõlm', weight: number }]`. Kaalud peavad olema **mittenegatiivsed** (0 on lubatud).
- **`dijkstra.test.js`** — ühiktestid (`node:test`): väikesed näited, kättesaamatus, pikem ahel (100 sõlme), 15×15 ruudustik jms.

## Nõuded

- **Node.js 18** või uuem (sisseehitatud `node --test`).

## Kuidas käivitada

Projekti kaustas:

```bash
npm test
```

See käivitab kõik testid failis `dijkstra.test.js`.

Otse Node’iga (sama tulemus):

```bash
node --test
```

## Näide

```js
import { shortestPath } from "./dijkstra.js";

const graph = {
  A: [
    { to: "B", weight: 1 },
    { to: "C", weight: 4 },
  ],
  B: [{ to: "C", weight: 2 }],
  C: [],
};

console.log(shortestPath(graph, "A", "C"));
// { path: ['A', 'B', 'C'], distance: 3 }
```

**Märkus:** negatiivsete servakaalude korral ei kehti Dijkstra garantii; sellisel juhul sobivad näiteks Bellman-Ford või Floyd-Warshall.
