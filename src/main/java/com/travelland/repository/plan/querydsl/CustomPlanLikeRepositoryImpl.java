package com.travelland.repository.plan.querydsl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.travelland.domain.member.Member;
import com.travelland.domain.plan.PlanLike;
import com.travelland.dto.plan.PlanDto;
import com.travelland.dto.plan.PlanLikeScrapDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.travelland.domain.plan.QPlan.plan;
import static com.travelland.domain.plan.QPlanLike.planLike;
import static com.travelland.domain.plan.QPlanScrap.planScrap;

@Repository
@RequiredArgsConstructor
public class CustomPlanLikeRepositoryImpl implements CustomPlanLikeRepository{
    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public List<PlanLikeScrapDto.GetList> getLikeListByMember(Member member, int size, int page){
        return  jpaQueryFactory
                .select(  Projections.constructor(
                        PlanLikeScrapDto.GetList.class,
                        plan.id,
                        plan.title,
                        plan.viewCount,
                        plan.createdAt
                ))
                .from(planLike)
                .where(planLike.member.eq(member), planLike.isDeleted.eq(false))
                .orderBy(planLike.plan.createdAt.desc())
                .limit(size)
                .offset((long) (page - 1) * size)
                .fetch();
    }
}
