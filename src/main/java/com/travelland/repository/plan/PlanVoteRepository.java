package com.travelland.repository.plan;

import com.travelland.domain.plan.Plan;
import com.travelland.domain.plan.PlanVote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanVoteRepository extends JpaRepository<PlanVote, Long> {
    Optional<PlanVote> findByIdAndIsDeleted(Long planVoteId, boolean isDeleted);

    Optional<PlanVote> findByIdAndIsDeletedAndIsClosed(Long planVoteId, boolean isDeleted, boolean isClosed);

    List<PlanVote> findAllByIsDeletedAndIsClosed(boolean isDeleted, boolean isClosed);

    Page<PlanVote> findAllByIsDeleted(Pageable pageable, boolean isDeleted);

    Page<PlanVote> findAllByIsDeletedAndMemberId(Pageable pageable, boolean isDeleted, Long memberId);

    List<PlanVote> findAllByPlanAOrPlanB(Plan planA, Plan planB);

    List<PlanVote> findAllByIsDeletedAndPlanAOrIsDeletedAndPlanB(boolean isDeletedA, Plan planA, boolean isDeletedB, Plan planB);

    List<PlanVote> findAllByPlanAAndIsDeletedOrPlanBAndIsDeleted(Plan planA, boolean isDeletedA, Plan planB, boolean isDeletedB);
}
