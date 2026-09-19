package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {
    @Test
    void calculaTotalEDisponibilidade() {
        ItemPedido item = new ItemPedido("A", 1_250, 4, 5, 100, false);
        assertAll(() -> assertEquals(5_000, item.totalCentavos()), () -> assertTrue(item.disponivel()));
    }

    @Test
    void quantidadeZeroFormaLinhaInativa() {
        ItemPedido item = new ItemPedido("A", 1_250, 0, 0, 100, true);
        assertEquals(0, item.totalCentavos());
        assertTrue(item.disponivel());
    }

    @Test
    void quantidadeAcimaDoEstoqueFicaIndisponivel() {
        ItemPedido item = new ItemPedido("A", 1_250, 3, 2, 100, false);
        assertFalse(item.disponivel());
    }

    @Test
    void rejeitaSkuNuloOuBranco() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido(null, 100, 1, 1, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("   ", 100, 1, 1, 1, false))
        );
    }

    @Test
    void rejeitaPrecoQuantidadeEstoqueEPesoInvalidos() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("A", 0, 1, 1, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("A", 1_000_001, 1, 1, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("A", 100, -1, 1, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("A", 100, 101, 1, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("A", 100, 1, -1, 1, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("A", 100, 1, 1, 0, false)),
            () -> assertThrows(IllegalArgumentException.class, () -> new ItemPedido("A", 100, 1, 1, 100_001, false))
        );
    }
}
