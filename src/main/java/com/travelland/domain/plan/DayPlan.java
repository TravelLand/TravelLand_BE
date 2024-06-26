package com.travelland.domain.plan;

import com.travelland.dto.plan.DayPlanDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DayPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private Boolean isDeleted = false;

    public DayPlan(DayPlanDto.CreateAllInOne request, Plan plan) {
        this.date = request.getDate();
        this.plan = plan;
    }

    public DayPlan update(DayPlanDto.UpdateAllInOne request) {
        this.date = request.getDate();

        return this;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
