package br.com.gestao.manager;

import br.com.gestao.model.*;
import br.com.gestao.model.enums.PerfilUsuario;
import br.com.gestao.model.enums.StatusProjeto;
import br.com.gestao.model.enums.TipoPersistencia;
import br.com.gestao.util.Persistencia;
import br.com.gestao.util.PersistenciaCSV;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.File;

public class GestorSistema {
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Projeto> projetos = new ArrayList<>();
    private List<Equipe> equipes = new ArrayList<>();

    private TipoPersistencia tipoPersistencia = TipoPersistencia.JSON; // padrão

    private static final String USUARIOS_ARQ = "usuarios.json";
    private static final String PROJETOS_ARQ = "projetos.json";
    private static final String EQUIPES_ARQ = "equipes.json";

    private static final String USUARIOS_CSV = "usuarios.csv";
    private static final String PROJETOS_CSV = "projetos.csv";
    private static final String EQUIPES_CSV = "equipes.csv";

    // ==== Configuração de persistência ====
    public void setTipoPersistencia(TipoPersistencia tipo) {
        this.tipoPersistencia = tipo;
    }

    public TipoPersistencia getTipoPersistencia() {
        return tipoPersistencia;
    }

    // ==== Salvar tudo ====
    public void salvarTudo() {
        if (tipoPersistencia == TipoPersistencia.JSON) {
            salvarJSON();
            salvarBackupJSON();
        } else {
            salvarCSV();
            salvarBackupCSV();
        }
    }

    // ==== Carregar tudo ====
    public void carregarTudo() {
        if (tipoPersistencia == TipoPersistencia.JSON) {
            carregarJSON();
        } else {
            carregarCSV();
        }
    }

    public List<String> listarBackups(String palavraChave) {
        File pasta = new File("."); // pasta atual
        String[] arquivos = pasta.list((dir, name) ->
                name.startsWith("backup_") && name.contains(palavraChave)
        );
        if (arquivos != null) {
            Arrays.sort(arquivos);
            return Arrays.asList(arquivos);
        }
        return new ArrayList<>();
    }

    // ==== Backup JSON ====
    private void salvarBackupJSON() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Persistencia.salvar("backup_usuarios_" + timestamp + ".json", usuarios);
        Persistencia.salvar("backup_projetos_" + timestamp + ".json", projetos);
        Persistencia.salvar("backup_equipes_" + timestamp + ".json", equipes);
        System.out.println("Backup JSON criado: " + timestamp);
    }

    // ==== Backup CSV ====
    private void salvarBackupCSV() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        PersistenciaCSV.salvar("backup_usuarios_" + timestamp + ".csv", gerarDadosUsuariosCSV());
        PersistenciaCSV.salvar("backup_projetos_" + timestamp + ".csv", gerarDadosProjetosCSV());
        PersistenciaCSV.salvar("backup_equipes_" + timestamp + ".csv", gerarDadosEquipesCSV());
        System.out.println("Backup CSV criado: " + timestamp);
    }

    // ==== Restaurar Backup ====
    public void restaurarBackup(String caminhoUsuarios, String caminhoProjetos, String caminhoEquipes) {
        if (tipoPersistencia == TipoPersistencia.JSON) {
            Type usuariosType = new TypeToken<ArrayList<Usuario>>() {}.getType();
            Type projetosType = new TypeToken<ArrayList<Projeto>>() {}.getType();
            Type equipesType = new TypeToken<ArrayList<Equipe>>() {}.getType();

            List<Usuario> u = Persistencia.carregar(caminhoUsuarios, usuariosType);
            List<Projeto> p = Persistencia.carregar(caminhoProjetos, projetosType);
            List<Equipe> e = Persistencia.carregar(caminhoEquipes, equipesType);

            if (u != null) usuarios = u;
            if (p != null) projetos = p;
            if (e != null) equipes = e;

        } else {
            List<String[]> usuariosCSV = PersistenciaCSV.carregar(caminhoUsuarios);
            usuarios.clear();
            for (String[] u : usuariosCSV) {
                usuarios.add(new Usuario(u[0], u[1], u[2], u[3], u[4], u[5], PerfilUsuario.valueOf(u[6])));
            }

            List<String[]> equipesCSV = PersistenciaCSV.carregar(caminhoEquipes);
            equipes.clear();
            for (String[] e : equipesCSV) {
                Equipe equipe = new Equipe(e[0], e[1]);
                if (e.length > 2 && !e[2].isEmpty()) {
                    String[] cpfs = e[2].split(",");
                    for (String cpf : cpfs) {
                        usuarios.stream()
                                .filter(u -> u.getCpf().equals(cpf))
                                .findFirst()
                                .ifPresent(equipe::adicionarMembro);
                    }
                }
                equipes.add(equipe);
            }

            List<String[]> projetosCSV = PersistenciaCSV.carregar(caminhoProjetos);
            projetos.clear();
            for (String[] p : projetosCSV) {
                Usuario gerente = usuarios.stream()
                        .filter(u -> u.getCpf().equals(p[2]))
                        .findFirst().orElse(null);
                Equipe equipe = equipes.stream()
                        .filter(eq -> eq.getNome().equalsIgnoreCase(p[3]))
                        .findFirst().orElse(null);

                Projeto projeto = new Projeto(
                        p[0], p[1],
                        LocalDate.parse(p[4]), LocalDate.parse(p[5]),
                        StatusProjeto.valueOf(p[6]), gerente
                );
                projeto.setEquipe(equipe);
                projetos.add(projeto);
            }
        }

        System.out.println("Backup restaurado com sucesso!");
    }

    // ==== Persistência JSON ====
    private void salvarJSON() {
        Persistencia.salvar(USUARIOS_ARQ, usuarios);
        Persistencia.salvar(PROJETOS_ARQ, projetos);
        Persistencia.salvar(EQUIPES_ARQ, equipes);
        System.out.println("Dados salvos em JSON!");
    }

    private void carregarJSON() {
        Type usuariosType = new TypeToken<ArrayList<Usuario>>() {}.getType();
        Type projetosType = new TypeToken<ArrayList<Projeto>>() {}.getType();
        Type equipesType = new TypeToken<ArrayList<Equipe>>() {}.getType();

        List<Usuario> u = Persistencia.carregar(USUARIOS_ARQ, usuariosType);
        List<Projeto> p = Persistencia.carregar(PROJETOS_ARQ, projetosType);
        List<Equipe> e = Persistencia.carregar(EQUIPES_ARQ, equipesType);

        if (u != null) usuarios = u;
        if (p != null) projetos = p;
        if (e != null) equipes = e;

        System.out.println("Dados carregados de JSON!");
    }

    // ==== Persistência CSV ====
    public void salvarCSV() {
        PersistenciaCSV.salvar(USUARIOS_CSV, gerarDadosUsuariosCSV());
        PersistenciaCSV.salvar(PROJETOS_CSV, gerarDadosProjetosCSV());
        PersistenciaCSV.salvar(EQUIPES_CSV, gerarDadosEquipesCSV());
        System.out.println("Dados salvos em CSV!");
    }

    public void carregarCSV() {
        usuarios.clear();
        for (String[] u : PersistenciaCSV.carregar(USUARIOS_CSV)) {
            usuarios.add(new Usuario(u[0], u[1], u[2], u[3], u[4], u[5], PerfilUsuario.valueOf(u[6])));
        }

        equipes.clear();
        for (String[] e : PersistenciaCSV.carregar(EQUIPES_CSV)) {
            Equipe equipe = new Equipe(e[0], e[1]);
            if (e.length > 2 && !e[2].isEmpty()) {
                String[] cpfs = e[2].split(",");
                for (String cpf : cpfs) {
                    usuarios.stream()
                            .filter(u -> u.getCpf().equals(cpf))
                            .findFirst()
                            .ifPresent(equipe::adicionarMembro);
                }
            }
            equipes.add(equipe);
        }

        projetos.clear();
        for (String[] p : PersistenciaCSV.carregar(PROJETOS_CSV)) {
            Usuario gerente = usuarios.stream()
                    .filter(u -> u.getCpf().equals(p[2]))
                    .findFirst().orElse(null);
            Equipe equipe = equipes.stream()
                    .filter(eq -> eq.getNome().equalsIgnoreCase(p[3]))
                    .findFirst().orElse(null);

            Projeto projeto = new Projeto(
                    p[0], p[1],
                    LocalDate.parse(p[4]), LocalDate.parse(p[5]),
                    StatusProjeto.valueOf(p[6]), gerente
            );
            projeto.setEquipe(equipe);
            projetos.add(projeto);
        }

        System.out.println("Dados carregados de CSV!");
    }

    // ==== USUÁRIOS ====
    public void cadastrarUsuario(String nome, String cpf, String email, String cargo,
                                 String login, String senha, PerfilUsuario perfil) {
        Usuario usuario = new Usuario(nome, cpf, email, cargo, login, senha, perfil);
        usuarios.add(usuario);
        salvarTudo();
        System.out.println("Usuário cadastrado com sucesso!");
    }

    public void atualizarUsuario(String cpf, String novoEmail, String novoCargo) {
        for (Usuario u : usuarios) {
            if (u.getCpf().equals(cpf)) {
                u.setEmail(novoEmail);
                u.setCargo(novoCargo);
                salvarTudo();
                System.out.println("Usuário atualizado!");
                return;
            }
        }
        System.out.println("Usuário não encontrado!");
    }

    public void removerUsuario(String cpf) {
        boolean removido = usuarios.removeIf(u -> u.getCpf().equals(cpf));
        if (removido) {
            salvarTudo();
            System.out.println("Usuário removido!");
        } else {
            System.out.println("Usuário não encontrado!");
        }
    }

    // ==== PROJETOS ====
    public void cadastrarProjeto(String nome, String descricao, LocalDate inicio,
                                 LocalDate fim, StatusProjeto status, Usuario gerente) {
        Projeto projeto = new Projeto(nome, descricao, inicio, fim, status, gerente);
        projetos.add(projeto);
        salvarTudo();
        System.out.println("Projeto cadastrado!");
    }

    public void atualizarProjeto(String nome, StatusProjeto novoStatus) {
        projetos.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .ifPresentOrElse(p -> {
                    p.setStatus(novoStatus);
                    salvarTudo();
                    System.out.println("Projeto atualizado: " + p);
                }, () -> System.out.println("Projeto não encontrado."));
    }

    public void removerProjeto(String nomeProjeto) {
        boolean removido = projetos.removeIf(p -> p.getNome().equalsIgnoreCase(nomeProjeto));
        if (removido) {
            salvarTudo();
            System.out.println("Projeto removido!");
        } else {
            System.out.println("Projeto não encontrado!");
        }
    }

    // ==== EQUIPES ====
    public void cadastrarEquipe(String nome, String descricao) {
        Equipe equipe = new Equipe(nome, descricao);
        equipes.add(equipe);
        salvarTudo();
        System.out.println("Equipe cadastrada!");
    }

    public void atualizarEquipe(String nome, String novoNome, String novaDesc) {
        equipes.stream()
                .filter(e -> e.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .ifPresentOrElse(e -> {
                    if (novoNome != null && !novoNome.isBlank()) e.setNome(novoNome);
                    if (novaDesc != null && !novaDesc.isBlank()) e.setDescricao(novaDesc);
                    salvarTudo();
                    System.out.println("Equipe atualizada: " + e);
                }, () -> System.out.println("Equipe não encontrada."));
    }

    public void removerEquipe(String nomeEquipe) {
        boolean removido = equipes.removeIf(e -> e.getNome().equalsIgnoreCase(nomeEquipe));
        if (removido) {
            salvarTudo();
            System.out.println("Equipe removida!");
        } else {
            System.out.println("Equipe não encontrada!");
        }
    }

    // ==== ASSOCIAÇÕES ====
    public void adicionarUsuarioEmEquipe(String cpf, String nomeEquipe) {
        Usuario usuario = usuarios.stream()
                .filter(u -> u.getCpf().equals(cpf))
                .findFirst().orElse(null);

        Equipe equipe = equipes.stream()
                .filter(e -> e.getNome().equalsIgnoreCase(nomeEquipe))
                .findFirst().orElse(null);

        if (usuario != null && equipe != null) {
            equipe.adicionarMembro(usuario);
            salvarTudo();
            System.out.println("Usuário adicionado à equipe!");
        } else {
            System.out.println("Usuário ou equipe não encontrados.");
        }
    }

    public void associarEquipeAProjeto(String nomeEquipe, String nomeProjeto) {
        Equipe equipe = equipes.stream()
                .filter(e -> e.getNome().equalsIgnoreCase(nomeEquipe))
                .findFirst().orElse(null);

        Projeto projeto = projetos.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nomeProjeto))
                .findFirst().orElse(null);

        if (equipe != null && projeto != null) {
            projeto.setEquipe(equipe);
            salvarTudo();
            System.out.println("Equipe atribuída ao projeto!");
        } else {
            System.out.println("Equipe ou projeto não encontrados.");
        }
    }

    // ==== CSV helpers ====
    private List<String[]> gerarDadosUsuariosCSV() {
        List<String[]> dados = new ArrayList<>();
        for (Usuario u : usuarios) {
            dados.add(new String[]{
                    u.getNomeCompleto(),
                    u.getCpf(),
                    u.getEmail(),
                    u.getCargo(),
                    u.getLogin(),
                    u.getSenha(),
                    u.getPerfil().name()
            });
        }
        return dados;
    }

    private List<String[]> gerarDadosProjetosCSV() {
        List<String[]> dados = new ArrayList<>();
        for (Projeto p : projetos) {
            dados.add(new String[]{
                    p.getNome(),
                    p.getDescricao(),
                    p.getGerenteResponsavel() != null ? p.getGerenteResponsavel().getCpf() : "",
                    p.getEquipe() != null ? p.getEquipe().getNome() : "",
                    p.getDataInicio().toString(),
                    p.getDataFimPrevista().toString(),
                    p.getStatus().name()
            });
        }
        return dados;
    }

    private List<String[]> gerarDadosEquipesCSV() {
        List<String[]> dados = new ArrayList<>();
        for (Equipe e : equipes) {
            String membros = String.join(",", e.getMembros().stream().map(Usuario::getCpf).toList());
            dados.add(new String[]{e.getNome(), e.getDescricao(), membros});
        }
        return dados;
    }

    // ==== GETTERS ====
    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Projeto> getProjetos() { return projetos; }
    public List<Equipe> getEquipes() { return equipes; }
}
