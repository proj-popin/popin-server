package com.poppin.poppinserver.repository;

import com.poppin.poppinserver.domain.Popup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PopupRepository extends JpaRepository<Popup, Long> {
    @Query("SELECT p FROM Popup p JOIN Interest i ON p.id = i.popup.id " +
            "WHERE p.operationStatus = 'OPERATING' AND i.createdAt >= :startOfDay AND i.createdAt < :endOfDay " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(i.popup.id) DESC, p.viewCnt DESC ")
    List<Popup> findTopOperatingPopupsByInterestAndViewCount(@Param("startOfDay") LocalDateTime startOfDay,
                                                             @Param("endOfDay") LocalDateTime endOfDay,
                                                             Pageable pageable);

    @Query("SELECT p FROM Popup p " +
            "WHERE p.operationStatus = 'OPERATING' " +
            "ORDER BY p.openDate DESC, p.id ")
    List<Popup> findNewOpenPopupByAll(Pageable pageable);

    @Query("SELECT p FROM Popup p " +
            "WHERE p.operationStatus = 'OPERATING' " +
            "ORDER BY p.closeDate, p.id ")
    List<Popup> findClosingPopupByAll(Pageable pageable);

    @Query("SELECT p FROM Popup p WHERE p.name LIKE %:text% OR p.introduce LIKE %:text%")
    Page<Popup> findByTextInNameOrIntroduce(String text, Pageable pageable);
}
