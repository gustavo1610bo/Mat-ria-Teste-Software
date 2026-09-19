package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoliticaDescontoTest {
    private final PoliticaDesconto politica = new PoliticaDesconto();

    @Test
    void vipRecebeDezPorCento() {
        assertEquals(1_000, politica.calcular(new Cliente(true, false, 2), 10_000, null));
    }

    @Test
    void comumAtingindoLimiarRecebeCincoPorCento() {
        assertEquals(2_500, politica.calcular(new Cliente(false, false, 1), 50_000, null));
        assertEquals(0, politica.calcular(new Cliente(false, false, 1), 49_999, null));
    }

    @Test
    void cupomNuloOuBrancoMantemDescontoBase() {
        Cliente comum = new Cliente(false, false, 1);
        assertEquals(2_500, politica.calcular(comum, 50_000, null));
        assertEquals(2_500, politica.calcular(comum, 50_000, "   "));
    }

    @Test
    void bemVindoExigePrimeiraCompraEValorMinimoEIgnoraCaixaEspacos() {
        assertEquals(2_000, politica.calcular(new Cliente(false, false, 0), 10_000, "  bemvindo "));
        assertEquals(0, politica.calcular(new Cliente(false, false, 1), 10_000, "BEMVINDO"));
        assertEquals(0, politica.calcular(new Cliente(false, false, 0), 9_999, "BEMVINDO"));
    }

    @Test
    void extraDezAtingeLimiar() {
        assertEquals(2_000, politica.calcular(new Cliente(false, false, 1), 20_000, "extra10"));
        assertEquals(0, politica.calcular(new Cliente(false, false, 1), 19_999, "EXTRA10"));
    }

    @Test
    void descontoCombinadoEhLimitadoAVintePorCento() {
        assertEquals(20_000, politica.calcular(new Cliente(true, false, 0), 100_000, "EXTRA10"));
    }

    @Test
    void cupomConhecidoSemElegibilidadeNaoAcrescentaEDesconhecidoFalha() {
        assertEquals(0, politica.calcular(new Cliente(false, false, 1), 10_000, "BEMVINDO"));
        assertEquals(0, politica.calcular(new Cliente(false, false, 1), 10_000, "EXTRA10"));
        assertThrows(IllegalArgumentException.class, () -> politica.calcular(new Cliente(false, false, 1), 10_000, "NADA"));
    }

    @Test
    void rejeitaSubtotalNegativo() {
        assertThrows(IllegalArgumentException.class, () -> politica.calcular(new Cliente(false, false, 0), -1, null));
    }
}
