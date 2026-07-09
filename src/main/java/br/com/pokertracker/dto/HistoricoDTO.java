package br.com.pokertracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HistoricoDTO {
    private Long id;
    private LocalDate data;
    private LocalDateTime dataRegistro; // NOVO
    private String descricao;
    private String plataforma;
    private Double custoTotal;
    private Double itm;
    private Double valorResultado;
    private String tipoItem;

    // Construtor atualizado
    public HistoricoDTO(Long id, LocalDate data, LocalDateTime dataRegistro, String descricao, String plataforma,
                        Double custoTotal, Double itm, Double valorResultado, String tipoItem) {
        this.id = id;
        this.data = data;
        this.dataRegistro = dataRegistro;
        this.descricao = descricao;
        this.plataforma = plataforma;
        this.custoTotal = custoTotal;
        this.itm = itm;
        this.valorResultado = valorResultado;
        this.tipoItem = tipoItem;
    }

    public Long getId() { return id; }
    public LocalDate getData() { return data; }
    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public String getDescricao() { return descricao; }
    public String getPlataforma() { return plataforma; }
    public Double getCustoTotal() { return custoTotal; }
    public Double getItm() { return itm; }
    public Double getValorResultado() { return valorResultado; }
    public String getTipoItem() { return tipoItem; }
}