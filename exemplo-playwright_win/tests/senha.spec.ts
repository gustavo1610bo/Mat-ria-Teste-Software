import { test, expect } from '@playwright/test';

async function preencherECadastrar(page: import('@playwright/test').Page, senha: string, confirmacao: string) {
    await page.goto('/senha');
    await page.getByLabel('Nova senha').fill(senha);
    await page.getByLabel('Confirmar senha').fill(confirmacao);
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();
}

function gerarSenha(tamanho: number): string {
    const preenchimento = 'a'.repeat(Math.max(tamanho - 2, 0));
    return `A1${preenchimento}`.slice(0, tamanho);
}


const casosTamanho = [
    { tamanho: 7, aceito: false, classe: 'abaixo do mínimo' },
    { tamanho: 8, aceito: true, classe: 'limite mínimo' },
    { tamanho: 9, aceito: true, classe: 'acima do mínimo' },
    { tamanho: 19, aceito: true, classe: 'abaixo do máximo' },
    { tamanho: 20, aceito: true, classe: 'limite máximo' },
    { tamanho: 21, aceito: false, classe: 'acima do máximo' },
];

for (const caso of casosTamanho) {
    test(`senha com ${caso.tamanho} caracteres — ${caso.classe}`, async ({ page }) => {
        const senha = gerarSenha(caso.tamanho);
        await preencherECadastrar(page, senha, senha);

        const resultado = page.locator('#resultado');
        await expect(resultado).toBeVisible();
        await expect(resultado).toHaveText(caso.aceito ? 'Senha cadastrada' : 'Senha fora do padrão');
        await expect(resultado).toHaveAttribute('role', caso.aceito ? 'status' : 'alert');
    });
}

const casosFormato = [
    { senha: 'abcdefg1', classe: 'sem letra maiúscula' },
    { senha: 'ABCDEFG1', classe: 'sem letra minúscula' },
    { senha: 'Abcdefgh', classe: 'sem dígito' },
    { senha: 'Abcde f1', classe: 'contém espaço' },
    { senha: '', classe: 'vazia' },
];

for (const caso of casosFormato) {
    test(`senha "${caso.senha || '(vazia)'}" — ${caso.classe}`, async ({ page }) => {
        await preencherECadastrar(page, caso.senha, caso.senha);

        const resultado = page.locator('#resultado');
        await expect(resultado).toBeVisible();
        await expect(resultado).toHaveText('Senha fora do padrão');
        await expect(resultado).toHaveAttribute('role', 'alert');
    });
}

test.describe('senha — caminho feliz e confirmação', () => {
    test('cadastra com sucesso quando a senha é válida e a confirmação coincide', async ({ page }) => {
        await preencherECadastrar(page, 'Abcdef12', 'Abcdef12');

        const resultado = page.locator('#resultado');
        await expect(resultado).toHaveText('Senha cadastrada');
        await expect(resultado).toHaveAttribute('role', 'status');
        await expect(resultado).toHaveClass('success');
        await expect(page.getByLabel('Nova senha')).toHaveValue('');
        await expect(page.getByLabel('Confirmar senha')).toHaveValue('');
    });

    test('rejeita quando a senha é válida mas a confirmação diverge', async ({ page }) => {
        await preencherECadastrar(page, 'Abcdef12', 'Abcdef13');

        const resultado = page.locator('#resultado');
        await expect(resultado).toHaveText('As senhas não coincidem');
        await expect(resultado).toHaveAttribute('role', 'alert');
    });
});
