package com.poppin.poppinserver.inform.domain;

import com.poppin.poppinserver.core.type.EInformProgress;
import com.poppin.poppinserver.popup.domain.Popup;
import com.poppin.poppinserver.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "manager_inform")
public class ManagerInform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "informer_id")
    private User informerId; // 제보자 id

    @Column(name = "informed_at")
    private LocalDateTime informedAt;  // 제보 일자

    @Column(name = "affiliation")
    private String affiliation; // 소속

    @Column(name = "informer_email")
    private String informerEmail; // 담당자 이메일

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "popup_id")
    private Popup popupId; // 팝업 정보

    @Column(name = "progress")
    @Enumerated(EnumType.STRING)
    private EInformProgress progress; // 처리 상태(NOTEXECUTED | EXECUTING | EXECUTED)

    @Column(name = "executed_at")
    private LocalDateTime executedAt; // 처리 일자

    @Builder
    public ManagerInform(User informerId, String affiliation,
                         String informerEmail, Popup popupId, EInformProgress progress) {
        this.informerId = informerId;
        this.informedAt = LocalDateTime.now();
        this.affiliation = affiliation;
        this.informerEmail = informerEmail;
        this.popupId = popupId;
        this.progress = progress;
    }

    public void update(EInformProgress progress, String affiliation, String informerEmail) {
        this.affiliation = affiliation;
        this.informerEmail = informerEmail;
        this.progress = progress;
        this.executedAt = LocalDateTime.now();
    }
}
