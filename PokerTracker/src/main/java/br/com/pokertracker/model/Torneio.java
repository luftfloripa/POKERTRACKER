package br.com.pokertracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tb_torneio")
@Data
public class Torneio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId;

    @NotBlank(message = "A descrição não pode ficar em branco")
    private String descricao;

    @NotBlank(message = "A plataforma é obrigatória")
    private String plataforma;

    @NotNull(message = "O valor do Buy-in é obrigatório")
    @Min(value = 0, message = "O Buy-in não pode ser negativo")
    private Double buyIn = 0.0;

    @Min(value = 0, message = "O Rebuy não pode ser negativo")
    private Double rebuy = 0.0;

    @Min(value = 0, message = "O Addon não pode ser negativo")
    private Double addon = 0.0;

    @Min(value = 0, message = "O ITM não pode ser negativo. Se não premiou, digite 0.")
    private Double itm = 0.0;

    @NotNull(message = "A data é obrigatória")
    @PastOrPresent(message = "Não é possível registrar torneios no futuro")
    private LocalDate data;

    // NOVO: Carimbo de tempo exato
    private LocalDateTime dataRegistro;

    @PrePersist
    protected void onCreate() {
        if (this.dataRegistro == null) {
            this.dataRegistro = LocalDateTime.now();
        }
    }

    public Double getLucroIndividual() {
        return (itm != null ? itm : 0.0) -
                ((buyIn != null ? buyIn : 0.0) +
                        (rebuy != null ? rebuy : 0.0) +
                        (addon != null ? addon : 0.0));
    }
}