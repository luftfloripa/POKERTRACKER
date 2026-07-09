package br.com.pokertracker.controller;

import br.com.pokertracker.dto.DashboardDTO;
import br.com.pokertracker.dto.HistoricoDTO;
import br.com.pokertracker.model.*;
import br.com.pokertracker.repository.*;
import br.com.pokertracker.service.TorneioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/torneios")
public class TorneioController {

    @Autowired private TorneioRepository repository;
    @Autowired private TorneioService service;
    @Autowired private BancaRepository bancaRepository;

    @PostMapping("/banca")
    public Banca definirBanca(@RequestBody Banca novaBanca) {
        Banca bancaExistente = bancaRepository.findByUsuarioId(novaBanca.getUsuarioId()).stream()
                .filter(b -> b.getPlataforma() != null && b.getPlataforma().equalsIgnoreCase(novaBanca.getPlataforma()))
                .findFirst().orElse(new Banca());
        bancaExistente.setSaldoInicial(novaBanca.getSaldoInicial());
        bancaExistente.setPlataforma(novaBanca.getPlataforma());
        bancaExistente.setUsuarioId(novaBanca.getUsuarioId());
        return bancaRepository.save(bancaExistente);
    }

    @PostMapping
    public ResponseEntity<?> salvarNovoTorneio(@Valid @RequestBody Torneio novoTorneio) {
        try { return ResponseEntity.ok(service.registrarTorneio(novoTorneio)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarTorneio(@PathVariable Long id, @Valid @RequestBody Torneio torneio) {
        try { return ResponseEntity.ok(service.atualizarTorneio(id, torneio)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping
    public List<Torneio> listarTodos(@RequestParam Long usuarioId) { return repository.findByUsuarioId(usuarioId); }

    @GetMapping("/dashboard")
    public DashboardDTO obterDadosDashboard(@RequestParam Long usuarioId, @RequestParam(required = false) LocalDate dataInicio, @RequestParam(required = false) LocalDate dataFim, @RequestParam(required = false) String plataforma) {
        return service.calcularIndicadores(usuarioId, plataforma);
    }

    @GetMapping("/historico")
    public ResponseEntity<Page<HistoricoDTO>> listarHistorico(@RequestParam Long usuarioId, @RequestParam(required = false) LocalDate dataInicio, @RequestParam(required = false) LocalDate dataFim, @RequestParam(required = false) String plataforma, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.obterHistoricoUnificado(usuarioId, dataInicio, dataFim, plataforma, page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTorneio(@PathVariable Long id) {
        service.deletarTorneio(id);
        return ResponseEntity.noContent().build();
    }
}