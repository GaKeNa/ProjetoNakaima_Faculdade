package br.com.gestao;

import br.com.gestao.manager.GestorSistema;
import br.com.gestao.model.Usuario;
import br.com.gestao.model.enums.PerfilUsuario;
import br.com.gestao.model.enums.StatusProjeto;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        GestorSistema gestor = new GestorSistema();

        // Cadastrar usuários
        gestor.cadastrarUsuario("Maria Silva", "12345678900", "maria@empresa.com",
                "Analista", "maria", "1234", PerfilUsuario.GERENTE);

        gestor.cadastrarUsuario("João Souza", "98765432100", "joao@empresa.com",
                "Dev", "joao", "abcd", PerfilUsuario.COLABORADOR);

        // Pegar um gerente para projeto
        Usuario gerente = gestor.getUsuarios().stream()
                .filter(u -> u.getPerfil() == PerfilUsuario.GERENTE)
                .findFirst()
                .orElse(null);

        // Cadastrar projeto
        gestor.cadastrarProjeto("Sistema de Gestão", "Sistema para gerenciar projetos",
                LocalDate.now(), LocalDate.now().plusMonths(6),
                StatusProjeto.PLANEJADO, gerente);

        // Cadastrar equipe
        gestor.cadastrarEquipe("Equipe Backend", "Equipe responsável pelo backend");

        // Mostrar listagens
        System.out.println("\nUsuários cadastrados: " + gestor.getUsuarios());
        System.out.println("Projetos cadastrados: " + gestor.getProjetos());
        System.out.println("Equipes cadastradas: " + gestor.getEquipes());
    }
}
