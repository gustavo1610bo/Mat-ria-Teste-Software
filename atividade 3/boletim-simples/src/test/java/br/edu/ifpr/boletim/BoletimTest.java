package br.edu.ifpr.boletim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BoletimTest {

    @Test
    void deveAprovarAlunoComMediaOito() {
        // Preparar: criar o objeto que será testado.
        Boletim boletim = new Boletim();

        // Executar: chamar um único método com uma entrada conhecida.
        String resultado = boletim.verificarSituacao(8);

        // Verificar: comparar o resultado esperado com o resultado obtido.
        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveAprovarAlunoComMediaExatamenteSete() {
        // Limite exato da aprovação: a regra usa ">=", então 7 já aprova.
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(7);

        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveColocarEmRecuperacaoComMediaLogoAbaixoDeSete() {
        // Um passo abaixo do limite de aprovação: ainda deve cair em recuperação.
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(6.9);

        assertEquals("RECUPERACAO", resultado);
    }

    @Test
    void deveRecuperarNotaAlunoComMediaQuatro() {
        Boletim boletim = new Boletim();

        // Executar: chamar um único método com uma entrada conhecida.
        String resultado = boletim.verificarSituacao(4);

        // Verificar: comparar o resultado esperado com o resultado obtido.
        assertEquals("RECUPERACAO", resultado);
    }

    @Test
    void deveReprovarAlunoComMediaLogoAbaixoDeQuatro() {
        // Um passo abaixo do limite de recuperação: já deve reprovar.
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(3.9);

        assertEquals("REPROVADO", resultado);
    }

    @Test
    void deveReprovarAlunoComMediaDois() {
        Boletim boletim = new Boletim();

        // Executar: chamar um único método com uma entrada conhecida.
        String resultado = boletim.verificarSituacao(2);

        // Verificar: comparar o resultado esperado com o resultado obtido.
        assertEquals("REPROVADO", resultado);
    }

    @Test
    void deveCalcularMediaIgualCinco() {
        Boletim boletim = new Boletim();

        double resultado = boletim.calcularMedia(5, 5);

        assertEquals(5, resultado);
    }

    @Test
    void deveCalcularMediaComParteDecimal() {
        // Notas 7 e 8 geram média 7.5, com parte decimal.
        // Para double, o terceiro argumento de assertEquals é a tolerância aceita.
        Boletim boletim = new Boletim();

        double resultado = boletim.calcularMedia(7, 8);

        assertEquals(7.5, resultado, 0.0001);
    }

    @Test
    void deveContarZeroAprovadosEmArrayVazio() {
        // Zero iterações do for: array vazio deve retornar zero.
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {});

        assertEquals(0, resultado);
    }

    @Test
    void deveContarUmAprovadoComUmUnicoElemento() {
        // Uma iteração do for, com o único elemento sendo aprovado.
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {8});

        assertEquals(1, resultado);
    }

    @Test
    void deveContarZeroAprovadosComUmUnicoElementoReprovado() {
        // Uma iteração do for, com o único elemento não sendo aprovado.
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {5});

        assertEquals(0, resultado);
    }

    @Test
    void deveContarAprovadosEntreVariosElementos() {
        // Várias iterações, misturando aprovados e não aprovados,
        // incluindo o limite exato (7) dentro do array.
        Boletim boletim = new Boletim();

        int resultado = boletim.contarAprovados(new double[] {8, 5, 7, 3, 9});

        assertEquals(3, resultado);
    }
}
