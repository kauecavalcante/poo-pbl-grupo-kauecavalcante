async function api(metodo, caminho, corpo) {
    const resposta = await fetch(caminho, {
        method: metodo,
        headers: { 'Content-Type': 'application/json' },
        body: corpo ? JSON.stringify(corpo) : null
    });
    const texto = await resposta.text();
    const dados = texto ? JSON.parse(texto) : null;
    if (!resposta.ok) {
        const erro = new Error(dados?.mensagem || resposta.statusText);
        erro.status = resposta.status;
        erro.codigo = dados?.erro;
        throw erro;
    }
    return dados;
}

function exibirErro(elemento, e) {
    elemento.innerHTML = `<div class="erro">[${e.status} ${e.codigo || 'ERRO'}] ${e.message}</div>`;
}

function exibirSucesso(elemento, mensagem) {
    elemento.innerHTML = `<div class="sucesso">${mensagem}</div>`;
}

function limpar(elemento) {
    elemento.innerHTML = '';
}
