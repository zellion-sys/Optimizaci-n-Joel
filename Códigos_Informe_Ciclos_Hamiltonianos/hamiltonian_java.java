/**
 * Ciclo Hamiltoniano en Grafo Dirigido
 * Ejecutar: java Hamiltonianjavacito
 */

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Hamiltonianjavacito {

    // ─────────────────────────────────────────────
    //  Crear grafo desde lista de aristas
    // ─────────────────────────────────────────────
    static int[][] crearGrafo(int n, int[][] aristas) {
        int[][] adj = new int[n][n];
        for (int[] a : aristas) adj[a[0]][a[1]] = 1;
        return adj;
    }

    static int[][] construirGrafoDesafiante(int n) {
        Set<String> conjunto = new LinkedHashSet<>();
        List<int[]> aristas = new ArrayList<>();

        // Ciclo descendente
        int[] ciclo = new int[n + 1];
        ciclo[0] = 0;
        for (int i = 1; i < n; i++) ciclo[i] = n - i;
        ciclo[n] = 0;
        for (int i = 0; i < n; i++) {
            String key = ciclo[i] + "," + ciclo[i + 1];
            if (conjunto.add(key)) aristas.add(new int[]{ciclo[i], ciclo[i + 1]});
        }

        // Aristas extra
        for (int k = 1; k < n - 2; k++) {
            if (conjunto.add(k + ",0"))   aristas.add(new int[]{k, 0});
            if (conjunto.add("0," + k))   aristas.add(new int[]{0, k});
        }
        if (n >= 4) {
            if (conjunto.add((n-1) + "," + (n-3))) aristas.add(new int[]{n-1, n-3});
            if (conjunto.add((n-2) + "," + (n-4))) aristas.add(new int[]{n-2, n-4});
        }

        return crearGrafo(n, aristas.toArray(new int[0][]));
    }

    // ─────────────────────────────────────────────
    //  Utilidades
    // ─────────────────────────────────────────────
    static int contarAristas(int[][] adj, int n) {
        int m = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i != j && adj[i][j] == 1) m++;
        return m;
    }

    static double densidad(int[][] adj, int n) {
        return 100.0 * contarAristas(adj, n) / (n * (n - 1));
    }

    // ─────────────────────────────────────────────
    //  SOLUCIÓN BASE — Fuerza bruta O(n!)
    // ─────────────────────────────────────────────
    static boolean esCicloHamiltoniano(int[][] adj, int[] perm) {
        int n = perm.length;
        for (int k = 0; k < n - 1; k++)
            if (adj[perm[k]][perm[k + 1]] == 0) return false;
        return adj[perm[n - 1]][perm[0]] == 1;
    }

    static boolean siguientePermutacion(int[] arr) {
        int n = arr.length, i = n - 2;
        while (i >= 0 && arr[i] >= arr[i + 1]) i--;
        if (i < 0) return false;
        int j = n - 1;
        while (arr[j] <= arr[i]) j--;
        int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        int lo = i + 1, hi = n - 1;
        while (lo < hi) { tmp = arr[lo]; arr[lo] = arr[hi]; arr[hi] = tmp; lo++; hi--; }
        return true;
    }

    static long[] fuerzaBruta(int[][] adj, int n) {
        int[] resto = new int[n - 1];
        for (int i = 0; i < n - 1; i++) resto[i] = i + 1;
        long perms = 0;
        do {
            perms++;
            int[] candidato = new int[n];
            candidato[0] = 0;
            for (int i = 0; i < n - 1; i++) candidato[i + 1] = resto[i];
            if (esCicloHamiltoniano(adj, candidato))
                return new long[]{1, perms};
        } while (siguientePermutacion(resto));
        return new long[]{0, perms};
    }

    // ─────────────────────────────────────────────
    //  SOLUCIÓN MEJORADA — Backtracking con poda
    // ─────────────────────────────────────────────
    static long expansionesBT;

    static boolean _backtrack(int[][] adj, int n, List<Integer> camino,
                               boolean[] visitado, int profundidad) {
        if (profundidad == n)
            return adj[camino.get(camino.size() - 1)][camino.get(0)] == 1;
        int actual = camino.get(camino.size() - 1);
        for (int vecino = 0; vecino < n; vecino++) {
            if (adj[actual][vecino] == 1 && !visitado[vecino]) {
                expansionesBT++;
                camino.add(vecino);
                visitado[vecino] = true;
                if (_backtrack(adj, n, camino, visitado, profundidad + 1)) return true;
                camino.remove(camino.size() - 1);
                visitado[vecino] = false;
            }
        }
        return false;
    }

    static long[] backtracking(int[][] adj, int n) {
        List<Integer> camino = new ArrayList<>();
        camino.add(0);
        boolean[] visitado = new boolean[n];
        visitado[0] = true;
        expansionesBT = 0;
        boolean encontrado = _backtrack(adj, n, camino, visitado, 1);
        return new long[]{encontrado ? 1 : 0, expansionesBT};
    }

    // ─────────────────────────────────────────────
    //  Main
    // ─────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  CICLO HAMILTONIANO — GRAFO DIRIGIDO");
        System.out.println("  Implementacion Java");
        System.out.println("=".repeat(50));
        System.out.println("\nComparacion en varios n (grafo desafiante)\n");

        // ── Fuerza bruta: n=8 a n=13 ────────────
        System.out.println("--- Fuerza Bruta (n=8..13) ---\n");
        for (int n = 8; n <= 13; n++) {
            int[][] adj = construirGrafoDesafiante(n);
            int m = contarAristas(adj, n);
            System.out.printf("n=%d, m=%d, densidad=%.2f%%%n", n, m, densidad(adj, n));

            long t0 = System.nanoTime();
            long[] resFb = fuerzaBruta(adj, n);
            double msFb = (System.nanoTime() - t0) / 1_000_000.0;
            System.out.printf("  Fuerza bruta: %.4f ms  (perms=%d)%n%n", msFb, resFb[1]);
        }

        // ── Backtracking: n=8 a n=20 ────────────
        System.out.println("--- Backtracking con Poda (n=8..20) ---\n");
        for (int n = 8; n <= 20; n++) {
            int[][] adj = construirGrafoDesafiante(n);
            int m = contarAristas(adj, n);
            System.out.printf("n=%d, m=%d, densidad=%.2f%%%n", n, m, densidad(adj, n));

            long t0 = System.nanoTime();
            long[] resBt = backtracking(adj, n);
            double msBt = (System.nanoTime() - t0) / 1_000_000.0;
            System.out.printf("  Backtracking: %.4f ms  (exp=%d)%n%n", msBt, resBt[1]);
        }
    }
}
