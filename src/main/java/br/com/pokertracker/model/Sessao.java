package br.com.pokertracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "tb_sessao")
@Data
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataSessao;
    private String titulo; // Ex: "Reta de Domingo" ou "Grind Cash Game"
    private String notas;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}