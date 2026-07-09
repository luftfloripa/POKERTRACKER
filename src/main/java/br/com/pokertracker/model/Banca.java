package br.com.pokertracker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_banca")
@Data
public class Banca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId;

    private Double saldoInicial = 0.0;
    private String plataforma;
}