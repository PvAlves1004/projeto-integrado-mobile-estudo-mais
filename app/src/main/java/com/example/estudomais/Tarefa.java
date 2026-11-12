package com.example.estudomais;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Tarefa {

    private String id;
    private String turmaId;
    private String turmaNome;
    @Exclude private String status;
    private String titulo;
    private String descricao;
    private Timestamp prazo;
    private boolean valeNota;
    private Double notaMaxima;
    private String professorId;
    private Timestamp publicadaEm;

    public Tarefa() {}

    public Tarefa(String turmaId, String titulo, String descricao,
                  Timestamp prazo, boolean valeNota, Double notaMaxima, String professorId) {
        this.turmaId = turmaId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.prazo = prazo;
        this.valeNota = valeNota;
        this.notaMaxima = notaMaxima;
        this.professorId = professorId;
        this.publicadaEm = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTurmaId() { return turmaId; }
    public void setTurmaId(String turmaId) { this.turmaId = turmaId; }

    public String getTurmaNome() { return turmaNome; }
    public void setTurmaNome(String turmaNome) { this.turmaNome = turmaNome; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Timestamp getPrazo() { return prazo; }
    public void setPrazo(Timestamp prazo) { this.prazo = prazo; }

    public boolean isValeNota() { return valeNota; }
    public void setValeNota(boolean valeNota) { this.valeNota = valeNota; }

    public Double getNotaMaxima() { return notaMaxima; }
    public void setNotaMaxima(Double notaMaxima) { this.notaMaxima = notaMaxima; }

    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }

    public Timestamp getPublicadaEm() { return publicadaEm; }
    public void setPublicadaEm(Timestamp publicadaEm) { this.publicadaEm = publicadaEm; }

    @Exclude public String getStatus() { return status; }
    @Exclude public void setStatus(String status) { this.status = status; }
}
