package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {
    @Test
    void aceitaClienteComHistoricoZero() {
        Cliente c = new Cliente(false, false, 0);
        assertAll(() -> assertFalse(c.vip()), () -> assertFalse(c.bloqueado()), () -> assertEquals(0, c.comprasAnteriores()));
    }

    @Test
    void rejeitaHistoricoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente(false, false, -1));
    }
}
