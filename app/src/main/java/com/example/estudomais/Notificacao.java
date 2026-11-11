package com.example.estudomais;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Notificacao {

    private String id;
    private String titulo;
    private String mensagem;
    private String remetenteId;
    private String remetenteNome;
    private List<String> destinatarios;
    private Timestamp criadaEm;
    private List<String> lidaPor;

    public Notificacao() {}

    public Notificacao(String titulo, String mensagem,
                       String remetenteId, String remetenteNome,
                       List<String> destinatarios) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.remetenteId = remetenteId;
        this.remetenteNome = remetenteNome;
        this.destinatarios = destinatarios;
        this.criadaEm = Timestamp.now();
        this.lidaPor = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getRemetenteId() { return remetenteId; }
    public void setRemetenteId(String remetenteId) { this.remetenteId = remetenteId; }

    public String getRemetenteNome() { return remetenteNome; }
    public void setRemetenteNome(String remetenteNome) { this.remetenteNome = remetenteNome; }

    public List<String> getDestinatarios() { return destinatarios; }
    public void setDestinatarios(List<String> destinatarios) { this.destinatarios = destinatarios; }

    public Timestamp getCriadaEm() { return criadaEm; }
    public void setCriadaEm(Timestamp criadaEm) { this.criadaEm = criadaEm; }

    public List<String> getLidaPor() { return lidaPor; }
    public void setLidaPor(List<String> lidaPor) { this.lidaPor = lidaPor; }
}
