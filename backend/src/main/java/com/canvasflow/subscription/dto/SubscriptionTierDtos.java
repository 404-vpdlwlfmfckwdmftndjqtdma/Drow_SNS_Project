package com.canvasflow.subscription.dto;

import com.canvasflow.subscription.entity.SubscriptionTier;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class SubscriptionTierDtos {

    public record TierCreateRequest(
            @NotBlank @Size(max = 30) String name,
            @Min(1) @Max(10) int level,
            @NotNull @DecimalMin("0") BigDecimal monthlyPrice,
            @Size(max = 200) String description
    ) {}

    public record TierUpdateRequest(
            @NotBlank @Size(max = 30) String name,
            @NotNull @DecimalMin("0") BigDecimal monthlyPrice,
            @Size(max = 200) String description
    ) {}

    public record TierResponse(
            Long id,
            String name,
            int level,
            BigDecimal monthlyPrice,
            String description
    ) {
        public static TierResponse from(SubscriptionTier tier) {
            return new TierResponse(
                    tier.getId(), tier.getName(), tier.getLevel(),
                    tier.getMonthlyPrice(), tier.getDescription());
        }

        public static List<TierResponse> from(List<SubscriptionTier> tiers) {
            return tiers.stream().map(TierResponse::from).toList();
        }
    }
}
