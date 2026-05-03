/*
 * Ciclo Hamiltoniano en Grafo Dirigido
 * Compilar: g++ -O2 -o hamiltonian hamiltonian_cpp.cpp
 */

#include <iostream>
#include <vector>
#include <algorithm>
#include <chrono>
#include <numeric>
#include <string>
#include <iomanip>
#include <set>

using namespace std;
using namespace std::chrono;

typedef vector<vector<int>> Grafo;
typedef vector<int>         Ciclo;

// ─────────────────────────────────────────────
//  Crear grafo desde lista de aristas
// ─────────────────────────────────────────────
Grafo crear_grafo(int n, const vector<pair<int,int>>& aristas) {
    Grafo adj(n, vector<int>(n, 0));
    for (auto& [u, v] : aristas)
        adj[u][v] = 1;
    return adj;
}


Grafo construir_grafo_desafiante(int n) {
    set<pair<int,int>> aristas_set;

    // Ciclo descendente
    vector<int> ciclo = {0};
    for (int i = n-1; i >= 1; i--) ciclo.push_back(i);
    ciclo.push_back(0);
    for (int i = 0; i < (int)ciclo.size()-1; i++)
        aristas_set.insert({ciclo[i], ciclo[i+1]});

    // Aristas extra
    for (int k = 1; k < n-2; k++) {
        aristas_set.insert({k, 0});
        aristas_set.insert({0, k});
    }
    if (n >= 4) {
        aristas_set.insert({n-1, n-3});
        aristas_set.insert({n-2, n-4});
    }

    vector<pair<int,int>> aristas(aristas_set.begin(), aristas_set.end());
    return crear_grafo(n, aristas);
}

// ─────────────────────────────────────────────
//  Utilidades
// ─────────────────────────────────────────────
double densidad(const Grafo& adj, int n) {
    int m = 0;
    for (int i = 0; i < n; i++)
        for (int j = 0; j < n; j++)
            if (i != j && adj[i][j]) m++;
    return 100.0 * m / (n * (n-1));
}

int contar_aristas(const Grafo& adj, int n) {
    int m = 0;
    for (int i = 0; i < n; i++)
        for (int j = 0; j < n; j++)
            if (i != j && adj[i][j]) m++;
    return m;
}

void imprimir_resultado(const string& nombre, bool encontrado, double ms, long long extra) {
    cout << "  " << nombre << ": " << fixed << setprecision(4) << ms << " ms";
    if (extra > 0) cout << "  (" << (nombre.find("bruta") != string::npos ? "perms=" : "exp=") << extra << ")";
    cout << "\n";
}

// ─────────────────────────────────────────────
//  SOLUCIÓN BASE — Fuerza bruta O(n!)
// ─────────────────────────────────────────────
bool es_ciclo_hamiltoniano(const Grafo& adj, const vector<int>& perm) {
    int n = perm.size();
    for (int k = 0; k < n-1; k++)
        if (adj[perm[k]][perm[k+1]] == 0) return false;
    return adj[perm[n-1]][perm[0]] == 1;
}

pair<Ciclo, long long> fuerza_bruta(const Grafo& adj, int n) {
    vector<int> resto(n-1);
    iota(resto.begin(), resto.end(), 1);
    long long perms = 0;

    do {
        perms++;
        vector<int> candidato = {0};
        candidato.insert(candidato.end(), resto.begin(), resto.end());
        if (es_ciclo_hamiltoniano(adj, candidato)) {
            candidato.push_back(0);
            return {candidato, perms};
        }
    } while (next_permutation(resto.begin(), resto.end()));

    return {{}, perms};
}

// ─────────────────────────────────────────────
//  SOLUCIÓN MEJORADA — Backtracking con poda
// ─────────────────────────────────────────────
bool _backtrack(const Grafo& adj, int n, Ciclo& camino,
                vector<bool>& visitado, int profundidad, long long& expansiones) {
    if (profundidad == n)
        return adj[camino.back()][camino[0]] == 1;

    int actual = camino.back();
    for (int vecino = 0; vecino < n; vecino++) {
        if (adj[actual][vecino] == 1 && !visitado[vecino]) {
            expansiones++;
            camino.push_back(vecino);
            visitado[vecino] = true;

            if (_backtrack(adj, n, camino, visitado, profundidad+1, expansiones))
                return true;

            camino.pop_back();
            visitado[vecino] = false;
        }
    }
    return false;
}

pair<Ciclo, long long> backtracking(const Grafo& adj, int n) {
    Ciclo camino = {0};
    vector<bool> visitado(n, false);
    visitado[0] = true;
    long long expansiones = 0;

    if (_backtrack(adj, n, camino, visitado, 1, expansiones)) {
        camino.push_back(camino[0]);
        return {camino, expansiones};
    }
    return {{}, expansiones};
}

// ─────────────────────────────────────────────
//  Main
// ─────────────────────────────────────────────
int main() {
    cout << "\n" << string(50,'=') << "\n";
    cout << "  CICLO HAMILTONIANO — GRAFO DIRIGIDO\n";
    cout << "  Implementacion C++\n";
    cout << string(50,'=') << "\n";
    cout << "\nComparacion en varios n (grafo desafiante)\n\n";

    // ── Fuerza bruta: n=8 a n=13 ────────────
    cout << "--- Fuerza Bruta (n=8..13) ---\n\n";
    for (int n = 8; n <= 13; n++) {
        Grafo adj = construir_grafo_desafiante(n);
        int m = contar_aristas(adj, n);

        cout << "n=" << n << ", m=" << m
             << fixed << setprecision(2)
             << ", densidad=" << densidad(adj, n) << "%\n";

        auto t0 = high_resolution_clock::now();
        auto [ciclo_fb, perms_fb] = fuerza_bruta(adj, n);
        auto t1 = high_resolution_clock::now();
        double ms_fb = duration<double, milli>(t1 - t0).count();
        imprimir_resultado("  Fuerza bruta", !ciclo_fb.empty(), ms_fb, perms_fb);
        cout << "\n";
    }

    // ── Backtracking: n=8 a n=20 ────────────
    cout << "--- Backtracking con Poda (n=8..20) ---\n\n";
    for (int n = 8; n <= 20; n++) {
        Grafo adj = construir_grafo_desafiante(n);
        int m = contar_aristas(adj, n);

        cout << "n=" << n << ", m=" << m
             << fixed << setprecision(2)
             << ", densidad=" << densidad(adj, n) << "%\n";

        auto t0 = high_resolution_clock::now();
        auto [ciclo_bt, exp_bt] = backtracking(adj, n);
        auto t1 = high_resolution_clock::now();
        double ms_bt = duration<double, milli>(t1 - t0).count();
        imprimir_resultado("  Backtracking", !ciclo_bt.empty(), ms_bt, exp_bt);
        cout << "\n";
    }

    return 0;
}
