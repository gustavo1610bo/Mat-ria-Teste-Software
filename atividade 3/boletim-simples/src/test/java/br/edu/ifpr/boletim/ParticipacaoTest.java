package br.edu.ifpr.boletim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParticipacaoTest {

    @Test
    void deveSomarTresPontosQuandoEntregouEParticipou() {
        // Combinação verdadeiro/verdadeiro: +2 (entrega) + 1 (participação) = 3.
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(true, true);

        assertEquals(3, resultado);
    }

    @Test
    void deveSomarDoisPontosQuandoApenasEntregouAtividade() {
        // Combinação verdadeiro/falso: só os +2 da entrega da atividade.
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(true, false);

        assertEquals(2, resultado);
    }

    @Test
    void deveSomarUmPontoQuandoApenasParticipouDaAula() {
        // Combinação falso/verdadeiro: só o +1 da participação na aula.
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(false, true);

        assertEquals(1, resultado);
    }

    @Test
    void deveSomarZeroPontosQuandoNaoEntregouNemParticipou() {
        // Combinação falso/falso: nenhum ponto deve ser somado.
        Participacao participacao = new Participacao();

        int resultado = participacao.calcularPontos(false, false);

        assertEquals(0, resultado);
    }
}
