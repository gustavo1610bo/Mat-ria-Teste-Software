package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {
    private ItemPedido item(String sku, long preco, int qtd, int estoque, int peso, boolean fragil) {
        return new ItemPedido(sku, preco, qtd, estoque, peso, fragil);
    }

    @Test
    void calculaSubtotalIgnorandoLinhasInativas() {
        Pedido pedido = new Pedido(List.of(item("A", 1_000, 2, 2, 100, false), item("B", 2_000, 0, 0, 200, true)), "PR", false, null);
        assertEquals(2_000, pedido.subtotalCentavos());
    }

    @Test
    void calculaPesoEFragilidadeApenasDosItensAtivos() {
        Pedido pedido = new Pedido(List.of(item("A", 1_000, 2, 2, 500, false), item("B", 2_000, 0, 0, 9_000, true), item("C", 500, 1, 1, 250, true)), "SP", false, null);
        assertEquals(1_250, pedido.pesoGramas());
        assertTrue(pedido.temFragil());
    }

    @Test
    void detectaEstoquePorLinhaEInterrompeNaPrimeiraFalta() {
        Pedido ok = new Pedido(List.of(item("A", 100, 1, 1, 10, false), item("B", 100, 2, 2, 10, false)), "PR", false, null);
        Pedido falta = new Pedido(List.of(item("A", 100, 2, 1, 10, false), item("B", 100, 1, 1, 10, false)), "PR", false, null);
        assertTrue(ok.estoqueSuficiente());
        assertFalse(falta.estoqueSuficiente());
    }

    @Test
    void copiaListaDefensivamente() {
        List<ItemPedido> itens = new ArrayList<>();
        itens.add(item("A", 100, 1, 1, 10, false));
        Pedido pedido = new Pedido(itens, "PR", false, null);
        itens.clear();
        assertEquals(100, pedido.subtotalCentavos());
        assertThrows(UnsupportedOperationException.class, () -> pedido.itens().add(item("B", 100, 1, 1, 10, false)));
    }

    @Test
    void rejeitaListaNulaGrandeElementosNulosEUfInvalida() {
        List<ItemPedido> grande = new ArrayList<>();
        for (int i = 0; i < 101; i++) grande.add(item("A" + i, 100, 1, 1, 1, false));
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> new Pedido(null, "PR", false, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Pedido(grande, "PR", false, null)),
            () -> assertThrows(NullPointerException.class, () -> new Pedido(List.of((ItemPedido) null), "PR", false, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(), null, false, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(), "pr", false, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(), "P1", false, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(), "PRX", false, null))
        );
    }
}
