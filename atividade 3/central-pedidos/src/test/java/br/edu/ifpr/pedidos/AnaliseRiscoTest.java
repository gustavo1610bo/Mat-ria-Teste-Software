package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnaliseRiscoTest {
    private final AnaliseRisco risco = new AnaliseRisco();

    @Test
    void bloqueadoEhRecusado() {
        assertEquals("RECUSADO", risco.avaliar(new Cliente(false, true, 5), 999_999, true));
    }

    @Test
    void primeiraCompraRevisaValorAcimaDeMilOuExpresso() {
        Cliente novo = new Cliente(false, false, 0);
        assertEquals("REVISAO", risco.avaliar(novo, 100_001, false));
        assertEquals("REVISAO", risco.avaliar(novo, 100_000, true));
        assertEquals("APROVADO", risco.avaliar(novo, 100_000, false));
    }

    @Test
    void clienteComHistoricoRevisaSomenteAcimaDeCincoMilSeNaoVip() {
        assertEquals("REVISAO", risco.avaliar(new Cliente(false, false, 1), 500_001, false));
        assertEquals("APROVADO", risco.avaliar(new Cliente(false, false, 1), 500_000, false));
        assertEquals("APROVADO", risco.avaliar(new Cliente(true, false, 1), 900_000, false));
    }

    @Test
    void demaisCasosSaoAprovados() {
        assertEquals("APROVADO", risco.avaliar(new Cliente(false, false, 3), 1, true));
    }

    @Test
    void rejeitaTotalNegativo() {
        assertThrows(IllegalArgumentException.class, () -> risco.avaliar(new Cliente(false, false, 0), -1, false));
    }
}
