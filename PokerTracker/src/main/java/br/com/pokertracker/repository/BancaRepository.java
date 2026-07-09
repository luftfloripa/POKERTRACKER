package br.com.pokertracker.repository;

import br.com.pokertracker.model.Banca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BancaRepository extends JpaRepository<Banca, Long> {
    List<Banca> findByUsuarioId(Long usuarioId);
}