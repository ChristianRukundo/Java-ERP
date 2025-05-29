package rca.ac.rw.template.deduction.converter;

import rca.ac.rw.template.deduction.dto.DeductionRequestDto;
import rca.ac.rw.template.deduction.dto.DeductionResponseDto;
import rca.ac.rw.template.deduction.entity.Deduction;

public class DeductionConverter {

    public static Deduction toEntity(DeductionRequestDto dto) {
        if (dto == null) return null;
        Deduction deduction = new Deduction();
        deduction.setCode(dto.getCode());
        deduction.setDeductionName(dto.getDeductionName());
        deduction.setPercentage(dto.getPercentage());
        deduction.setDescription(dto.getDescription());
        if (dto.getIsActive() != null) {
            deduction.setActive(dto.getIsActive());
        } else {
            deduction.setActive(true); // Default to active on creation if not specified
        }
        return deduction;
    }

    public static DeductionResponseDto toDto(Deduction entity) {
        if (entity == null) return null;
        return new DeductionResponseDto(
                entity.getId(),
                entity.getCode(),
                entity.getDeductionName(),
                entity.getPercentage(),
                entity.isActive(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static void updateEntityFromDto(DeductionRequestDto dto, Deduction deduction) {
        if (dto == null || deduction == null) return;

        // Code is a business key, typically not updatable. If it is, handle uniqueness.
        // if (dto.getCode() != null) deduction.setCode(dto.getCode());

        if (dto.getDeductionName() != null) {
            deduction.setDeductionName(dto.getDeductionName());
        }
        if (dto.getPercentage() != null) {
            deduction.setPercentage(dto.getPercentage());
        }
        if (dto.getIsActive() != null) {
            deduction.setActive(dto.getIsActive());
        }
        if (dto.getDescription() != null) { // Allow clearing description
            deduction.setDescription(dto.getDescription());
        } else if (deduction.getDescription() != null && dto.getDescription() == null) {

            deduction.setDescription(null);
        }
    }
}