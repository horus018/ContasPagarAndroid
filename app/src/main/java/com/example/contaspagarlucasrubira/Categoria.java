package com.example.contaspagarlucasrubira;

import java.io.Serializable;
import java.util.ArrayList;

public class Categoria implements Serializable {
    private String descricao;
    private ArrayList<Conta> contas;

    public Categoria(String descricao) {
        this.descricao = descricao;
        contas = new ArrayList<Conta>();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public ArrayList<Conta> getContas() {
        return contas;
    }

    public void addConta(Conta conta){
        this.contas.add(conta);
    }
}