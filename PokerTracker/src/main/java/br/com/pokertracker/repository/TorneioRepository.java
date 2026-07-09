package br.com.pokertracker.repository;

import br.com.pokertracker.model.Torneio;
import br.com.pokertracker.dto.HistoricoDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface TorneioRepository extends JpaRepository<Torneio, Long> {

    List<Torneio> findByUsuarioId(Long usuarioId);
    List<Torneio> findByUsuarioIdAndDataBetween(Long usuarioId, LocalDate dataInicio, LocalDate dataFim);

    // MÁGICA: Nomes das tabelas corrigidos (tb_torneio e tb_movimentacao)
    @Query(value = "SELECT * FROM (" +
            "  SELECT id AS id, data AS data, descricao AS descricao, plataforma AS plataforma, " +
            "  (COALESCE(itm, 0) - buy_in - COALESCE(rebuy, 0) - COALESCE(addon, 0)) AS valorResultado, " +
            "  'TORNEIO' AS tipoItem, " +
            "  (buy_in + COALESCE(rebuy, 0) + COALESCE(addon, 0)) AS custoTotal, " +
            "  COALESCE(itm, 0) AS itm " +
            "  FROM tb_torneio WHERE usuario_id = :usuarioId " + // <--- CORRIGIDO AQUI
            "  UNION ALL " +
            "  SELECT id AS id, data AS data, tipo AS descricao, plataforma AS plataforma, " +
            "  CASE WHEN tipo = 'DEPOSITO' THEN valor ELSE -valor END AS valorResultado, " +
            "  'MOVIMENTACAO' AS tipoItem, " +
            "  0.0 AS custoTotal, " +
            "  0.0 AS itm " +
            "  FROM tb_movimentacao WHERE usuario_id = :usuarioId" + // <--- CORRIGIDO AQUI
            ") AS historico " +
            "WHERE data BETWEEN :dataInicio AND :dataFim " +
            "AND (:plataforma = '' OR plataforma = :plataforma) " +
            "ORDER BY data DESC",
            countQuery = "SELECT count(*) FROM (" +
                    "  SELECT id, data, plataforma FROM tb_torneio WHERE usuario_id = :usuarioId " + // <--- CORRIGIDO AQUI
                    "  UNION ALL " +
                    "  SELECT id, data, plataforma FROM tb_movimentacao WHERE usuario_id = :usuarioId" + // <--- CORRIGIDO AQUI
                    ") AS historico " +
                    "WHERE data BETWEEN :dataInicio AND :dataFim " +
                    "AND (:plataforma = '' OR plataforma = :plataforma)",
            nativeQuery = true)
    Page<HistoricoDTO> buscarHistoricoUnificado(
            @Param("usuarioId") Long usuarioId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("plataforma") String plataforma,
            Pageable pageable);
}