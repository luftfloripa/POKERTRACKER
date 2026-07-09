-- 1. Criação do Banco de Dados (Caso ainda não tenha feito)
CREATE DATABASE IF NOT EXISTS PI2;
USE PI2;

-- 2. Tabela de Usuários (Login e Roles)
CREATE TABLE IF NOT EXISTS tb_usuario (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
    );

-- 3. Tabela de Bancas (Saldos Iniciais por Plataforma)
CREATE TABLE IF NOT EXISTS tb_banca (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        saldo_inicial DOUBLE DEFAULT 0.0,
                                        plataforma VARCHAR(100)
    );

-- 4. Tabela de Torneios (Histórico de Jogos)
CREATE TABLE IF NOT EXISTS tb_torneio (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          descricao VARCHAR(255),
    plataforma VARCHAR(100),
    buy_in DOUBLE NOT NULL,
    rebuy DOUBLE DEFAULT 0.0,
    addon DOUBLE DEFAULT 0.0,
    itm DOUBLE DEFAULT 0.0,
    data DATE NOT NULL
    );

-- 5. Tabela de Movimentações (Depósitos e Saques)
CREATE TABLE IF NOT EXISTS tb_movimentacao (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               valor DOUBLE NOT NULL,
                                               tipo VARCHAR(20), -- DEPOSITO ou SAQUE
    plataforma VARCHAR(100),
    descricao VARCHAR(255),
    data DATE NOT NULL
    );