package pt.ipma.recrutamento.domain.enums;

/** Tipo de Oferta — determina o Workflow Template aplicável à Vaga (secção 2 / 15.3.3 da spec). */
public enum OfferType {
    PROCEDIMENTO_CONCURSAL_COMUM,
    PROCEDIMENTO_CONCURSAL_RESERVA,
    PROCEDIMENTO_CONCURSAL_SELECAO_INTERNACIONAL,
    PROCEDIMENTO_CONCURSAL_CARGOS_DIRECAO,
    MOBILIDADE_INTERNA,
    MOBILIDADE_INTERCARREIRAS,
    BOLSA_INVESTIGACAO_CIENTIFICA
}
