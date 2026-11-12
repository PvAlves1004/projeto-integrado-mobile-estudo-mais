package com.example.estudomais;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Turma {

    private String id;
    private String nome;
    private String disciplina;
    private String semestre;
    private List<String> professorIds;
    private List<String> alunoIds;
    private boolean ativa;
    private Timestamp criadoEm;

    public Turma() {}

    public Turma(String nome, String disciplina, String semestre) {
        this.nome = nome;
        this.disciplina = disciplina;
        this.semestre = semestre;
        this.professorIds = new ArrayList<>();
        this.alunoIds = new ArrayList<>();
        this.ativa = true;
        this.criadoEm = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDisciplina() { return disciplina; }
    public void setDisciplina(String disciplina) { this.disciplina = disciplina; }

    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }

    public List<String> getProfessorIds() { return professorIds != null ? professorIds : new ArrayList<>(); }
    public void setProfessorIds(List<String> professorIds) { this.professorIds = professorIds; }

    public List<String> getAlunoIds() { return alunoIds != null ? alunoIds : new ArrayList<>(); }
    public void setAlunoIds(List<String> alunoIds) { this.alunoIds = alunoIds; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public Timestamp getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Timestamp criadoEm) { this.criadoEm = criadoEm; }

    @Override
    public String toString() { return nome + " — " + disciplina; }
}
