package br.com.pokertracker.service;

import br.com.pokertracker.dto.DashboardDTO;
import br.com.pokertracker.dto.HistoricoDTO;
import br.com.pokertracker.model.*;
import br.com.pokertracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class TorneioService {
    @Autowired private TorneioRepository torneioRepository;
    @Autowired private BancaRepository bancaRepository;
    @Autowired private MovimentacaoRepository movimentacaoRepository;

    public Torneio registrarTorneio(Torneio torneio) {
        double custos = (torneio.getBuyIn() != null ? torneio.getBuyIn() : 0.0) +
                (torneio.getRebuy() != null ? torneio.getRebuy() : 0.0) +
                (torneio.getAddon() != null ? torneio.getAddon() : 0.0);

        double saldoPlataforma = calcularBancaDaPlataforma(torneio.getPlataforma(), torneio.getUsuarioId());

        if (custos > saldoPlataforma) {
            throw new IllegalArgumentException("Saldo insuficiente na plataforma " + torneio.getPlataforma());
        }
        return torneioRepository.save(torneio);
    }

    public Torneio atualizarTorneio(Long id, Torneio tAtualizado) {
        Torneio tAntigo = torneioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Torneio não encontrado."));

        double custoAntigo = (tAntigo.getBuyIn() != null ? tAntigo.getBuyIn() : 0.0) + (tAntigo.getRebuy() != null ? tAntigo.getRebuy() : 0.0) + (tAntigo.getAddon() != null ? tAntigo.getAddon() : 0.0);
        double custoNovo = (tAtualizado.getBuyIn() != null ? tAtualizado.getBuyIn() : 0.0) + (tAtualizado.getRebuy() != null ? tAtualizado.getRebuy() : 0.0) + (tAtualizado.getAddon() != null ? tAtualizado.getAddon() : 0.0);

        if (custoNovo > custoAntigo) {
            double saldo = calcularBancaDaPlataforma(tAtualizado.getPlataforma(), tAtualizado.getUsuarioId());
            if ((custoNovo - custoAntigo) > saldo) throw new IllegalArgumentException("Saldo insuficiente para atualização.");
        }

        tAntigo.setDescricao(tAtualizado.getDescricao());
        tAntigo.setPlataforma(tAtualizado.getPlataforma());
        tAntigo.setBuyIn(tAtualizado.getBuyIn());
        tAntigo.setRebuy(tAtualizado.getRebuy());
        tAntigo.setAddon(tAtualizado.getAddon());
        tAntigo.setItm(tAtualizado.getItm());
        tAntigo.setData(tAtualizado.getData());
        return torneioRepository.save(tAntigo);
    }

    public void deletarTorneio(Long id) {
        torneioRepository.deleteById(id);
    }

    private double calcularBancaDaPlataforma(String plataforma, Long usuarioId) {
        double bancaInicial = bancaRepository.findByUsuarioId(usuarioId).stream()
                .filter(b -> plataforma.equalsIgnoreCase(b.getPlataforma()))
                .mapToDouble(b -> b.getSaldoInicial() != null ? b.getSaldoInicial() : 0.0).sum();

        List<Movimentacao> movs = movimentacaoRepository.findByUsuarioId(usuarioId);
        double movPlat = movs.stream().filter(m -> plataforma.equalsIgnoreCase(m.getPlataforma()))
                .mapToDouble(m -> "DEPOSITO".equalsIgnoreCase(m.getTipo()) ? m.getValor() : -m.getValor()).sum();

        List<Torneio> tns = torneioRepository.findByUsuarioId(usuarioId);
        double lucroPlat = tns.stream().filter(t -> plataforma.equalsIgnoreCase(t.getPlataforma()))
                .mapToDouble(t -> (t.getItm()!=null?t.getItm():0) - ((t.getBuyIn()!=null?t.getBuyIn():0) + (t.getRebuy()!=null?t.getRebuy():0) + (t.getAddon()!=null?t.getAddon():0))).sum();

        return bancaInicial + movPlat + lucroPlat;
    }

    public DashboardDTO calcularIndicadores(Long usuarioId, String plataforma) {
        List<Torneio> torneios = torneioRepository.findByUsuarioId(usuarioId);
        List<Movimentacao> movs = movimentacaoRepository.findByUsuarioId(usuarioId);

        // Aplica o filtro de plataforma SE o usuário clicou na caixinha
        if (plataforma != null && !plataforma.isEmpty()) {
            torneios = torneios.stream().filter(t -> t.getPlataforma().equalsIgnoreCase(plataforma)).toList();
            movs = movs.stream().filter(m -> plataforma.equalsIgnoreCase(m.getPlataforma())).toList();
        }

        double totalDepositos = movs.stream().filter(m -> "DEPOSITO".equalsIgnoreCase(m.getTipo())).mapToDouble(Movimentacao::getValor).sum();
        double totalSaques = movs.stream().filter(m -> "SAQUE".equalsIgnoreCase(m.getTipo())).mapToDouble(Movimentacao::getValor).sum();
        double custos = torneios.stream().mapToDouble(t -> (t.getBuyIn()!=null?t.getBuyIn():0) + (t.getRebuy()!=null?t.getRebuy():0) + (t.getAddon()!=null?t.getAddon():0)).sum();
        double premiacoes = torneios.stream().mapToDouble(t -> t.getItm() != null ? t.getItm() : 0).sum();
        double lucroTotal = premiacoes - custos;

        double makeUp = Math.max(0, lucroTotal - totalSaques);
        double valorBanca = bancaRepository.findByUsuarioId(usuarioId).stream().mapToDouble(b -> b.getSaldoInicial() != null ? b.getSaldoInicial() : 0.0).sum() + lucroTotal + (totalDepositos - totalSaques);
        double abi = torneios.isEmpty() ? 0.0 : custos / torneios.size();
        double roi = custos == 0 ? 0.0 : (lucroTotal / custos) * 100;

        // RESTAURANDO A LOGICA DAS BANCAS INDIVIDUAIS
        Map<String, Double> bancasPorPlataforma = new HashMap<>();
        Set<String> plataformasAtivas = new HashSet<>();
        bancaRepository.findByUsuarioId(usuarioId).forEach(b -> { if (b.getPlataforma() != null) plataformasAtivas.add(b.getPlataforma()); });
        torneioRepository.findByUsuarioId(usuarioId).forEach(t -> plataformasAtivas.add(t.getPlataforma()));
        movimentacaoRepository.findByUsuarioId(usuarioId).forEach(m -> { if (m.getPlataforma() != null) plataformasAtivas.add(m.getPlataforma()); });

        for (String plat : plataformasAtivas) {
            if (plat == null || plat.isEmpty()) continue;
            double saldoP = calcularBancaDaPlataforma(plat, usuarioId);
            bancasPorPlataforma.put(plat, saldoP);
        }

        return new DashboardDTO(roi, abi, lucroTotal, makeUp, valorBanca, (long) torneios.size(), bancasPorPlataforma);
    }

    public Page<HistoricoDTO> obterHistoricoUnificado(Long usuarioId, LocalDate inicio, LocalDate fim, String plataforma, int page, int size) {
        List<Torneio> torneios = torneioRepository.findByUsuarioId(usuarioId);
        List<Movimentacao> movs = movimentacaoRepository.findByUsuarioId(usuarioId);

        if (inicio != null && fim != null) {
            torneios = torneios.stream().filter(t -> !t.getData().isBefore(inicio) && !t.getData().isAfter(fim)).toList();
            movs = movs.stream().filter(m -> !m.getData().isBefore(inicio) && !m.getData().isAfter(fim)).toList();
        }

        if (plataforma != null && !plataforma.isEmpty()) {
            torneios = torneios.stream().filter(t -> t.getPlataforma().equalsIgnoreCase(plataforma)).toList();
            movs = movs.stream().filter(m -> m.getPlataforma() != null && m.getPlataforma().equalsIgnoreCase(plataforma)).toList();
        }

        List<HistoricoDTO> lista = new ArrayList<>();
        for(Torneio t : torneios) {
            double c = (t.getBuyIn()!=null?t.getBuyIn():0) + (t.getRebuy()!=null?t.getRebuy():0) + (t.getAddon()!=null?t.getAddon():0);
            lista.add(new HistoricoDTO(t.getId(), t.getData(), t.getDataRegistro(), t.getDescricao(), t.getPlataforma(), c, t.getItm(), t.getItm() - c, "TORNEIO"));
        }
        for(Movimentacao m : movs) {
            double v = "DEPOSITO".equalsIgnoreCase(m.getTipo()) ? m.getValor() : -m.getValor();
            lista.add(new HistoricoDTO(m.getId(), m.getData(), m.getDataRegistro(), m.getTipo(), m.getPlataforma(), 0.0, 0.0, v, "MOVIMENTACAO"));
        }

        lista.sort((a, b) -> {
            int compData = b.getData().compareTo(a.getData());
            if(compData != 0) return compData;
            if(a.getDataRegistro() != null && b.getDataRegistro() != null) return b.getDataRegistro().compareTo(a.getDataRegistro());
            return b.getId().compareTo(a.getId());
        });

        // PAGINAÇÃO REAL DE 10 ITENS
        int start = Math.min(page * size, lista.size());
        int end = Math.min(start + size, lista.size());
        return new PageImpl<>(lista.subList(start, end), PageRequest.of(page, size), lista.size());
    }
}