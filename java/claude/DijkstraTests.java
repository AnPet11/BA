/**
 * ================================================================
 *  Dijkstra algoritmuse testid ja demo
 * ================================================================
 */
public class DijkstraTests {

    // ================================================================
    //  Lihtne ühiktestide raamistik
    // ================================================================

    private static class TestSuite {
        private int pass = 0, fail = 0;
        private final java.util.List<String> failures = new java.util.ArrayList<>();

        void run(String name, Runnable test) {
            try {
                test.run();
                System.out.println("  ✓  LÄBIS  " + name);
                pass++;
            } catch (AssertionError | RuntimeException e) {
                System.out.println("  ✗  KUKUS  " + name);
                System.out.println("             → " + e.getMessage());
                failures.add(name + ": " + e.getMessage());
                fail++;
            }
        }

        void summary() {
            System.out.println("\n" + "-".repeat(50));
            System.out.printf("Tulemus: %d/%d testi läbis%n", pass, pass + fail);
            if (fail > 0) {
                System.out.println("Ebaõnnestunud testid:");
                failures.forEach(f -> System.out.println("  • " + f));
            }
            System.out.println("=".repeat(50));
        }

        // Abimeetodid
        static void assertEquals(int actual, int expected, String msg) {
            if (actual != expected)
                throw new AssertionError(msg + ": oodati " + expected
                                         + ", saadi " + actual);
        }
        static void assertTrue(boolean cond, String msg) {
            if (!cond) throw new AssertionError(msg);
        }
        static void assertThrows(Class<? extends Throwable> type,
                                  Runnable action, String msg) {
            try { action.run(); throw new AssertionError(msg + ": erind puudus"); }
            catch (Throwable t) {
                if (!type.isInstance(t))
                    throw new AssertionError(msg + ": oodati " + type.getSimpleName()
                                             + ", saadi " + t.getClass().getSimpleName());
            }
        }
    }

    // ================================================================
    //  ÜHIKTESTID
    // ================================================================

    private static void runAllTests() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  ÜHIKTESTID");
        System.out.println("=".repeat(50) + "\n");

        TestSuite t = new TestSuite();

        // ── Test 1: lihtne 5-sõlmeline graaf ──────────────────────
        t.run("Lihtne graaf A→E, kulu 6", () -> {
            int[][] m = {
                {0, 4, 2,  0, 0},
                {4, 0, 5, 10, 0},
                {2, 5, 0,  3, 8},
                {0,10, 3,  0, 1},
                {0, 0, 8,  1, 0}
            };
            Dijkstra.DijkstraResult r = Dijkstra.compute(m, 0);
            Dijkstra.ShortestPath   sp = Dijkstra.getPath(r, 4);
            // Lühim tee: A→C→D→E = 2+3+1 = 6
            TestSuite.assertTrue(sp.exists, "Tee peaks eksisteerima");
            TestSuite.assertEquals(sp.cost, 6, "kulu");
            TestSuite.assertTrue(sp.path.get(0) == 0
                && sp.path.get(sp.path.size() - 1) == 4, "Tee otspunktid");
        });

        // ── Test 2: kaudtee lühem kui otsetee ─────────────────────
        t.run("Kaudtee lühem kui otsetee", () -> {
            int[][] m = {
                {0, 10, 1, 0},
                {0,  0, 0, 1},
                {0,  1, 0, 5},
                {0,  0, 0, 0}
            };
            Dijkstra.ShortestPath sp = Dijkstra.getPath(Dijkstra.compute(m, 0), 3);
            // Kaudtee 0→2→1→3 = 1+1+1 = 3 vs otse 0→1→3 = 11
            TestSuite.assertTrue(sp.exists, "Tee peaks eksisteerima");
            TestSuite.assertEquals(sp.cost, 3, "kulu");
            TestSuite.assertEquals(sp.path.size(), 4, "sammu arv");
        });

        // ── Test 3: lahutatud graaf ────────────────────────────────
        t.run("Lahutatud graaf: tee puudub", () -> {
            int[][] m = {
                {0, 1, 0, 0},
                {1, 0, 0, 0},
                {0, 0, 0, 1},
                {0, 0, 1, 0}
            };
            Dijkstra.ShortestPath sp = Dijkstra.getPath(Dijkstra.compute(m, 0), 3);
            TestSuite.assertTrue(!sp.exists, "Tee ei peaks eksisteerima");
            TestSuite.assertEquals(sp.cost, Integer.MAX_VALUE, "kulu peaks olema INF");
            TestSuite.assertTrue(sp.path.isEmpty(), "Tee peaks olema tühi");
        });

        // ── Test 4: algsõlm === sihtpunkt ─────────────────────────
        t.run("Algsõlm === sihtpunkt, kulu 0", () -> {
            int[][] m = {{0, 5}, {5, 0}};
            Dijkstra.ShortestPath sp = Dijkstra.getPath(Dijkstra.compute(m, 0), 0);
            TestSuite.assertTrue(sp.exists, "Tee peaks eksisteerima");
            TestSuite.assertEquals(sp.cost, 0, "kulu");
            TestSuite.assertEquals(sp.path.size(), 1, "Ainult üks sõlm");
            TestSuite.assertEquals((int) sp.path.get(0), 0, "Sõlm on algsõlm");
        });

        // ── Test 5: ühe sõlmega graaf ─────────────────────────────
        t.run("Ühe sõlmega graaf", () -> {
            int[][] m = {{0}};
            Dijkstra.ShortestPath sp = Dijkstra.getPath(Dijkstra.compute(m, 0), 0);
            TestSuite.assertTrue(sp.exists, "Tee peaks eksisteerima");
            TestSuite.assertEquals(sp.cost, 0, "kulu");
        });

        // ── Test 6: lineaarne ahel ────────────────────────────────
        t.run("Lineaarne ahel A→B→C→D→E, kulu 11", () -> {
            int[][] m = {
                {0, 3, 0, 0, 0},
                {0, 0, 2, 0, 0},
                {0, 0, 0, 5, 0},
                {0, 0, 0, 0, 1},
                {0, 0, 0, 0, 0}
            };
            Dijkstra.ShortestPath sp = Dijkstra.getPath(Dijkstra.compute(m, 0), 4);
            TestSuite.assertTrue(sp.exists, "Tee peaks eksisteerima");
            TestSuite.assertEquals(sp.cost, 11, "kulu");
            TestSuite.assertEquals(sp.path.size(), 5, "5 sõlme");
        });

        // ── Test 7: 7-sõlmeline graaf ─────────────────────────────
        t.run("7-sõlmeline graaf A→E, kulu 20", () -> {
            int[][] m = {
                { 0,  7,  9,  0,  0, 14,  0},
                { 7,  0, 10, 15,  0,  0,  0},
                { 9, 10,  0, 11,  0,  2,  0},
                { 0, 15, 11,  0,  6,  0,  0},
                { 0,  0,  0,  6,  0,  9,  0},
                {14,  0,  2,  0,  9,  0,  0},
                { 0,  0,  0,  0,  0,  0,  0}
            };
            Dijkstra.ShortestPath sp = Dijkstra.getPath(Dijkstra.compute(m, 0), 4);
            // Lühim tee: A→C→F→E = 9+2+9 = 20
            TestSuite.assertTrue(sp.exists, "Tee peaks eksisteerima");
            TestSuite.assertEquals(sp.cost, 20, "kulu");
        });

        // ── Test 8: kõigi kauguste õigsus ─────────────────────────
        t.run("Kõik kaugused 5-sõlmelises graafis", () -> {
            int[][] m = {
                {0, 4, 2,  0, 0},
                {4, 0, 5, 10, 0},
                {2, 5, 0,  3, 8},
                {0,10, 3,  0, 1},
                {0, 0, 8,  1, 0}
            };
            Dijkstra.DijkstraResult r = Dijkstra.compute(m, 0);
            TestSuite.assertEquals(r.dist[0], 0, "A→A");
            TestSuite.assertEquals(r.dist[1], 4, "A→B");
            TestSuite.assertEquals(r.dist[2], 2, "A→C");
            TestSuite.assertEquals(r.dist[3], 5, "A→D");
            TestSuite.assertEquals(r.dist[4], 6, "A→E");
        });

        // ── Test 9: suunatud graaf (ühesuunaline serv) ────────────
        t.run("Suunatud serv: vastassuunas tee puudub", () -> {
            int[][] m = {
                {0, 5, 0},
                {0, 0, 3},
                {0, 0, 0}
            };
            // 0→2 = 8, aga 2→0 = puudub
            Dijkstra.ShortestPath sp1 = Dijkstra.getPath(Dijkstra.compute(m, 0), 2);
            TestSuite.assertTrue(sp1.exists, "0→2 peaks eksisteerima");
            TestSuite.assertEquals(sp1.cost, 8, "0→2 kulu");

            Dijkstra.ShortestPath sp2 = Dijkstra.getPath(Dijkstra.compute(m, 2), 0);
            TestSuite.assertTrue(!sp2.exists, "2→0 ei peaks eksisteerima");
        });

        // ── Test 10: suurem graaf (10 sõlme) ──────────────────────
        t.run("10-sõlmeline graaf, kulu kontroll", () -> {
            // Ring + otsetee: 0→1→2→...→9 (kaalud 1) vs otsetee 0→9 (kaal 100)
            int[][] m = new int[10][10];
            for (int i = 0; i < 9; i++) m[i][i + 1] = 1;
            m[0][9] = 100; // kallis otsetee
            Dijkstra.ShortestPath sp = Dijkstra.getPath(Dijkstra.compute(m, 0), 9);
            TestSuite.assertTrue(sp.exists, "Tee peaks eksisteerima");
            TestSuite.assertEquals(sp.cost, 9, "Aheltee kulu 9 < otsetee 100");
            TestSuite.assertEquals(sp.path.size(), 10, "10 sõlme");
        });

        // ── Test 11: vigane sisend — null maatriks ─────────────────
        t.run("Veakäsitlus: null maatriks", () ->
            TestSuite.assertThrows(IllegalArgumentException.class,
                () -> Dijkstra.compute(null, 0), "Null maatriks"));

        // ── Test 12: vigane sisend — tühi maatriks ────────────────
        t.run("Veakäsitlus: tühi maatriks", () ->
            TestSuite.assertThrows(IllegalArgumentException.class,
                () -> Dijkstra.compute(new int[0][0], 0), "Tühi maatriks"));

        // ── Test 13: vigane sisend — negatiivne kaal ──────────────
        t.run("Veakäsitlus: negatiivne kaal", () ->
            TestSuite.assertThrows(IllegalArgumentException.class,
                () -> Dijkstra.compute(new int[][]{{0, -3}, {-3, 0}}, 0),
                "Negatiivne kaal"));

        // ── Test 14: vigane sisend — algsõlm väljaspool vahemikku ─
        t.run("Veakäsitlus: algsõlm väljaspool vahemikku", () ->
            TestSuite.assertThrows(IndexOutOfBoundsException.class,
                () -> Dijkstra.compute(new int[][]{{0, 1}, {1, 0}}, 5),
                "Algsõlm väljaspool"));

        // ── Test 15: vigane sisend — sihtpunkt väljaspool vahemikku
        t.run("Veakäsitlus: sihtpunkt väljaspool vahemikku", () -> {
            Dijkstra.DijkstraResult r = Dijkstra.compute(new int[][]{{0, 1}, {1, 0}}, 0);
            TestSuite.assertThrows(IndexOutOfBoundsException.class,
                () -> Dijkstra.getPath(r, 99), "Sihtpunkt väljaspool");
        });

        // ── Test 16: vigane sisend — mitte-ruutmaatriks ───────────
        t.run("Veakäsitlus: mitte-ruutmaatriks", () ->
            TestSuite.assertThrows(IllegalArgumentException.class,
                () -> Dijkstra.compute(new int[][]{{0, 1, 2}, {1, 0}}, 0),
                "Mitte-ruutmaatriks"));

        t.summary();
    }

    // ================================================================
    //  DEMO
    // ================================================================

    private static void runDemo() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  DEMO — 7-sõlmeline graaf");
        System.out.println("=".repeat(50));

        String[] labels = {"A", "B", "C", "D", "E", "F", "G"};
        int[][] m = {
            { 0,  7,  9,  0,  0, 14,  0},
            { 7,  0, 10, 15,  0,  0,  0},
            { 9, 10,  0, 11,  0,  2,  0},
            { 0, 15, 11,  0,  6,  0,  0},
            { 0,  0,  0,  6,  0,  9,  0},
            {14,  0,  2,  0,  9,  0,  0},
            { 0,  0,  0,  0,  0,  0,  0}
        };

        Dijkstra.DijkstraResult result = Dijkstra.compute(m, 0);
        Dijkstra.printAllDistances(result, labels);

        for (int target : new int[]{1, 4, 6}) {
            System.out.println("\n" + "-".repeat(35));
            Dijkstra.printPath(Dijkstra.getPath(result, target), 0, target, labels);
        }
    }

    // ================================================================
    //  main
    // ================================================================

    public static void main(String[] args) {
        runAllTests();
        runDemo();
    }
}