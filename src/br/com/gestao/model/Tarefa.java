package br.com.gestao.model;

import java.time.LocalDate;

public class Tarefa {
    private String nome;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFimPrevista;
    private LocalDate dataConclusao;
    private boolean concluida;

    // Prioridade da tarefa: Baixa, Média, Alta
    private Prioridade prioridade;

    // Referências
    private String projeto;
    private String equipe;
    private String responsavel;

    // Enum para status detalhado do prazo
    public enum StatusPrazo {
        NAO_CONCLUIDA("Não concluída"),
        NO_PRAZO("No prazo"),
        ATRASADA("Atrasada");

        private final String mensagem;

        StatusPrazo(String mensagem) { this.mensagem = mensagem; }

        @Override
        public String toString() { return mensagem; }
    }


    public enum Prioridade {
        BAIXA,
        MEDIA,
        ALTA
    }

    // ==== Construtor com prioridade ====
    public Tarefa(String nome, String descricao, LocalDate dataInicio, LocalDate dataFimPrevista,
                  String projeto, String equipe, String responsavel, Prioridade prioridade) {
        this.nome = nome;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.dataFimPrevista = dataFimPrevista;
        this.projeto = projeto;
        this.equipe = equipe;
        this.responsavel = responsavel;
        this.prioridade = prioridade;
        this.concluida = false;
    }

    // ==== Getters & Setters ====
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFimPrevista() { return dataFimPrevista; }
    public LocalDate getDataConclusao() { return dataConclusao; }
    public boolean isConcluida() { return concluida; }
    public Prioridade getPrioridade() { return prioridade; }

    public String getProjeto() { return projeto; }
    public void setProjeto(String projeto) { this.projeto = projeto; }

    public String getEquipe() { return equipe; }
    public void setEquipe(String equipe) { this.equipe = equipe; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }

    // ==== Conclusão da tarefa ====
    public void concluir(LocalDate dataConclusao) {
        this.dataConclusao = dataConclusao;
        this.concluida = true;
    }

    public boolean concluidaNoPrazo() {
        return concluida && (dataConclusao != null) && !dataConclusao.isAfter(dataFimPrevista);
    }

    public StatusPrazo getStatusPrazo() {
        if (!concluida) return StatusPrazo.NAO_CONCLUIDA;
        return concluidaNoPrazo() ? StatusPrazo.NO_PRAZO : StatusPrazo.ATRASADA;
    }

    @Override
    public String toString() {
        return nome + " (Responsável: " + (responsavel != null ? responsavel : "N/A") +
               ", Prioridade: " + prioridade +
               ", Concluída: " + concluida +
               ", Previsto: " + dataFimPrevista +
               ", Concluída em: " + (dataConclusao != null ? dataConclusao : "N/A") +
               ", Status: " + getStatusPrazo() +
               ", Projeto: " + (projeto != null ? projeto : "N/A") +
               ", Equipe: " + (equipe != null ? equipe : "N/A") + ")";
    }
}
