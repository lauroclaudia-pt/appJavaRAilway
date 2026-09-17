package pt.ipma.recrutamento.web.dto;

import lombok.Data;

/** Preenchimento dos campos de triagem — Requisitos de Admissão (secção 6.2). */
@Data
public class TriagemRequest {
    private Boolean habilitOk;
    private Boolean vinculoOk;
    private Boolean docsOk;
    private Boolean expOk;
    private String motivoExclusao;
}
