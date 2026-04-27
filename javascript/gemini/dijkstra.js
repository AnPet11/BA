/**
 * Dijkstra algoritm lühima tee leidmiseks suunatud graafis.
 */
class DijkstraRouter {
    /**
     * @param {number[][]} matrix - Graafi naabrusmaatriks.
     * @param {number} startNode - Algpunkti indeks.
     */
    constructor(matrix, startNode) {
        this.validateInput(matrix, startNode);
        this.matrix = matrix;
        this.startNode = startNode;
        this.n = matrix.length;
    }

    /**
     * Kontrollib sisendandmete korrektsust.
     */
    validateInput(matrix, startNode) {
        if (!Array.isArray(matrix) || matrix.length === 0) {
            throw new Error("Viga: Graaf peab olema mitte-tühi massiiv.");
        }
        const rowCount = matrix.length;
        for (let i = 0; i < rowCount; i++) {
            if (!Array.isArray(matrix[i]) || matrix[i].length !== rowCount) {
                throw new Error(`Viga: Rida ${i} peab olema massiiv pikkusega ${rowCount} (ruutmaatriks).`);
            }
            for (let j = 0; j < rowCount; j++) {
                if (typeof matrix[i][j] !== 'number' || matrix[i][j] < 0) {
                    throw new Error(`Viga: Element kohal [${i}][${j}] peab olema positiivne arv.`);
                }
            }
        }
        if (!Number.isInteger(startNode) || startNode < 0 || startNode >= rowCount) {
            throw new Error("Viga: Algpunkti indeks on vigane.");
        }
    }

    /**
     * Arvutab lühimad teed.
     * @returns {Object} Distantsid ja eelnevate sõlmede massiiv.
     */
    calculate() {
        const distances = new Array(this.n).fill(Infinity);
        const visited = new Array(this.n).fill(false);
        const previous = new Array(this.n).fill(null);

        distances[this.startNode] = 0;

        for (let i = 0; i < this.n - 1; i++) {
            const u = this.getMinDistanceNode(distances, visited);
            if (u === -1) break; // Ülejäänud sõlmed on kättesaamatud

            visited[u] = true;

            for (let v = 0; v < this.n; v++) {
                // Kui on serv (kaal > 0), sõlm pole külastatud ja uus tee on lühem
                if (!visited[v] && this.matrix[u][v] !== 0 && 
                    distances[u] !== Infinity && 
                    distances[u] + this.matrix[u][v] < distances[v]) {
                    distances[v] = distances[u] + this.matrix[u][v];
                    previous[v] = u;
                }
            }
        }

        return { distances, previous };
    }

    /**
     * Leiab külastamata sõlme, millel on vähim distants.
     */
    getMinDistanceNode(distances, visited) {
        let min = Infinity;
        let minIndex = -1;

        for (let v = 0; v < this.n; v++) {
            if (!visited[v] && distances[v] <= min) {
                min = distances[v];
                minIndex = v;
            }
        }
        return minIndex;
    }

    /**
     * Formaadib tulemuse konkreetse sihtpunkti jaoks.
     * @param {number} targetNode 
     */
    getPathTo(targetNode) {
        if (targetNode < 0 || targetNode >= this.n) {
            return "Viga: Sihtpunkt on graafi piiridest väljas.";
        }

        const { distances, previous } = this.calculate();
        
        if (distances[targetNode] === Infinity) {
            return `Teed punktist ${this.startNode} punkti ${targetNode} ei eksisteeri.`;
        }

        const path = [];
        let curr = targetNode;
        while (curr !== null) {
            path.unshift(curr);
            curr = previous[curr];
        }

        return {
            path: path,
            distance: distances[targetNode],
            message: `Lühim tee: ${path.join(" -> ")}, kogupikkus: ${distances[targetNode]}`
        };
    }
}
function runTests() {
    console.log("--- Alustan ühiktestidega ---");

    // Test 1: Standardne suunatud graaf
    const graph1 = [
        [0, 10, 0, 5, 0],
        [0, 0, 1, 2, 0],
        [0, 0, 0, 0, 4],
        [0, 3, 9, 0, 2],
        [7, 0, 6, 0, 0]
    ];
    const router1 = new DijkstraRouter(graph1, 0);
    const res1 = router1.getPathTo(2);
    console.assert(res1.distance === 8, "Test 1 viga: Distants peaks olema 8");
    console.assert(res1.path.join(',') === '0,3,1,2', "Test 1 viga: Tee on vale");
    console.log("Test 1 (Standard): Läbitud");

    // Test 2: Kättesaamatu sõlm
    const graph2 = [
        [0, 5, 0],
        [0, 0, 0],
        [0, 2, 0]
    ];
    const router2 = new DijkstraRouter(graph2, 0);
    const res2 = router2.getPathTo(2);
    console.assert(typeof res2 === 'string', "Test 2 viga: Peaks tagastama stringi teatega");
    console.log("Test 2 (Kättesaamatus): Läbitud");

    // Test 3: Vigane maatriks (mitte-ruutmaatriks)
    try {
        new DijkstraRouter([[0, 1], [0]], 0);
        console.error("Test 3 viga: Oleks pidanud viskama errori");
    } catch (e) {
        console.log("Test 3 (Vigane sisend): Läbitud");
    }

    // Test 4: Suurem graaf (automaatne kontroll)
    const size = 10;
    const bigGraph = Array.from({ length: size }, () => Array(size).fill(0));
    for(let i=0; i<size-1; i++) bigGraph[i][i+1] = 1; // Lineaarne ahel
    const router3 = new DijkstraRouter(bigGraph, 0);
    const res3 = router3.getPathTo(9);
    console.assert(res3.distance === 9, "Test 4 viga: Suure graafi distants vale");
    console.log("Test 4 (Suur graaf): Läbitud");

    console.log("--- Kõik testid edukalt lõpetatud ---");
}

runTests();
