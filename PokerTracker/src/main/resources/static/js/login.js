document.getElementById('formLogin').addEventListener('submit', function(e) {
    e.preventDefault();

    const msgErro = document.getElementById('msgErro');
    msgErro.classList.add('hidden'); // Esconde o erro antes de tentar novamente

    const dados = {
        email: document.getElementById('email').value,
        senha: document.getElementById('senha').value
    };

    fetch('http://localhost:8081/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dados)
    })
        .then(res => {
            if (res.ok) return res.json();
            throw new Error("Credenciais inválidas");
        })
        .then(dadosRetornados => {
            // A MÁGICA: Guarda o token de segurança no navegador
            localStorage.setItem('token', dadosRetornados.token);

            // Guarda o usuário como já fazia antes
            localStorage.setItem('usuarioLogado', JSON.stringify(dadosRetornados.usuario));

            window.location.href = '/index.html';
        })
        .catch(err => {
            msgErro.classList.remove('hidden'); // Mostra o erro usando a classe do CSS
            console.error("Erro no login:", err);
        });
});