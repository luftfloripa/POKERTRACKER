package br.com.pokertracker.repository;

import br.com.pokertracker.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SessaoRepository extends JpaRepository<Sessao, Long> {
    List<Sessao> findByUsuario_Id(Long usuarioId);
}