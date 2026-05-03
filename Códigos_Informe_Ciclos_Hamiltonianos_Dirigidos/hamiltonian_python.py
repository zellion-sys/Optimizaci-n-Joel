"""
Ciclo Hamiltoniano en Grafo Dirigido
Curso de Optimización - Problemas NP-Completos

Implementación en Python:
"""

import time
from itertools import permutations
from typing import List, Optional, Tuple


# ─────────────────────────────────────────────
#  Representación del grafo
# ─────────────────────────────────────────────

def crear_grafo(n: int, aristas: List[Tuple[int, int]]) -> List[List[int]]:
    adj = [[0] * n for _ in range(n)]
    for i, j in aristas:
        adj[i][j] = 1
    return adj


def construir_grafo_desafiante(n: int):
    """
    Ciclo descendente: 0→(n-1)→(n-2)→...→1→0
    + aristas extra para agregar ruido de conectividad.
    El ciclo es la última permutación lexicográfica,
    forzando a la fuerza bruta a recorrer casi todo el árbol.
    """
    aristas_set = set()

    # Ciclo descendente
    ciclo = [0] + list(range(n - 1, 0, -1)) + [0]
    for i in range(len(ciclo) - 1):
        aristas_set.add((ciclo[i], ciclo[i + 1]))

    # Aristas extra
    for k in range(1, n - 2):
        aristas_set.add((k, 0))
        aristas_set.add((0, k))
    if n >= 4:
        aristas_set.add((n - 1, n - 3))
        aristas_set.add((n - 2, n - 4))

    aristas = sorted(aristas_set)
    adj = crear_grafo(n, aristas)
    m   = sum(adj[i][j] for i in range(n) for j in range(n) if i != j)
    dens = 100.0 * m / (n * (n - 1))
    return adj, m, dens


# ─────────────────────────────────────────────
#  SOLUCIÓN BASE — Fuerza bruta O(n!)
# ─────────────────────────────────────────────

def es_ciclo_hamiltoniano(adj, permutacion):
    n = len(permutacion)
    for k in range(n - 1):
        if adj[permutacion[k]][permutacion[k + 1]] == 0:
            return False
    return adj[permutacion[-1]][permutacion[0]] == 1


def fuerza_bruta(adj, n) -> Tuple[Optional[List[int]], int]:
    resto = list(range(1, n))
    perms = 0
    for perm in permutations(resto):
        perms += 1
        candidato = [0] + list(perm)
        if es_ciclo_hamiltoniano(adj, candidato):
            return candidato + [0], perms
    return None, perms


# ─────────────────────────────────────────────
#  SOLUCIÓN MEJORADA — Backtracking con poda
# ─────────────────────────────────────────────

def backtracking(adj, n) -> Tuple[Optional[List[int]], int]:
    camino   = [0]
    visitado = [False] * n
    visitado[0] = True
    expansiones = 0

    def _bt(profundidad):
        nonlocal expansiones
        if profundidad == n:
            return adj[camino[-1]][camino[0]] == 1
        nodo_actual = camino[-1]
        for vecino in range(n):
            if adj[nodo_actual][vecino] == 1 and not visitado[vecino]:
                expansiones += 1
                camino.append(vecino)
                visitado[vecino] = True
                if _bt(profundidad + 1):
                    return True
                camino.pop()
                visitado[vecino] = False
        return False

    if _bt(1):
        return camino + [camino[0]], expansiones
    return None, expansiones


# ─────────────────────────────────────────────
#  Main
# ─────────────────────────────────────────────

if __name__ == "__main__":

    print("\n" + "=" * 50)
    print("  CICLO HAMILTONIANO — GRAFO DIRIGIDO")
    print("  Implementación Python")
    print("=" * 50)
    print("\nComparacion en varios n (grafo desafiante)\n")

    # ── Fuerza bruta: n=8 a n=13 ────────────
    print("--- Fuerza Bruta (n=8..13) ---\n")
    for n in range(8, 14):
        adj, m, dens = construir_grafo_desafiante(n)
        print(f"n={n}, m={m}, densidad={dens:.2f}%")

        t0 = time.perf_counter()
        _, perms = fuerza_bruta(adj, n)
        ms_fb = (time.perf_counter() - t0) * 1000
        print(f"  Fuerza bruta: {ms_fb:.4f} ms  (perms={perms})\n")

    # ── Backtracking: n=8 a n=20 ────────────
    print("--- Backtracking con Poda (n=8..20) ---\n")
    for n in range(8, 21):
        adj, m, dens = construir_grafo_desafiante(n)
        print(f"n={n}, m={m}, densidad={dens:.2f}%")

        t0 = time.perf_counter()
        _, exp = backtracking(adj, n)
        ms_bt = (time.perf_counter() - t0) * 1000
        print(f"  Backtracking: {ms_bt:.4f} ms  (exp={exp})\n")
