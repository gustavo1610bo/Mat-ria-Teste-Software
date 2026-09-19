import { test, expect } from '@playwright/test';

async function preencherECalcular(page: import('@playwright/test').Page, cep: string, valor: string) {
    await page.goto('/frete');
    await page.getByLabel('CEP').fill(cep);
    await page.getByLabel('Valor do pedido').fill(valor);
    await page.getByRole('button', { name: 'Calcular frete' }).click();
}

const casosCep = [
    { cep: '8000000', valor: '100,00', esperado: 'Dados inválidos', classe: 'CEP com 7 dígitos (abaixo do mínimo)' },
    { cep: '80000000', valor: '100,00', esperado: 'Frete: R$ 15,00', classe: 'CEP com 8 dígitos iniciado em 8 (limite mínimo de tamanho)' },
    { cep: '800000000', valor: '100,00', esperado: 'Dados inválidos', classe: 'CEP com 9 dígitos (acima do máximo)' },
    { cep: '10000000', valor: '100,00', esperado: 'Frete: R$ 25,00', classe: 'CEP válido não iniciado em 8' },
    { cep: '1000000A', valor: '100,00', esperado: 'Dados inválidos', classe: 'CEP com caractere não numérico' },
    { cep: '', valor: '100,00', esperado: 'Dados inválidos', classe: 'CEP vazio' },
];

for (const caso of casosCep) {
    test(`CEP "${caso.cep || '(vazio)'}" — ${caso.classe}`, async ({ page }) => {
        await preencherECalcular(page, caso.cep, caso.valor);

        const resultado = page.locator('#resultado');
        const valido = caso.esperado !== 'Dados inválidos';
        await expect(resultado).toBeVisible();
        await expect(resultado).toHaveText(caso.esperado);
        await expect(resultado).toHaveAttribute('role', valido ? 'status' : 'alert');
    });
}


const casosValor = [
    { valor: '0', esperado: 'Dados inválidos', classe: 'valor zero (não é positivo)' },
    { valor: '-10', esperado: 'Dados inválidos', classe: 'valor negativo' },
    { valor: '10', esperado: 'Frete: R$ 25,00', classe: 'valor inteiro válido, abaixo do limite de frete grátis' },
    { valor: '199,99', esperado: 'Frete: R$ 25,00', classe: 'limite inferior ao frete grátis (199,99)' },
    { valor: '200,00', esperado: 'Frete grátis', classe: 'limite mínimo do frete grátis (200,00)' },
    { valor: '200,01', esperado: 'Frete grátis', classe: 'acima do limite do frete grátis (200,01)' },
    { valor: '10,999', esperado: 'Dados inválidos', classe: 'valor com mais de 2 casas decimais' },
    { valor: 'dez reais', esperado: 'Dados inválidos', classe: 'valor com tipo inválido' },
    { valor: '', esperado: 'Dados inválidos', classe: 'valor vazio' },
];

for (const caso of casosValor) {
    test(`valor "${caso.valor || '(vazio)'}" — ${caso.classe}`, async ({ page }) => {
        await preencherECalcular(page, '10000000', caso.valor);

        const resultado = page.locator('#resultado');
        const valido = caso.esperado !== 'Dados inválidos';
        await expect(resultado).toBeVisible();
        await expect(resultado).toHaveText(caso.esperado);
        await expect(resultado).toHaveAttribute('role', valido ? 'status' : 'alert');
    });
}

test.describe('frete — caminho feliz combinado', () => {
    test('CEP iniciado em 8 e valor abaixo de 200 aplica frete de R$ 15,00', async ({ page }) => {
        await preencherECalcular(page, '80000000', '50,00');

        const resultado = page.locator('#resultado');
        await expect(resultado).toHaveText('Frete: R$ 15,00');
        await expect(resultado).toHaveAttribute('role', 'status');
        await expect(resultado).toHaveClass('success');
    });

    test('valor igual a 200,00 concede frete grátis independente do CEP', async ({ page }) => {
        await preencherECalcular(page, '80000000', '200,00');

        const resultado = page.locator('#resultado');
        await expect(resultado).toHaveText('Frete grátis');
        await expect(resultado).toHaveAttribute('role', 'status');
    });
});
