package pt.ipma.recrutamento.service;

import org.springframework.stereotype.Service;

/**
 * Validação do NIF português através do algoritmo do Módulo 11 (secção 15.1 da spec).
 * Implementação nativa — substitui a dependência da biblioteca "stdnum" usada no Odoo.
 */
@Service
public class NifValidationService {

    private static final java.util.Set<Character> VALID_FIRST_DIGITS =
            java.util.Set.of('1', '2', '3', '5', '6', '7', '8', '9');

    public boolean isValid(String nif) {
        if (nif == null) return false;
        String clean = nif.trim().toUpperCase().replace("PT", "");
        if (!clean.matches("\\d{9}")) {
            return false;
        }
        if (!VALID_FIRST_DIGITS.contains(clean.charAt(0))) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 8; i++) {
            int digit = clean.charAt(i) - '0';
            sum += digit * (9 - i);
        }
        int mod = sum % 11;
        int checkDigit = (mod < 2) ? 0 : 11 - mod;

        int lastDigit = clean.charAt(8) - '0';
        return checkDigit == lastDigit;
    }

    public void validateOrThrow(String nif) {
        if (!isValid(nif)) {
            throw new IllegalArgumentException("O NIF '" + nif + "' não é um número de identificação fiscal português válido.");
        }
    }
}
