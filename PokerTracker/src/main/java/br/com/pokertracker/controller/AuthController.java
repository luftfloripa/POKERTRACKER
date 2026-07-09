package br.com.pokertracker.controller;

import br.com.pokertracker.model.Usuario;
import br.com.pokertracker.repository.UsuarioRepository;
import br.com.pokertracker.security.TokenService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager manager;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Autowired
    public AuthController(UsuarioRepository usuarioRepository,
                          AuthenticationManager manager,
                          TokenService tokenService,
                          PasswordEncoder passwordEncoder,
                          EntityManager entityManager) {
        this.usuarioRepository = usuarioRepository;
        this.manager = manager;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    // 1. ROTA DE LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> fazerLogin(@RequestBody Usuario dadosLogin) {
        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(dadosLogin.getEmail(), dadosLogin.getSenha());
            var authentication = manager.authenticate(authenticationToken);

            var tokenJWT = tokenService.gerarToken(authentication.getName());
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

            // DTO On-the-fly: Retorna o usuário sem a senha!
            Map<String, Object> usuarioSeguro = Map.of(
                    "id", usuarioLogado.getId(),
                    "nome", usuarioLogado.getNome(),
                    "email", usuarioLogado.getEmail(),
                    "role", usuarioLogado.getRole()
            );

            return ResponseEntity.ok(Map.of("token", tokenJWT, "usuario", usuarioSeguro));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("E-mail ou senha inválidos.");
        }
    }

    // 2. ROTA PARA CADASTRAR NOVO USUÁRIO
    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@RequestBody Usuario novo) {
        String senhaCriptografada = passwordEncoder.encode(novo.getSenha());
        novo.setSenha(senhaCriptografada);
        Usuario salvo = usuarioRepository.save(novo);

        // Retorna sucesso, mas sem expor a senha de volta pro front
        return ResponseEntity.status(201).body(Map.of("mensagem", "Usuário cadastrado com sucesso", "id", salvo.getId()));
    }

    // 3. ROTA PARA LISTAR TODOS OS USUÁRIOS (BLINDADA)
    @GetMapping("/usuarios")
    public ResponseEntity<List<Map<String, Object>>> listar() {
        // Pega todos do banco e converte para Map (DTO), removendo a senha
        List<Map<String, Object>> listaSegura = usuarioRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "nome", u.getNome(),
                        "email", u.getEmail(),
                        "role", u.getRole()
                ))
                .toList();

        return ResponseEntity.ok(listaSegura);
    }

    // 4. ROTA PARA DELETAR USUÁRIO
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        if (id == 1L) {
            return ResponseEntity.badRequest().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 5. ROTA PARA ATUALIZAR PERFIL (Nome e/ou Senha)
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> atualizarUsuario(@PathVariable Long id, @RequestBody Usuario dadosAtualizados) {
        Optional<Usuario> busca = usuarioRepository.findById(id);

        if (busca.isPresent()) {
            Usuario usuarioExistente = busca.get();
            usuarioExistente.setNome(dadosAtualizados.getNome());

            if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().trim().isEmpty()) {
                String senhaCriptografada = passwordEncoder.encode(dadosAtualizados.getSenha());
                usuarioExistente.setSenha(senhaCriptografada);
            }

            Usuario salvo = usuarioRepository.save(usuarioExistente);

            // Retorna dados seguros
            return ResponseEntity.ok(Map.of(
                    "id", salvo.getId(),
                    "nome", salvo.getNome(),
                    "email", salvo.getEmail()
            ));
        }

        return ResponseEntity.notFound().build();
    }

    // 6. ROTA: HARD RESET (Limpa o histórico com JPQL, mantendo o login)
    @DeleteMapping("/usuarios/{id}/reset")
    @Transactional
    public ResponseEntity<Void> resetarDadosUsuario(@PathVariable Long id) {
        entityManager.createQuery("DELETE FROM Torneio t WHERE t.usuarioId = :id")
                .setParameter("id", id).executeUpdate();

        entityManager.createQuery("DELETE FROM Movimentacao m WHERE m.usuarioId = :id")
                .setParameter("id", id).executeUpdate();

        entityManager.createQuery("DELETE FROM Banca b WHERE b.usuarioId = :id")
                .setParameter("id", id).executeUpdate();

        return ResponseEntity.noContent().build();
    }
}