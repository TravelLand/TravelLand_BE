package com.travelland.domain.plan;

import com.travelland.constant.PlanVoteDuration;
import com.travelland.domain.member.Member;
import com.travelland.dto.plan.PlanVoteDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt; // 투표 가능기간 설정용

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime modifiedAt;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private PlanVoteDuration planVoteDuration;

    @Column(length = 100)
    private String planVoteTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_a_id")
    private Plan planA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_b_id")
    private Plan planB;

    private int planACount = 0;

    private int planBCount = 0;

    private Boolean isClosed = false;

    private Boolean isDeleted = false;

    public PlanVote(PlanVoteDto.Create request, Plan planA, Plan planB, Member member) {
        this.member = member;
        this.planVoteDuration = request.getPlanVoteDuration();
        this.planVoteTitle = request.getTitle();
        this.planA = planA;
        this.planB = planB;
    }

    public PlanVote update(PlanVoteDto.Update request, Plan planA, Plan planB) {
        this.planVoteDuration = request.getPlanVoteDuration();
        this.planVoteTitle = request.getTitle();
        this.planA = planA;
        this.planB = planB;

        return this;
    }

    public void increaseAVoteCount() {
        this.planACount++;
    }
    public void increaseBVoteCount() {
        this.planBCount++;
    }
    public void decreaseAVoteCount() {
        this.planACount--;
    }
    public void decreaseBVoteCount() {
        this.planBCount--;
    }
    public void changeAtoBVoteCount() {
        this.planACount--;
        this.planBCount++;
    }
    public void changeBtoAVoteCount() {
        this.planBCount--;
        this.planACount++;
    }

    public void close() {
        this.isClosed = true;
    }
    public void delete() {
        this.isDeleted = true;
    }

    // 종료예정시각을 경과했는지 체크
    public boolean checkTimeOut() {
        // 이미 종료되어 있는 경우, 바로 종료상태(true) 반환
        if (isClosed) {
            return true;
        }

        // 아직 종료되지 않은 경우, 시간을 계산해서 종료해야하는지 체크
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime closingTime = createdAt.plus(planVoteDuration.getNumberDuration());
        if (now.isAfter(closingTime)) {
            isClosed = true;
            return true;
        }

        return false;
    }
}
