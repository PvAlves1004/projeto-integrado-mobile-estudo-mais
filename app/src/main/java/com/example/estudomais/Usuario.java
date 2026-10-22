package com.example.estudomais;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Usuario {

    private String nomeCompleto;
    private String email;
    private String telefone;
    private String nascimento;
    private String matricula;
    private String perfilId;   // "PROFESSOR", "ALUNO" ou "RESPONSAVEL"
    private boolean ativo;
    @ServerTimestamp
    private Date criadoEm;

    //construtor vazio usado para o firestore reconstruir o objeto
    public Usuario() {}

    public Usuario(String nomeCompleto, String email, String telefone,
                   String nascimento, String matricula, String perfilId) {
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.telefone = telefone;
        this.nascimento = nascimento;
        this.matricula = matricula;
        this.perfilId = perfilId;
        this.ativo = true;


    }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getNascimento() { return nascimento; }
    public void setNascimento(String nascimento) { this.nascimento = nascimento; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getPerfilId() { return perfilId; }
    public void setPerfilId(String perfilId) { this.perfilId = perfilId; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Date getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Date criadoEm) { this.criadoEm = criadoEm; }
}
