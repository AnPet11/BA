import java.util.*;

/**
 * ================================================================
 *  Dijkstra algoritm suunatud kaalutud graafis
 *
 *  Sisend : naabrusmaatriksina esitatud graaf + algsõlm
 *  Väljund: lühim tee iga sihtpunkti kohta ja selle kogupikkus
 *
 *  Ajakeerukus : O((V + E) * log V)  — MinHeap prioriteedijärjekorraga
 *  Ruumikeerukus: O(V)
 * ================================================================
 */
public class Dijkstra {

    // ── Konstandid ────────────────────────────────────────────────
    private static final int INF = Integer.MAX_VALUE;

    // ================================================================
    //  Andmestruktuurid
    // ================================================================

    /**
     * Algoritmi täielik väljund kõigi sihtpunktide kohta.
     */
    public static class DijkstraResult {
        public final int[] dist;   // kaugused algsõlmest
        public final int[] prev;   // eelkäijad tee rekonstrueerimiseks
        public final int   source; // algsõlm

        DijkstraResult(int[] dist, int[] prev, int source) {
            this.dist   = dist;
            this.prev   = prev;
            this.source = source;
        }
    }

    /**
     * Konkreetse sihtpunkti lühima tee kirjeldus.
     */
    public static class ShortestPath {
        public final List<Integer> path;   // sõlmede jada
        public final int           cost;   // kogupikkus (INF = tee puudub)
        public final boolean       exists; // kas tee eksisteerib

        ShortestPath(List<Integer> path, int cost, boolean exists) {
            this.path   = Collections.unmodifiableList(path);
            this.cost   = cost;
            this.exists = exists;
        }
    }

    // ================================================================
    //  Sisendi valideerimine
    // ================================================================

    /**
     * Kontrollib, et maatriks on ruudukujuline, sisaldab ainult
     * mittenegatiivseid kaale ning et sõlme indeks on vahemikus.
     *
     * @throws IllegalArgumentException  vigase maatriksi korral
     * @throws IndexOutOfBoundsException vigase sõlme indeksi korral
     */
    private static void validateInput(int[][] matrix, int node, String label) {
        if (matrix == null || matrix.length == 0)
            throw new IllegalArgumentException(
                "Viga: naabrusmaatriksit ei leitud (null või tühi).\n");

        int n = matrix.length;

        for (int i = 0; i < n; i++) {
            if (matrix[i] == null || matrix[i].length != n)
                throw new IllegalArgumentException(
                    "Viga: rida " + i + " pikkus on " +
                    (matrix[i] == null ? "null" : matrix[i].length) +
                    ", oodati " + n + ". Maatriks peab olema ruudukujuline.\n");

            for (int j = 0; j < n; j++) {
                if (matrix[i][j] < 0)
                    throw new IllegalArgumentException(
                        "Viga: negatiivne kaal [" + i + "][" + j + "] = " +
                        matrix[i][j] + ". Dijkstra ei toeta negatiivseid kaale.\n");
            }
        }

        if (node < 0 || node >= n)
            throw new IndexOutOfBoundsException(
                "Viga: " + label + " sõlm " + node +
                " on väljaspool vahemikku [0, " + (n - 1) + "].\n");
    }

    // ================================================================
    //  Dijkstra põhialgoritm
    // ================================================================

    /**
     * Käivitab Dijkstra algoritmi alates {@code source} sõlmest.
     * Arvutab lühimad kaugused kõigi sõlmedeni.
     *
     * @param matrix  naabrusmaatriksina esitatud suunatud kaalutud graaf
     *                (0 tähendab serva puudumist)
     * @param source  algsõlm (0-indekseeritud)
     * @return        {@link DijkstraResult} kauguste ja eelkäijatega
     * @throws IllegalArgumentException  vigase maatriksi korral
     * @throws IndexOutOfBoundsException vigase algsõlme korral
     */
    public static DijkstraResult compute(int[][] matrix, int source) {
        validateInput(matrix, source, "alg");

        int n    = matrix.length;
        int[] dist = new int[n];
        int[] prev = new int[n];

        Arrays.fill(dist, INF);
        Arrays.fill(prev, -1);
        dist[source] = 0;

        // Min-heap: [kaugus, sõlm]
        // Laisad kustutused — aegunud kirjed ignoreeritakse
        PriorityQueue<int[]> pq = new PriorityQueue<>(
            Comparator.comparingInt(e -> e[0])
        );
        pq.offer(new int[]{0, source});

        while (!pq.isEmpty()) {
            int[] top  = pq.poll();
            int   d    = top[0];
            int   u    = top[1];

            // Laisad kustutused: jäta aegunud kirje vahele
            if (d > dist[u]) continue;

            for (int v = 0; v < n; v++) {
                int w = matrix[u][v];
                if (w == 0) continue; // serv puudub

                // Ülevoolu kaitse enne liitmist
                if (dist[u] != INF && (long) dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    prev[v] = u;
                    pq.offer(new int[]{dist[v], v});
                }
            }
        }

        return new DijkstraResult(dist, prev, source);
    }

    // ================================================================
    //  Tee rekonstrueerimine
    // ================================================================

    /**
     * Rekonstrueerib lühima tee algsõlmest {@code target} sõlmeni,
     * kasutades {@link DijkstraResult#prev} massiivi.
     *
     * @param result  {@link #compute} väljund
     * @param target  sihtpunkti sõlm
     * @return        {@link ShortestPath} koos tee ja kuluga
     * @throws IndexOutOfBoundsException vigase sihtpunkti korral
     */
    public static ShortestPath getPath(DijkstraResult result, int target) {
        int n = result.dist.length;
        if (target < 0 || target >= n)
            throw new IndexOutOfBoundsException(
                "Viga: sihtpunkt " + target +
                " on väljaspool vahemikku [0, " + (n - 1) + "].\n");

        if (result.dist[target] == INF)
            return new ShortestPath(Collections.emptyList(), INF, false);

        // Rekonstrueeri tee eelkäijate kaudu
        LinkedList<Integer> path = new LinkedList<>();
        for (int cur = target; cur != -1; cur = result.prev[cur])
            path.addFirst(cur);

        return new ShortestPath(path, result.dist[target], true);
    }

    // ================================================================
    //  Väljundi abimeetodid
    // ================================================================

    /**
     * Prindib ühe lühima tee algsõlmest sihtpunkti.
     *
     * @param sp      {@link ShortestPath}
     * @param source  algsõlm
     * @param target  sihtpunkt
     * @param labels  valikulised sõlmede nimed (null → kasuta indekseid)
     */
    public static void printPath(ShortestPath sp,
                                  int source, int target,
                                  String[] labels) {
        System.out.println("Algsõlm   : " + name(source, labels));
        System.out.println("Sihtpunkt : " + name(target, labels));

        if (!sp.exists) {
            System.out.println("Tulemus   : tee PUUDUB (sõlmed pole ühendatud)");
            return;
        }

        StringJoiner sj = new StringJoiner(" → ");
        for (int node : sp.path) sj.add(name(node, labels));
        System.out.println("Tee       : " + sj);
        System.out.println("Kogupikkus: " + sp.cost);
    }

    /** Prindib kõik kaugused algsõlmest. */
    public static void printAllDistances(DijkstraResult result,
                                          String[] labels) {
        int n = result.dist.length;
        System.out.println("\nKõik kaugused algsõlmest "
                           + name(result.source, labels) + ":");
        System.out.println("-".repeat(36));
        for (int i = 0; i < n; i++) {
            String dist = result.dist[i] == INF
                ? "INF  (ligipääsmatu)"
                : String.valueOf(result.dist[i]);
            System.out.printf("  %-4s → %-4s : %s%n",
                name(result.source, labels), name(i, labels), dist);
        }
    }

    private static String name(int i, String[] labels) {
        return (labels != null && i < labels.length) ? labels[i] : String.valueOf(i);
    }
}