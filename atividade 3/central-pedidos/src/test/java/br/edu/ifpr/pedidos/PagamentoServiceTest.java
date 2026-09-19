package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PagamentoServiceTest {
    @Test
    void aprovaNaPrimeiraTentativaSemRepetir() {
        List<Long> chamadas = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> { chamadas.add(total); return true; });
        assertTrue(service.pagar(10_000, 3));
        assertEquals(List.of(10_000L), chamadas);
    }

    @Test
    void recusaDefinitivamenteSemRepetir() {
        int[] chamadas = {0};
        PagamentoService service = new PagamentoService(total -> { chamadas[0]++; return false; });
        assertFalse(service.pagar(10_000, 3));
        assertEquals(1, chamadas[0]);
    }

    @Test
    void indisponibilidadePermiteNovaTentativaEAprova() {
        int[] chamadas = {0};
        List<Long> valores = new ArrayList<>();
        PagamentoService service = new PagamentoService(total -> {
            chamadas[0]++;
            valores.add(total);
            if (chamadas[0] < 3) throw new IllegalStateException("temporario");
            return true;
        });
        assertTrue(service.pagar(12_345, 3));
        assertEquals(3, chamadas[0]);
        assertEquals(List.of(12_345L, 12_345L, 12_345L), valores);
    }

    @Test
    void esgotaTentativasRetornandoFalso() {
        int[] chamadas = {0};
        PagamentoService service = new PagamentoService(total -> { chamadas[0]++; throw new IllegalStateException(); });
        assertFalse(service.pagar(10_000, 2));
        assertEquals(2, chamadas[0]);
    }

    @Test
    void excecaoDiferenteDeIndisponibilidadePropaga() {
        PagamentoService service = new PagamentoService(total -> { throw new IllegalArgumentException("erro"); });
        assertThrows(IllegalArgumentException.class, () -> service.pagar(10_000, 3));
    }

    @Test
    void validaDependenciaTotalELimite() {
        assertThrows(NullPointerException.class, () -> new PagamentoService(null));
        PagamentoService service = new PagamentoService(total -> true);
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> service.pagar(0, 1)),
            () -> assertThrows(IllegalArgumentException.class, () -> service.pagar(-1, 1)),
            () -> assertThrows(IllegalArgumentException.class, () -> service.pagar(1, 0)),
            () -> assertThrows(IllegalArgumentException.class, () -> service.pagar(1, 4))
        );
    }
}
