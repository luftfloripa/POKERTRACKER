/* ==========================================
   1. ESTADO GLOBAL E INICIALIZAÇÃO
   ========================================== */
let usuarioAtual = null;
let tokenAtual = null;
let meuGrafico = null;
let torneiosParaExportacao = [];
let torneiosParaTabela = [];
let lucroAtualGlobal = 0;
let paginaAtual = 0;
let totalPaginas = 0;
let todosUsuarios = [];
let paginaAtualUsuarios = 0;
const itensPorPaginaUsuarios = 10;

document.addEventListener('DOMContentLoaded', () => {
    iniciarAplicacao();
});

function iniciarAplicacao() {
    usuarioAtual = JSON.parse(localStorage.getItem('usuarioLogado'));
    tokenAtual = localStorage.getItem('token');

    if (!usuarioAtual || !tokenAtual) {
        mostrarTela('tela-login');
        configurarFormLogin();
    } else {
        mostrarTela('tela-app');
        configurarSessaoUsuario();
        navegarPara('dashboard');
    }
}

/* ==========================================
   2. CONTROLE DE TELAS (SPA E ROTEAMENTO)
   ========================================== */
function mostrarTela(idTelaDesejada) {
    document.getElementById('tela-login').classList.add('hidden');
    document.getElementById('tela-app').classList.add('hidden');

    const tela = document.getElementById(idTelaDesejada);
    if(tela) tela.classList.remove('hidden');
}

function navegarPara(viewDesejada) {
    document.querySelectorAll('.view-section').forEach(el => el.classList.add('hidden'));

    const viewAtiva = document.getElementById(`view-${viewDesejada}`);
    if (viewAtiva) viewAtiva.classList.remove('hidden');

    const navMenu = document.getElementById('navMenu');
    if (navMenu && navMenu.classList.contains('active')) {
        navMenu.classList.remove('active');
    }

    if (viewDesejada === 'usuarios') {
        carregarListaUsuarios();
    } else if (viewDesejada === 'perfil') {
        preencherDadosPerfil();
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function toggleMenu() {
    const navMenu = document.getElementById('navMenu');
    navMenu.classList.toggle('active');
}

/* ==========================================
   3. AUTENTICAÇÃO E SEGURANÇA
   ========================================== */
function getHeaders() {
    return { 'Content-Type': 'application/json', 'Authorization': `Bearer ${tokenAtual}` };
}

function verificarSessao(response) {
    if (response.status === 401 || response.status === 403) {
        fazerLogout();
        throw new Error("Sessão expirada ou acesso negado.");
    }
    return response;
}

function configurarFormLogin() {
    const formLogin = document.getElementById('formLogin');
    if (formLogin) {
        formLogin.addEventListener('submit', function(e) {
            e.preventDefault(); // MÁGICA: Impede a página de recarregar e colocar o "?" na URL

            const btn = this.querySelector('.btn-login');
            const txtOriginal = btn.innerText;
            btn.disabled = true;
            btn.innerText = 'CARREGANDO...';

            const email = document.getElementById('email').value;
            const senha = document.getElementById('senha').value;
            const msgErro = document.getElementById('msgErro');

            fetch('/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, senha })
            })
                .then(res => {
                    if (!res.ok) throw new Error("Credenciais inválidas");
                    return res.json();
                })
                .then(dados => {
                    // Guarda o passaporte VIP
                    localStorage.setItem('token', dados.token);
                    localStorage.setItem('usuarioLogado', JSON.stringify(dados.usuario));
                    if (msgErro) msgErro.classList.add('hidden');

                    // Inicia o app e esconde a tela de login
                    iniciarAplicacao();
                })
                .catch(err => {
                    if (msgErro) msgErro.classList.remove('hidden');
                })
                .finally(() => {
                    btn.disabled = false;
                    btn.innerText = txtOriginal;
                });
        });
    }
}

function configurarSessaoUsuario() {
    const elNome = document.getElementById('nomeUsuarioLogado');
    if (elNome) elNome.innerText = `Olá, ${usuarioAtual.nome}`;

    if (usuarioAtual.role === 'ADMIN') {
        document.getElementById('linkAdmin')?.classList.remove('hidden');
    } else {
        document.getElementById('linkAdmin')?.classList.add('hidden');
    }

    configurarFormulariosDashboard();
    configurarFormCadastroAdmin();
    configurarFormPerfil();
    configurarFormEdicaoUsuario();
    carregarTudo();
}

function fazerLogout() {
    localStorage.clear();
    usuarioAtual = null;
    tokenAtual = null;
    location.reload();
}

/* ==========================================
   4. SISTEMA DE NOTIFICAÇÕES (TOAST)
   ========================================== */
function mostrarToast(mensagem, tipo = 'sucesso') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = `toast ${tipo}`;
    toast.innerText = mensagem;
    container.appendChild(toast);
    setTimeout(() => toast.classList.add('show'), 10);
    setTimeout(() => { toast.classList.remove('show'); setTimeout(() => toast.remove(), 300); }, 3500);
}

/* ==========================================
   5. DASHBOARD E GRÁFICOS
   ========================================== */
function carregarTudo() {
    const inicio = document.getElementById('filtroDataInicio')?.value || '';
    const fim = document.getElementById('filtroDataFim')?.value || '';
    const plataforma = document.getElementById('filtroPlataforma')?.value || '';

    carregarDashboard(inicio, fim, plataforma);
    carregarGrafico(inicio, fim, plataforma);
    carregarTabelaPaginada(true);
}

function limparFiltros() {
    document.getElementById('filtroDataInicio').value = '';
    document.getElementById('filtroDataFim').value = '';
    document.getElementById('filtroPlataforma').value = '';
    carregarTudo();
}

function limparFiltrosEAnimar() {
    limparFiltros();
    const grafico = document.querySelector('.container-grafico');
    if (grafico) grafico.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function filtrarPorPlataformaRapido(plataforma) {
    const select = document.getElementById('filtroPlataforma');
    if (select) select.value = plataforma;
    carregarTudo();
    const grafico = document.querySelector('.container-grafico');
    if (grafico) grafico.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function carregarDashboard(dataInicio = '', dataFim = '', plataforma = '') {
    const params = new URLSearchParams();
    params.append('usuarioId', usuarioAtual.id);
    if (dataInicio) params.append('dataInicio', dataInicio);
    if (dataFim) params.append('dataFim', dataFim);
    if (plataforma) params.append('plataforma', plataforma);

    fetch(`/torneios/dashboard?${params.toString()}`, { headers: getHeaders() })
        .then(verificarSessao).then(res => res.json())
        .then(dados => {
            lucroAtualGlobal = dados.lucroTotal;
            document.getElementById('valorBanca').innerText = `$ ${dados.valorBanca.toFixed(2)}`;
            document.getElementById('lucroTotal').innerText = `$ ${dados.lucroTotal.toFixed(2)}`;
            document.getElementById('roi').innerText = `${dados.roi.toFixed(2)}%`;
            document.getElementById('abi').innerText = `$ ${dados.abi.toFixed(2)}`;

            const elMakeUp = document.getElementById('makeUp');
            if (dados.makeUp < 0) {
                elMakeUp.innerText = `$ ${dados.makeUp.toFixed(2)}`;
                elMakeUp.className = 'valor prejuizo';
            } else if (dados.makeUp > 0) {
                elMakeUp.innerText = `+ $ ${dados.makeUp.toFixed(2)}`;
                elMakeUp.className = 'valor lucro';
            } else {
                elMakeUp.innerText = `$ 0.00`;
                elMakeUp.className = 'valor';
            }

            document.getElementById('lucroTotal').className = `valor ${dados.lucroTotal < 0 ? 'prejuizo' : 'lucro'}`;
            document.getElementById('roi').className = `valor ${dados.lucroTotal < 0 ? 'prejuizo' : 'lucro'}`;
            document.getElementById('valorBanca').className = `valor ${dados.valorBanca < 0 ? 'prejuizo' : 'lucro'}`;
            document.getElementById('abi').className = `valor ${dados.abi < 0 ? 'prejuizo' : 'lucro'}`;

            if (dados.bancasPorPlataforma) {
                // Zera visualmente
                ['pokerstars', 'ggpoker', 'coinpoker'].forEach(p => {
                    const el = document.getElementById(`banca-${p}`);
                    if (el) el.innerText = '$ 0.00';
                });

                Object.keys(dados.bancasPorPlataforma).forEach(plat => {
                    const id = `banca-${plat.toLowerCase().replace(/\s+/g, '')}`;
                    const el = document.getElementById(id);
                    if (el) el.innerText = `$ ${dados.bancasPorPlataforma[plat].toFixed(2)}`;
                });
            }
        });
}

function carregarGrafico(dataInicio = '', dataFim = '', plataforma = '') {
    const fetchTorneios = fetch(`/torneios?usuarioId=${usuarioAtual.id}`, { headers: getHeaders() }).then(res => res.json());
    const fetchMovimentacoes = fetch(`/movimentacoes?usuarioId=${usuarioAtual.id}`, { headers: getHeaders() }).then(res => res.json());

    Promise.all([fetchTorneios, fetchMovimentacoes]).then(([torneios, movimentacoes]) => {
        torneios.forEach(t => t.isTorneio = true);
        movimentacoes.forEach(m => m.isMovimentacao = true);

        let lista = [...torneios, ...movimentacoes];
        if (dataInicio && dataFim) lista = lista.filter(item => item.data >= dataInicio && item.data <= dataFim);
        if (plataforma) lista = lista.filter(item => item.plataforma === plataforma);

        torneiosParaExportacao = lista;
        const listaGrafico = [...lista].sort((a, b) => new Date(a.data) - new Date(b.data));
        atualizarGrafico(listaGrafico);
    });
}

function atualizarGrafico(lista) {
    const canvas = document.getElementById('graficoBanca');
    if (!canvas) return;

    let labels = ['Início'];
    let valores = [0];
    let saldoAcumulado = 0;

    lista.forEach(item => {
        if (item.isMovimentacao) {
            saldoAcumulado += item.tipo === 'DEPOSITO' ? item.valor : -item.valor;
        } else if (item.isTorneio) {
            const custos = (item.buyIn || 0) + (item.rebuy || 0) + (item.addon || 0);
            saldoAcumulado += (item.itm || 0) - custos;
        }
        labels.push(new Date(item.data).toLocaleDateString('pt-BR', {timeZone: 'UTC'}));
        valores.push(saldoAcumulado);
    });

    if (meuGrafico) meuGrafico.destroy();
    meuGrafico = new Chart(canvas.getContext('2d'), {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Evolução da Banca ($)',
                data: valores, fill: true, tension: 0.2, pointRadius: 2, pointHoverRadius: 5,
                segment: {
                    borderColor: ctx => ctx.p1.parsed.y < 0 ? '#e74c3c' : '#2ecc71',
                    backgroundColor: ctx => ctx.p1.parsed.y < 0 ? 'rgba(231, 76, 60, 0.1)' : 'rgba(46, 204, 113, 0.1)'
                }
            }]
        },
        options: { responsive: true, maintainAspectRatio: false }
    });
}

function exportarCSV() {
    if (!torneiosParaExportacao || torneiosParaExportacao.length === 0) {
        mostrarToast("Não há dados para exportar.", "erro"); return;
    }
    let csvContent = "Data;Descricao;Plataforma;Custos ($);ITM ($);Resultado ($)\n";
    const listaOrdenada = [...torneiosParaExportacao].sort((a, b) => new Date(b.data) - new Date(a.data));

    listaOrdenada.forEach(item => {
        const data = new Date(item.data).toLocaleDateString('pt-BR', {timeZone: 'UTC'});
        if (item.isTorneio) {
            const custos = (item.buyIn + (item.rebuy || 0) + (item.addon || 0)).toFixed(2);
            const resultado = ((item.itm || 0) - (item.buyIn + (item.rebuy || 0) + (item.addon || 0))).toFixed(2);
            csvContent += `${data};${item.descricao.replace(/;/g, "")};${item.plataforma};${custos};${(item.itm || 0).toFixed(2)};${resultado}\n`;
        } else {
            const isDeposito = item.tipo === 'DEPOSITO';
            csvContent += `${data};${isDeposito ? "DEPÓSITO" : "SAQUE"};${item.plataforma};-;-;${isDeposito ? item.valor.toFixed(2) : (-item.valor).toFixed(2)}\n`;
        }
    });

    const blob = new Blob(["\uFEFF" + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = `PokerTracker_Relatorio_${new Date().toLocaleDateString('pt-BR').replace(/\//g, '-')}.csv`;
    link.click();
}

/* ==========================================
   6. TABELA E PAGINAÇÃO (HISTÓRICO)
   ========================================== */
function carregarTabelaPaginada(novaBusca = false) {
    if (novaBusca) paginaAtual = 0;

    const size = 10;
    const inicio = document.getElementById('filtroDataInicio')?.value || '';
    const fim = document.getElementById('filtroDataFim')?.value || '';
    const plataforma = document.getElementById('filtroPlataforma')?.value || '';

    const params = new URLSearchParams();
    params.append('usuarioId', usuarioAtual.id);
    params.append('page', paginaAtual);
    params.append('size', size);
    if (inicio) params.append('dataInicio', inicio);
    if (fim) params.append('dataFim', fim);
    if (plataforma) params.append('plataforma', plataforma);

    fetch(`/torneios/historico?${params.toString()}`, { headers: getHeaders() })
        .then(verificarSessao).then(res => res.json())
        .then(pageData => {
            torneiosParaTabela = pageData.content || [];

            // 👇 A MÁGICA RESTAURADA AQUI 👇
            // O JavaScript agora sabe ler a paginação no formato novo do Spring Boot 3.3
            if (pageData.page && pageData.page.totalPages !== undefined) {
                totalPaginas = pageData.page.totalPages;
            } else if (pageData.totalPages !== undefined) {
                totalPaginas = pageData.totalPages;
            } else {
                totalPaginas = 1;
            }

            renderizarTabela(torneiosParaTabela);
            atualizarBotoesPaginacao();
        });
}

function renderizarTabela(lista) {
    const corpo = document.getElementById('corpoTabela');
    if (!corpo) return;
    corpo.innerHTML = lista.map(item => {
        const dataFormatada = new Date(item.data).toLocaleDateString('pt-BR', {timeZone: 'UTC'});
        if (item.tipoItem === 'TORNEIO') {
            return `<tr>
                <td>${dataFormatada}</td><td title="${item.descricao}">${item.descricao}</td><td>${item.plataforma}</td>
                <td>$ ${item.custoTotal.toFixed(2)}</td><td>$ ${item.itm.toFixed(2)}</td>
                <td class="${item.valorResultado >= 0 ? 'lucro' : 'prejuizo'}">$ ${item.valorResultado.toFixed(2)}</td>
                <td><button onclick="editarTorneio(${item.id})" class="btn-editar">✏️</button></td>
                <td><button onclick="deletarTorneio(${item.id})" class="btn-excluir">🗑️</button></td>
            </tr>`;
        } else {
            const isDeposito = item.valorResultado >= 0;
            return `<tr style="background-color: rgba(0,0,0,0.04);">
                <td>${dataFormatada}</td><td style="font-weight: bold;">${isDeposito ? '🟢 DEPÓSITO' : '🔴 SAQUE'}</td>
                <td>${item.plataforma}</td><td>-</td><td>-</td>
                <td class="${isDeposito ? 'lucro' : 'prejuizo'}">$ ${item.valorResultado.toFixed(2)}</td>
                <td></td><td></td>
            </tr>`;
        }
    }).join('');
}

function mudarPagina(direcao) {
    if (paginaAtual + direcao >= 0 && paginaAtual + direcao < totalPaginas) {
        paginaAtual += direcao;
        carregarTabelaPaginada(false);
    }
}

function atualizarBotoesPaginacao() {
    const btnAnt = document.getElementById('btnPaginaAnterior');
    const btnProx = document.getElementById('btnPaginaProxima');
    const info = document.getElementById('infoPagina');
    if (btnAnt && btnProx && info) {
        btnAnt.disabled = paginaAtual === 0;
        btnProx.disabled = paginaAtual >= totalPaginas - 1;
        info.innerText = `Página ${totalPaginas === 0 ? 1 : paginaAtual + 1} de ${totalPaginas === 0 ? 1 : totalPaginas}`;
    }
}

/* ==========================================
   7. FORMULÁRIOS DE REGISTRO
   ========================================== */
function toggleAccordion(idContent, elementHeader) {
    const content = document.getElementById(idContent);
    elementHeader.classList.toggle('active');
    content.style.maxHeight = content.style.maxHeight ? null : content.scrollHeight + "px";
}

function configurarFormulariosDashboard() {
    const formTorneio = document.getElementById('formTorneio');
    if (formTorneio) {
        formTorneio.addEventListener('submit', function(e) {
            e.preventDefault();
            const dados = Object.fromEntries(new FormData(this).entries());
            dados.usuarioId = usuarioAtual.id;

            const btn = document.getElementById('btnSalvarTorneio');
            const txtOr = btn.innerText;
            btn.disabled = true; btn.innerText = 'SALVANDO...';

            fetch('/torneios', { method: 'POST', headers: getHeaders(), body: JSON.stringify(dados) })
                .then(async res => { if(!res.ok) throw new Error(await res.text()); mostrarToast("Salvo com sucesso!"); this.reset(); carregarTudo(); })
                .catch(err => mostrarToast(err.message || "Erro ao salvar", "erro"))
                .finally(() => { btn.disabled = false; btn.innerText = txtOr; });
        });
    }

    const formCaixa = document.getElementById('formCaixa');
    if (formCaixa) {
        formCaixa.addEventListener('submit', function(e) {
            e.preventDefault();
            const dados = Object.fromEntries(new FormData(this).entries());
            dados.usuarioId = usuarioAtual.id;

            const btn = this.querySelector('button[type="submit"]');
            btn.disabled = true;

            fetch('/movimentacoes', { method: 'POST', headers: getHeaders(), body: JSON.stringify(dados) })
                .then(async res => { if(!res.ok) throw new Error(await res.text()); mostrarToast("Registrado!"); this.reset(); carregarTudo(); })
                .catch(err => mostrarToast(err.message, "erro"))
                .finally(() => btn.disabled = false);
        });
    }
}

function editarTorneio(id) {
    fetch(`/torneios?usuarioId=${usuarioAtual.id}`, { headers: getHeaders() })
        .then(res => res.json())
        .then(lista => {
            const torneio = lista.find(t => t.id === id);
            if (!torneio) return;

            const form = document.getElementById('formTorneio');
            const contentTorneio = document.getElementById('content-torneio');
            if(!contentTorneio.previousElementSibling.classList.contains('active')) toggleAccordion('content-torneio', contentTorneio.previousElementSibling);

            form.elements['id'].value = torneio.id;
            form.elements['descricao'].value = torneio.descricao;
            form.elements['plataforma'].value = torneio.plataforma;
            form.elements['buyIn'].value = torneio.buyIn;
            form.elements['rebuy'].value = torneio.rebuy || '';
            form.elements['addon'].value = torneio.addon || '';
            form.elements['itm'].value = torneio.itm || 0;
            form.elements['data'].value = new Date(torneio.data).toISOString().split('T')[0];

            document.getElementById('btnSalvarTorneio').innerText = 'ATUALIZAR';
            document.getElementById('btnCancelarEdicao').classList.remove('hidden');
            form.scrollIntoView({ behavior: 'smooth', block: 'center' });
        });
}

function cancelarEdicao() {
    const form = document.getElementById('formTorneio');
    form.reset(); form.elements['id'].value = '';
    document.getElementById('btnSalvarTorneio').innerText = 'SALVAR';
    document.getElementById('btnCancelarEdicao').classList.add('hidden');
}

function deletarTorneio(id) {
    if (confirm("Deseja excluir este registro? 🗑️")) {
        fetch(`/torneios/${id}`, { method: 'DELETE', headers: getHeaders() }).then(() => { mostrarToast("Excluído!"); carregarTudo(); });
    }
}

/* ==========================================
   8. GESTÃO DE CLIENTES
   ========================================== */
function carregarListaUsuarios() {
    fetch('/auth/usuarios', { headers: getHeaders() }).then(res => res.json()).then(lista => {
        todosUsuarios = lista;
        paginaAtualUsuarios = 0;
        renderizarTabelaUsuariosPaginada();
    });
}

function renderizarTabelaUsuariosPaginada() {
    const inicio = paginaAtualUsuarios * itensPorPaginaUsuarios;
    const tbody = document.getElementById('tabelaUsuarios');
    if (!tbody) return;
    tbody.innerHTML = todosUsuarios.slice(inicio, inicio + itensPorPaginaUsuarios).map(u => `
        <tr><td><strong>${u.nome}</strong></td><td>${u.email}</td><td>${u.role}</td>
        <td>
            ${u.id !== 1 ? `<button onclick="abrirModalEdicao(${u.id}, '${u.nome}')" class="btn-editar" title="Editar">✏️</button>` : ''}
            ${u.id !== 1 ? `<button onclick="resetarDadosUsuario(${u.id})" class="btn-editar" title="Zerar Histórico">🧹</button>` : ''}
            ${u.id !== 1 ? `<button onclick="deletarUsuario(${u.id})" class="btn-excluir" title="Excluir Definitivamente">🗑️</button>` : '<span style="color:#999">Protegido</span>'}
        </td></tr>
    `).join('');
    atualizarBotoesPaginacaoUsuarios();
}

function mudarPaginaUsuarios(direcao) {
    const total = Math.ceil(todosUsuarios.length / itensPorPaginaUsuarios);
    if (paginaAtualUsuarios + direcao >= 0 && paginaAtualUsuarios + direcao < total) {
        paginaAtualUsuarios += direcao;
        renderizarTabelaUsuariosPaginada();
    }
}

function atualizarBotoesPaginacaoUsuarios() {
    const total = Math.ceil(todosUsuarios.length / itensPorPaginaUsuarios) || 1;
    document.getElementById('btnPaginaAnteriorUsuarios').disabled = paginaAtualUsuarios === 0;
    document.getElementById('btnPaginaProximaUsuarios').disabled = paginaAtualUsuarios >= total - 1;
    document.getElementById('infoPaginaUsuarios').innerText = `Página ${paginaAtualUsuarios + 1} de ${total}`;
}

function configurarFormCadastroAdmin() {
    const formCadastro = document.getElementById('formCadastroUsuario');
    if (formCadastro) {
        formCadastro.addEventListener('submit', function(e) {
            e.preventDefault();
            const dados = { nome: document.getElementById('novoNome').value, email: document.getElementById('novoEmail').value, senha: document.getElementById('novaSenhaAdmin').value, role: document.getElementById('novoRole').value };
            fetch('/auth/cadastro', { method: 'POST', headers: getHeaders(), body: JSON.stringify(dados) })
                .then(res => { if(!res.ok) throw new Error(); mostrarToast("Cliente cadastrado!"); this.reset(); carregarListaUsuarios(); })
                .catch(() => mostrarToast("Erro: E-mail já existe.", "erro"));
        });
    }
}

function deletarUsuario(id) {
    if (confirm("Tem certeza que deseja excluir este cliente?")) {
        fetch(`/auth/usuarios/${id}`, { method: 'DELETE', headers: getHeaders() }).then(() => { mostrarToast("Excluído!"); carregarListaUsuarios(); });
    }
}

function resetarDadosUsuario(id) {
    if (confirm("⚠️ Isso vai apagar TODOS os torneios e o saldo deste jogador. Tem certeza?")) {
        fetch(`/auth/usuarios/${id}/reset`, { method: 'DELETE', headers: getHeaders() })
            .then(() => { mostrarToast("Histórico varrido! 🧹"); if (usuarioAtual.id === id) carregarTudo(); });
    }
}

/* ==========================================
   9. EDIÇÃO DE PERFIL E MODAL
   ========================================== */
function preencherDadosPerfil() {
    const nome = document.getElementById('perfilNome');
    const email = document.getElementById('perfilEmail');
    if (nome && usuarioAtual) nome.value = usuarioAtual.nome;
    if (email && usuarioAtual) email.value = usuarioAtual.email;
    const n = document.getElementById('novaSenhaPerfil'), c = document.getElementById('confirmaSenhaPerfil');
    if (n) n.value = ''; if (c) c.value = '';
}

function configurarFormPerfil() {
    const formPerfil = document.getElementById('formPerfil');
    if (formPerfil) {
        formPerfil.addEventListener('submit', function(e) {
            e.preventDefault();
            const senha = document.getElementById('novaSenhaPerfil').value;
            const confirma = document.getElementById('confirmaSenhaPerfil').value;
            if (senha && senha !== confirma) return mostrarToast("As senhas não conferem!", "erro");

            const dados = { nome: document.getElementById('perfilNome').value, email: usuarioAtual.email, role: usuarioAtual.role };
            if (senha) dados.senha = senha;

            fetch(`/auth/usuarios/${usuarioAtual.id}`, { method: 'PUT', headers: getHeaders(), body: JSON.stringify(dados) })
                .then(res => {
                    usuarioAtual.nome = dados.nome;
                    localStorage.setItem('usuarioLogado', JSON.stringify(usuarioAtual));
                    document.getElementById('nomeUsuarioLogado').innerText = `Olá, ${usuarioAtual.nome}`;
                    mostrarToast("Perfil atualizado!");
                });
        });
    }
}

function abrirModalEdicao(id, nomeAtual) {
    document.getElementById('editUsuarioId').value = id;
    document.getElementById('editNome').value = nomeAtual;
    document.getElementById('editSenha').value = '';
    document.getElementById('modalEditarUsuario').classList.remove('hidden');
}

function fecharModalEdicao() { document.getElementById('modalEditarUsuario').classList.add('hidden'); }

function configurarFormEdicaoUsuario() {
    const formEditar = document.getElementById('formEditarUsuario');
    if (formEditar) {
        formEditar.addEventListener('submit', function(e) {
            e.preventDefault();
            const id = document.getElementById('editUsuarioId').value;
            const senha = document.getElementById('editSenha').value;
            const dados = { nome: document.getElementById('editNome').value };
            if (senha) dados.senha = senha;

            fetch(`/auth/usuarios/${id}`, { method: 'PUT', headers: getHeaders(), body: JSON.stringify(dados) })
                .then(() => { mostrarToast("Cliente atualizado!"); fecharModalEdicao(); carregarListaUsuarios(); });
        });
    }
}