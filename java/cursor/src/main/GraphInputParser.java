package ee.tlu.ba.dijkstra;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Input format (whitespace separated; lines are not significant):
 * n
 * n*n matrix entries (long). Use -1 to indicate "no edge".
 * start target
 *
 * Vertex indices can be either 0..n-1 or 1..n (auto-detected).
 */
public final class GraphInputParser {
    private GraphInputParser() {}

    public static GraphInput parse(BufferedReader br) throws IOException {
        List<String> tokens = tokenize(br);
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty input.");
        }

        int pos = 0;
        int n = parseInt(tokens.get(pos++), "n (number of vertices)");
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive.");
        }
        if ((long) n * (long) n > 50_000_000L) {
            throw new IllegalArgumentException("Matrix is too large: n*n exceeds 50,000,000.");
        }

        int neededForMatrix = n * n;
        if (tokens.size() < 1 + neededForMatrix + 2) {
            throw new IllegalArgumentException("Not enough tokens for n*n matrix and start/target indices.");
        }

        long[][] matrix = new long[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                long w = parseLong(tokens.get(pos++), "matrix[" + i + "][" + j + "]");
                validateWeight(w, i, j);
                matrix[i][j] = w;
            }
        }

        int startRaw = parseInt(tokens.get(pos++), "start");
        int targetRaw = parseInt(tokens.get(pos++), "target");
        int[] normalized = normalizeIndices(startRaw, targetRaw, n);

        if (pos != tokens.size()) {
            throw new IllegalArgumentException("Unexpected extra tokens after reading start/target.");
        }

        return new GraphInput(matrix, normalized[0], normalized[1]);
    }

    private static void validateWeight(long w, int i, int j) {
        if (w < -1) {
            throw new IllegalArgumentException("Negative weight detected at matrix[" + i + "][" + j + "]. Only -1 is allowed as 'no edge'.");
        }
        if (w == 0 && i != j) {
            // 0-weight edges are allowed by Dijkstra; keep it.
            return;
        }
    }

    private static int[] normalizeIndices(int startRaw, int targetRaw, int n) {
        boolean zeroBased = inRange(startRaw, 0, n - 1) && inRange(targetRaw, 0, n - 1);
        boolean oneBased = inRange(startRaw, 1, n) && inRange(targetRaw, 1, n);

        if (zeroBased && !oneBased) return new int[] { startRaw, targetRaw };
        if (oneBased && !zeroBased) return new int[] { startRaw - 1, targetRaw - 1 };

        if (zeroBased) {
            // Ambiguous (e.g. n=1, start=0,target=1 impossible; or small n). Prefer 0-based if it's valid.
            return new int[] { startRaw, targetRaw };
        }

        throw new IllegalArgumentException("Start/target indices must be in [0.." + (n - 1) + "] or [1.." + n + "].");
    }

    private static boolean inRange(int v, int lo, int hi) {
        return v >= lo && v <= hi;
    }

    private static int parseInt(String s, String name) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer for " + name + ": '" + s + "'.");
        }
    }

    private static long parseLong(String s, String name) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid long for " + name + ": '" + s + "'.");
        }
    }

    private static List<String> tokenize(BufferedReader br) throws IOException {
        List<String> out = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            // allow '#' comments
            int commentIdx = trimmed.indexOf('#');
            if (commentIdx >= 0) trimmed = trimmed.substring(0, commentIdx).trim();
            if (trimmed.isEmpty()) continue;

            String[] parts = trimmed.split("\\s+");
            for (String p : parts) {
                if (!p.isEmpty()) out.add(p);
            }
        }
        return out;
    }
}

