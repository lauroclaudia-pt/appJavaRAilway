package pt.ipma.recrutamento.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pt.ipma.recrutamento.domain.enums.Gender;
import pt.ipma.recrutamento.domain.enums.SelectionMethod;

import java.time.LocalDate;

/** Corresponde ao formulário de candidatura do Portal (secção 16.1.4 da spec). */
@Data
public class ApplicationSubmitRequest {

    @NotBlank private String partnerName;
    @NotNull  private LocalDate birthDate;
    private Gender gender;
    @NotBlank private String nationality;
    @NotBlank private String idNumber;
    @NotBlank private String idNif;
    private String address;
    private String postalCode;
    private String locality;
    private String municipality;
    @NotBlank @Email private String emailFrom;
    private String partnerPhone;
    @NotBlank private String partnerMobile;

    private String educationCourse;
    private String postgradInfo;
    private boolean publicEmployment;
    private String employmentSituation;
    private String lastEmployer;
    private String lastActivity;
    private String performanceEvaluation;
    private String relevantExperience;
    private String otherExperience;
    private String alternativeQualification;

    private SelectionMethod selectionMethods;
    private boolean hasDisability;
    private String specialNeedsDesc;

    @NotNull private Boolean declarationTrue;
    private Boolean mobDec;
}
