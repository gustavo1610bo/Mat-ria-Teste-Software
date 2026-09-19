package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest {
    private ItemPedido item(String sku, long preco, int qtd, int estoque, int peso, boolean fragil) {
        return new ItemPedido(sku, preco, qtd, estoque, peso, fragil);
    }

    @Test
    void deveFecharPedidoDeClienteComumComFreteDoParanaEPagamentoAprovado() {
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> { cobrancas.add(total); return true; });
        ResultadoPedido resultado = service.fechar(pedido, cliente);
        assertAll(
            () -> assertEquals("PAGO", resultado.status()),
            () -> assertEquals(10_000L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(1_200L, resultado.freteCentavos()),
            () -> assertEquals(11_200L, resultado.totalCentavos()),
            () -> assertEquals(List.of(11_200L), cobrancas)
        );
    }

    @Test
    void bloqueadoRetornaTudoZeroESemCobrar() {
        int[] chamadas = {0};
        Pedido pedido = new Pedido(List.of(item("A", 100, 1, 0, 1, false)), "PR", false, "CUPOM-INVALIDO");
        ResultadoPedido r = new PedidoService(total -> { chamadas[0]++; return true; }).fechar(pedido, new Cliente(false, true, 0));
        assertAll(() -> assertEquals("BLOQUEADO", r.status()), () -> assertEquals(0, r.subtotalCentavos()), () -> assertEquals(0, r.totalCentavos()), () -> assertEquals(0, chamadas[0]));
    }

    @Test
    void pedidoSemItensAtivosFalhaAntesDeEstoqueEDesconto() {
        Pedido pedido = new Pedido(List.of(item("A", 100, 0, 0, 1, false)), "PR", false, "DESCONHECIDO");
        assertThrows(IllegalArgumentException.class, () -> new PedidoService(total -> true).fechar(pedido, new Cliente(false, false, 0)));
    }

    @Test
    void faltaDeEstoqueRetornaSemEstoqueESemCobrar() {
        int[] chamadas = {0};
        Pedido pedido = new Pedido(List.of(item("A", 10_000, 2, 1, 1, false)), "PR", false, "DESCONHECIDO");
        ResultadoPedido r = new PedidoService(total -> { chamadas[0]++; return true; }).fechar(pedido, new Cliente(false, false, 0));
        assertEquals("SEM_ESTOQUE", r.status());
        assertEquals(0, r.totalCentavos());
        assertEquals(0, chamadas[0]);
    }

    @Test
    void aplicaDescontoFreteERetornaRevisaoSemCobrar() {
        int[] chamadas = {0};
        Pedido pedido = new Pedido(List.of(item("A", 120_000, 1, 1, 1_000, false)), "PR", false, "BEMVINDO");
        ResultadoPedido r = new PedidoService(total -> { chamadas[0]++; return true; }).fechar(pedido, new Cliente(false, false, 0));
        assertAll(
            () -> assertEquals("REVISAO", r.status()),
            () -> assertEquals(120_000, r.subtotalCentavos()),
            () -> assertEquals(8_000, r.descontoCentavos()),
            () -> assertEquals(0, r.freteCentavos()),
            () -> assertEquals(112_000, r.totalCentavos()),
            () -> assertEquals(0, chamadas[0])
        );
    }

    @Test
    void riscoRevisaCompraAntigaNaoVipAcimaDeCincoMil() {
        int[] chamadas = {0};
        Pedido pedido = new Pedido(List.of(item("A", 530_000, 1, 1, 1_000, false)), "SP", false, null);
        ResultadoPedido r = new PedidoService(total -> { chamadas[0]++; return true; }).fechar(pedido, new Cliente(false, false, 1));
        assertEquals("REVISAO", r.status());
        assertEquals(503_500, r.totalCentavos());
        assertEquals(0, chamadas[0]);
    }

    @Test
    void pagamentoRecusadoRetornaValoresCalculados() {
        int[] chamadas = {0};
        Pedido pedido = new Pedido(List.of(item("A", 20_000, 1, 1, 1_000, false)), "PR", false, null);
        ResultadoPedido r = new PedidoService(total -> { chamadas[0]++; return false; }).fechar(pedido, new Cliente(false, false, 1));
        assertAll(() -> assertEquals("PAGAMENTO_RECUSADO", r.status()), () -> assertEquals(20_000, r.subtotalCentavos()), () -> assertEquals(1_200, r.freteCentavos()), () -> assertEquals(21_200, r.totalCentavos()), () -> assertEquals(1, chamadas[0]));
    }

    @Test
    void pagamentoTemAteTresTentativasQuandoHaIndisponibilidade() {
        int[] chamadas = {0};
        Pedido pedido = new Pedido(List.of(item("A", 20_000, 1, 1, 1_000, false)), "PR", false, null);
        ResultadoPedido r = new PedidoService(total -> { chamadas[0]++; throw new IllegalStateException(); }).fechar(pedido, new Cliente(false, false, 1));
        assertEquals("PAGAMENTO_RECUSADO", r.status());
        assertEquals(3, chamadas[0]);
    }

    @Test
    void expressoEFragilAlteramFreteERiscoNaPrimeiraCompra() {
        Pedido pedido = new Pedido(List.of(item("A", 100_000, 1, 1, 1_000, true)), "RJ", true, null);
        ResultadoPedido r = new PedidoService(total -> true).fechar(pedido, new Cliente(false, false, 0));
        assertAll(() -> assertEquals("REVISAO", r.status()), () -> assertEquals(99_000, r.totalCentavos()), () -> assertEquals(4_000, r.freteCentavos()));
    }

    @Test
    void rejeitaReferenciasNulasAntesDeQualquerEfeito() {
        PedidoService service = new PedidoService(total -> fail("não deveria cobrar"));
        assertAll(
            () -> assertThrows(NullPointerException.class, () -> service.fechar(null, new Cliente(false, false, 0))),
            () -> assertThrows(NullPointerException.class, () -> service.fechar(new Pedido(List.of(item("A", 100, 1, 1, 1, false)), "PR", false, null), null))
        );
    }
}
