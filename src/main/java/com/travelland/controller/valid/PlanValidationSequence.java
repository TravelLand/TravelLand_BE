package com.travelland.controller.valid;

import jakarta.validation.GroupSequence;

@GroupSequence({
        PlanValidationGroups.TitleBlankGroup.class,
        PlanValidationGroups.ContentBlankGroup.class,
        PlanValidationGroups.CostRangeGroup.class,
        PlanValidationGroups.AddressBlankGroup.class,
        PlanValidationGroups.TimeBlankGroup.class
})

public interface PlanValidationSequence {
}