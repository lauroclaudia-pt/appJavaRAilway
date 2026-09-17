package pt.ipma.recrutamento.domain.enums;

/** Estado de uma etapa do workflow de uma Vaga concreta (job_stage). */
public enum StageState {
    DRAFT,      // Por iniciar
    ACTIVE,     // Em execução — só uma etapa ativa por vaga em cada momento
    COMPLETED,  // Concluída
    SKIPPED,    // Ignorada por regra de negócio
    CANCELLED   // Cancelada manualmente
}
