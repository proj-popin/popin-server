package com.poppin.poppinserver.interest.domain;

import com.poppin.poppinserver.popup.domain.Popup;
import com.poppin.poppinserver.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "interest")
public class Interest {
    @EmbeddedId
    private InterestId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @MapsId("popupId")
    @JoinColumn(name = "popup_id", referencedColumnName = "id")
    private Popup popup;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // InterestId 정적 중첩 클래스 정의
    // 복합키 생성자
    @Embeddable
    @Getter
    public static class InterestId implements Serializable {
        private Long userId;
        private Long popupId;

        public InterestId() {
        }

        public InterestId(Long userId, Long popupId) {
            this.userId = userId;
            this.popupId = popupId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InterestId that = (InterestId) o;
            return Objects.equals(userId, that.userId) &&
                    Objects.equals(popupId, that.popupId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, popupId);
        }
    }

    @Builder
    public Interest(User user, Popup popup) {
        this.id = new InterestId(user.getId(), popup.getId());
        this.user = user;
        this.popup = popup;
        this.createdAt = LocalDateTime.now();
    }
}