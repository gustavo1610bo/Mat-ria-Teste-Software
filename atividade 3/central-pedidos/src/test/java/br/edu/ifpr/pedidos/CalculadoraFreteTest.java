package br.edu.ifpr.pedidos;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraFreteTest {
    private ItemPedido item(int peso, int qtd, boolean fragil) { return new ItemPedido("A", 1_000, qtd, qtd, peso, fragil); }
    private Pedido pedido(String uf, boolean expresso, ItemPedido... itens) { return new Pedido(List.of(itens), uf, expresso, null); }

    @Test
    void usaTarifasDePrSpRjEDefault() {
        CalculadoraFrete c = new CalculadoraFrete();
        Cliente comum = new Cliente(false, false, 1);
        assertEquals(1_200, c.calcular(pedido("PR", false, item(1_000, 1, false)), comum, 10_000));
        assertEquals(2_000, c.calcular(pedido("SP", false, item(1_000, 1, false)), comum, 10_000));
        assertEquals(2_000, c.calcular(pedido("RJ", false, item(1_000, 1, false)), comum, 10_000));
        assertEquals(3_000, c.calcular(pedido("MG", false, item(1_000, 1, false)), comum, 10_000));
    }

    @Test
    void cobraPesoPorKgOuFracaoAcimaDeDoisKg() {
        CalculadoraFrete c = new CalculadoraFrete();
        Cliente comum = new Cliente(false, false, 1);
        assertEquals(1_200, c.calcular(pedido("PR", false, item(2_000, 1, false)), comum, 10_000));
        assertEquals(1_500, c.calcular(pedido("PR", false, item(2_001, 1, false)), comum, 10_000));
        assertEquals(1_800, c.calcular(pedido("PR", false, item(3_001, 1, false)), comum, 10_000));
    }

    @Test
    void freteGratisQuandoLiquidoAtingeTrezentosENaoEhExpresso() {
        CalculadoraFrete c = new CalculadoraFrete();
        Cliente comum = new Cliente(false, false, 1);
        assertEquals(0, c.calcular(pedido("PR", false, item(5_000, 1, false)), comum, 30_000));
    }

    @Test
    void vipPagaMetadeInclusiveDoAdicionalDePeso() {
        CalculadoraFrete c = new CalculadoraFrete();
        Cliente vip = new Cliente(true, false, 1);
        assertEquals(750, c.calcular(pedido("PR", false, item(2_001, 1, false)), vip, 10_000));
    }

    @Test
    void expressoESemFragilAcrescentaQuinze() {
        CalculadoraFrete c = new CalculadoraFrete();
        Cliente comum = new Cliente(false, false, 1);
        assertEquals(2_700, c.calcular(pedido("PR", true, item(1_000, 1, false)), comum, 30_000));
    }

    @Test
    void fragilAcrescentaUmaVezEAdicionaisContinuamQuandoBaseZerada() {
        CalculadoraFrete c = new CalculadoraFrete();
        Cliente comum = new Cliente(false, false, 1);
        Pedido p = pedido("PR", false, item(1_000, 1, true), item(1_000, 1, true));
        assertEquals(500, c.calcular(p, comum, 30_000));
        Pedido exp = pedido("PR", true, item(1_000, 1, true));
        assertEquals(3_200, c.calcular(exp, comum, 30_000));
    }

    @Test
    void rejeitaLiquidoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new CalculadoraFrete().calcular(pedido("PR", false, item(1_000, 1, false)), new Cliente(false, false, 0), -1));
    }
}
