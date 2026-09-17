package pt.ipma.recrutamento.domain.enums;

/** Estado do candidato ao longo do procedimento (secção 15.3.3). */
public enum ApplicantState {
    SUBMITTED,
    UNDER_REVIEW,
    ADMITTED,
    EXCLUDED,
    UNDER_APPEAL,
    APPROVED,
    HIRED,
    REJECTED,
    CANCELLED
}
