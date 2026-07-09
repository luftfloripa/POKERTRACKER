package br.com.pokertracker.repository;

import br.com.pokertracker.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // O Spring faz a mágica de buscar o usuário pelo e-mail só lendo esse nome!
    Optional<Usuario> findByEmail(String email);
}