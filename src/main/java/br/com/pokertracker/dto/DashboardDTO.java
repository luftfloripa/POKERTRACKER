package br.com.pokertracker.dto;

import java.util.Map;

public class DashboardDTO {
    private double roi;
    private double abi;
    private double lucroTotal;
    private double makeUp; // <--- NOVO CAMPO ADICIONADO
    private double valorBanca;
    private long quantidadeTorneios;
    private Map<String, Double> bancasPorPlataforma;

    // Construtor completo atualizado
    public DashboardDTO(double roi, double abi, double lucroTotal, double makeUp, double valorBanca, long quantidadeTorneios, Map<String, Double> bancasPorPlataforma) {
        this.roi = roi;
        this.abi = abi;
        this.lucroTotal = lucroTotal;
        this.makeUp = makeUp; // <--- NOVO CAMPO ADICIONADO
        this.valorBanca = valorBanca;
        this.quantidadeTorneios = quantidadeTorneios;
        this.bancasPorPlataforma = bancasPorPlataforma;
    }

    public double getRoi() { return roi; }
    public double getAbi() { return abi; }
    public double getLucroTotal() { return lucroTotal; }
    public double getMakeUp() { return makeUp; } // <--- NOVO CAMPO ADICIONADO
    public double getValorBanca() { return valorBanca; }
    public long getQuantidadeTorneios() { return quantidadeTorneios; }
    public Map<String, Double> getBancasPorPlataforma() { return bancasPorPlataforma; }
}