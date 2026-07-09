package br.com.pokertracker.service;

import br.com.pokertracker.model.Movimentacao;
import br.com.pokertracker.model.Torneio;
import br.com.pokertracker.repository.MovimentacaoRepository;
import br.com.pokertracker.repository.TorneioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimentacaoService {

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private TorneioRepository torneioRepository;

    public Movimentacao registrarMovimentacao(Movimentacao movimentacao) {
        if ("SAQUE".equalsIgnoreCase(movimentacao.getTipo())) {
            Long usuarioId = movimentacao.getUsuarioId();
            String plataforma = movimentacao.getPlataforma();

            // 1. Calcula lucro real (Profit dos torneios na plataforma)
            double lucroReal = calcularLucroReal(plataforma, usuarioId);

            // 2. Trava de Segurança: Não pode sacar mais que o lucro acumulado (Make-up positivo)
            if (movimentacao.getValor() > lucroReal) {
                throw new IllegalArgumentException(
                        "Saque negado: Você só pode sacar o valor correspondente ao seu lucro. " +
                                "Seu lucro atual na " + plataforma + " é de $ " + String.format("%.2f", lucroReal)
                );
            }
        }
        return movimentacaoRepository.save(movimentacao);
    }

    // Novo método auxiliar para calcular SOMENTE o lucro dos torneios
    private double calcularLucroReal(String plataforma, Long usuarioId) {
        List<Torneio> torneios = torneioRepository.findByUsuarioId(usuarioId);
        return torneios.stream()
                .filter(t -> plataforma.equalsIgnoreCase(t.getPlataforma()))
                .mapToDouble(t -> (t.getItm() != null ? t.getItm() : 0.0) -
                        ((t.getBuyIn() != null ? t.getBuyIn() : 0.0) +
                                (t.getRebuy() != null ? t.getRebuy() : 0.0) +
                                (t.getAddon() != null ? t.getAddon() : 0.0)))
                .sum();
    }

    // Método privado que isola o cálculo da matemática
    private double calcularBancaDaPlataforma(String plataforma, Long usuarioId) {
        List<Movimentacao> historicoCaixa = movimentacaoRepository.findByUsuarioId(usuarioId);
        List<Torneio> historicoTorneios = torneioRepository.findByUsuarioId(usuarioId);

        double totalDepositos = 0.0;
        double totalSaques = 0.0;
        double lucroTorneios = 0.0;

        // Soma Depósitos e Saques
        for (Movimentacao mov : historicoCaixa) {
            if (mov.getPlataforma().equals(plataforma)) {
                if ("DEPOSITO".equalsIgnoreCase(mov.getTipo())) {
                    totalDepositos += mov.getValor();
                } else if ("SAQUE".equalsIgnoreCase(mov.getTipo())) {
                    totalSaques += mov.getValor();
                }
            }
        }

        // Calcula o Profit/Prejuízo dos torneios
        for (Torneio t : historicoTorneios) {
            if (t.getPlataforma().equals(plataforma)) {
                double custos = t.getBuyIn() + (t.getRebuy() != null ? t.getRebuy() : 0) + (t.getAddon() != null ? t.getAddon() : 0);
                lucroTorneios += (t.getItm() - custos);
            }
        }

        // Banca = Depósitos - Saques + Lucro (ou Prejuízo)
        return totalDepositos - totalSaques + lucroTorneios;
    }
}