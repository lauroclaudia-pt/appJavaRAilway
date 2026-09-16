package pt.ipma.recrutamento.domain.enums;

/** Código técnico da etapa do workflow (nunca o nome visível — secção 2.5 da spec). */
public enum StageCode {
    OPENING,
    APPLICATIONS,
    ADMISSION,
    MISSING_REQUIREMENTS,
    EVALUATION,
    INTERVIEW,
    APPEAL,
    CONTRACT,
    MOBILITY,
    APPOINTMENT
}
