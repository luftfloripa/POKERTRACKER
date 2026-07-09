# ♠️♥️ PokerTracker ♦️♣️

Bem-vindo ao **PokerTracker**, o seu assistente pessoal para gestão profissional de bankroll e torneios de Poker Online. 

Este sistema foi projetado para ajudar jogadores recreativos e regulares a saírem do escuro e passarem a ter controle absoluto sobre suas finanças e resultados (Profit/Loss, ROI, ABI) em múltiplas plataformas, com uma interface responsiva, elegante e intuitiva.


## 🚀 O que o projeto faz?

O PokerTracker não é apenas uma planilha; é um sistema completo de gestão de banca (Bankroll Management) e registro de histórico de torneios. Com ele, você consegue:

*   **Dashboards em Tempo Real:** Visualização instantânea de indicadores fundamentais como Profit (Lucro), ROI (Retorno sobre Investimento), ABI (Average Buy-in), Make Up e Saldo Total.
*   **Gestão Multiplataforma:** Controle de bancas independentes para PokerStars, GG Poker e Coin Poker (expansível).
*   **Evolução Visual (Gráfico):** Um gráfico interativo de linha do tempo que desenha a evolução do seu caixa (Bankroll) torneio a torneio.
*   **Caixa Flexível:** Registre depósitos e saques para cada plataforma de forma separada, mantendo o controle do "dinheiro real".
*   **Histórico e Filtros Inteligentes:** Busque seus torneios passados por período (datas) ou por plataforma específica, com paginação para melhor performance.
*   **Gestão de Usuários (Admin):** Módulo de administração para cadastrar novos jogadores, editar credenciais e realizar um *Hard Reset* na conta (limpando o histórico financeiro, mas mantendo o perfil ativo para um novo ciclo).


## 🛠️ Tecnologias Utilizadas

Este projeto foi construído utilizando uma arquitetura moderna e dividida entre Frontend e Backend, garantindo segurança e fluidez.

### 💻 Frontend (Client-Side)
*   **HTML5, CSS3 e JavaScript (Vanilla):** Sem frameworks pesados. Todo o layout responsivo, sistema de paginação, comunicação de API (Fetch) e renderização de modais foram construídos do zero para máxima performance e controle.
*   **Chart.js:** Biblioteca utilizada para a renderização do gráfico interativo de evolução da banca.
*   **PWA Standards:** Meta tags otimizadas para mobile e padrões modernos de navegação.

### ⚙️ Backend (Server-Side)
*   **Java 21:** A espinha dorsal do sistema, rodando com as features mais recentes da linguagem.
*   **Spring Boot 3.x:** Framework principal para injeção de dependências, controle das rotas da API (Controllers) e regras de negócio (Services).
*   **Spring Security & JWT (JSON Web Tokens):** Proteção rigorosa das rotas da API. Apenas usuários logados com um token válido conseguem ver ou alterar seus dados financeiros.
*   **Spring Data JPA / Hibernate:** Mapeamento objeto-relacional inteligente, com uso de consultas (JPQL) seguras para evitar conflitos de IDs no banco.
*   **Banco de Dados (H2 / MySQL):** Armazenamento relacional dos usuários, transações de caixa e registros de torneios.


## 💡 Destaques de Arquitetura (Por debaixo do capô)

*   **Linha do Tempo Perfeita (Time-stamping):** Implementação de `@PrePersist` no Java com `LocalDateTime` invisível. Isso garante que a ordenação de torneios e saques feitos *no mesmo dia* seja desempatada pelo milissegundo exato do clique, mantendo a matemática do Make Up 100% fiel à realidade.
*   **Segurança Anti-vazamento:** Validações robustas no Backend (Spring Validation) impedem que torneios sejam salvos se o saldo do jogador na plataforma específica for insuficiente.
*   **Tratamento de Exceções Global:** Sistema preparado para capturar e evitar o clássico *NullPointerException* (Erros 500), usando "datas âncora" para gerenciar registros legados ou corrompidos de forma silenciosa.
*   **UI/UX Mobile First:** Formulários responsivos, "Efeito Sanfona" (Accordion) nos menus de navegação e layout flexível (Flexbox/Grid) que se ajusta perfeitamente desde monitores Ultrawide até smartphones.


## 📥 Como rodar este projeto na sua máquina

Siga os passos abaixo para testar o PokerTracker localmente.

### Pré-requisitos
*   **Java Development Kit (JDK) 21** instalado.
*   **Maven** instalado (ou uso do wrapper embutido `mvnw`).
*   Uma IDE Java (IntelliJ IDEA, Eclipse ou VS Code).
*   Um navegador web moderno (Chrome, Edge, Firefox).

### Passo a Passo

1. **Clone o repositório:**
   ```bash
   git clone [https://github.com/LuftFloripa/PokerTracker.git](https://github.com/LuftFloripa/PokerTracker.git)

2. **Abra o projeto no Backend:**
Navegue até a pasta clonada e abra o projeto em sua IDE (IntelliJ recomendado).

3. **Execute o Servidor Spring Boot:**
Localize o arquivo principal PokerTrackerApplication.java e execute-o. O servidor será iniciado em http://localhost:8081 (verifique as portas no application.properties).

4. **Acesse o Frontend:**
O Frontend roda independentemente do Backend. Basta ir até a pasta onde estão os arquivos visuais (geralmente dentro de src/main/resources/static ou em uma pasta separada) e abrir o arquivo index.html no seu navegador, ou rodá-lo utilizando a extensão Live Server do VS Code.

5. **Acesso Inicial:**
Caso seja a primeira vez rodando (e com o ddl-auto=update ativado no Spring), você pode precisar registrar um usuário de teste ou injetar um Admin via banco de dados para acessar o sistema.

👨‍💻 **Autor**
Desenvolvido com foco em boas práticas de programação, lógica de negócios robusta e usabilidade intuitiva.

**Claudio Luft**
