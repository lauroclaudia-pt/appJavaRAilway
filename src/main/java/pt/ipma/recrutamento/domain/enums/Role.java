package pt.ipma.recrutamento.domain.enums;

/**
 * Perfis do sistema (secção 13.1 da spec):
 * CDRH        — Chefe de Divisão de RH: acesso total, sem restrições de registo.
 * GESTOR_RH   — Gestor do Procedimento (titular ou suplente): acesso às vagas onde está associado.
 * JURI        — Membro do Júri: leitura restrita às vagas/candidatos onde tem esse papel.
 * PORTAL      — Candidato autenticado no Portal: acesso apenas ao seu próprio registo.
 * ADMIN       — Administração técnica da plataforma.
 */
public enum Role {
    ADMIN,
    CDRH,
    GESTOR_RH,
    JURI,
    PORTAL
}
