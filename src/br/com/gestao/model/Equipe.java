package br.com.gestao.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Equipe {
    private String nome;
    private String descricao;
    private final List<Usuario> membros = new ArrayList<>();

    public Equipe(String nome, String descricao) {
        setNome(nome); // usa validação
        this.descricao = descricao;
    }

    // ==== GETTERS & SETTERS ====
    public String getNome() { return nome; }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da equipe não pode ser nulo ou vazio.");
        }
        this.nome = nome;
    }

    public String getDescricao() { return descricao; }

    public void setDescricao(String descricao) { this.descricao = descricao; }

    public List<Usuario> getMembros() {
        return Collections.unmodifiableList(membros); // proteção contra alterações externas
    }

    // ==== REGRAS ====
    public void adicionarMembro(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo.");
        }
        if (!membros.contains(usuario)) {
            membros.add(usuario);
        }
    }

    public void removerMembro(String cpf) {
        membros.removeIf(u -> u.getCpf().equals(cpf));
    }

    @Override
    public String toString() {
        return "Equipe{" +
                "nome='" + nome + '\'' +
                ", descricao='" + (descricao != null ? descricao : "N/A") + '\'' +
                ", totalMembros=" + membros.size() +
                '}';
    }
}
