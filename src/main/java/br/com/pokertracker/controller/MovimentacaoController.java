package br.com.pokertracker.controller;

import br.com.pokertracker.model.Movimentacao;
import br.com.pokertracker.repository.MovimentacaoRepository;
import br.com.pokertracker.service.MovimentacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/movimentacoes")
public class MovimentacaoController {

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private MovimentacaoService movimentacaoService;

    @PostMapping
    public ResponseEntity<?> salvarMovimentacao(@Valid @RequestBody Movimentacao movimentacao) {
        try {
            // Repassa a responsabilidade de validar e salvar para o Service
            Movimentacao salva = movimentacaoService.registrarMovimentacao(movimentacao);
            return ResponseEntity.ok(salva);

        } catch (IllegalArgumentException e) {
            // Se o Service barrar o saque, cai aqui e devolve o Erro 400 (Bad Request) para o Front-end
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Movimentacao>> listarMovimentacoes(@RequestParam Long usuarioId) {
        List<Movimentacao> lista = movimentacaoRepository.findByUsuarioId(usuarioId);
        return ResponseEntity.ok(lista);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMovimentacao(@PathVariable Long id) {
        movimentacaoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}