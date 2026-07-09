package br.com.pokertracker.controller;

import br.com.pokertracker.model.Sessao;
import br.com.pokertracker.repository.SessaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sessoes")
public class SessaoController {

    @Autowired
    private SessaoRepository repository;

    // CREATE (POST)
    @PostMapping
    public Sessao criarSessao(@RequestBody Sessao novaSessao) {
        return repository.save(novaSessao);
    }

    // READ (GET)
    @GetMapping
    public List<Sessao> listarTodas() {
        return repository.findAll();
    }

    // UPDATE (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Sessao> atualizarSessao(@PathVariable Long id, @RequestBody Sessao dadosAtualizados) {
        Optional<Sessao> busca = repository.findById(id);

        if (busca.isPresent()) {
            Sessao sessaoExistente = busca.get();
            sessaoExistente.setDataSessao(dadosAtualizados.getDataSessao());
            sessaoExistente.setTitulo(dadosAtualizados.getTitulo());
            sessaoExistente.setNotas(dadosAtualizados.getNotas());

            return ResponseEntity.ok(repository.save(sessaoExistente));
        }

        return ResponseEntity.notFound().build();
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarSessao(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}