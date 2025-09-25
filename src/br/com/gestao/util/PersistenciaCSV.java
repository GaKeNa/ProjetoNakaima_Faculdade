package br.com.gestao.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenciaCSV {

    // ==== SALVAR ====
    public static void salvar(String caminho, List<String[]> dados) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(caminho))) {
            for (String[] linha : dados) {
                writer.println(String.join(";", linha));
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar CSV: " + e.getMessage());
        }
    }

    // ==== CARREGAR ====
    public static List<String[]> carregar(String caminho) {
        List<String[]> linhas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                linhas.add(linha.split(";"));
            }
        } catch (IOException e) {
            System.out.println("Arquivo CSV não encontrado: " + caminho);
        }
        return linhas;
    }
}
