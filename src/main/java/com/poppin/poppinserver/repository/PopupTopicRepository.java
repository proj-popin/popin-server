package com.poppin.poppinserver.repository;

import com.poppin.poppinserver.domain.Popup;
import com.poppin.poppinserver.domain.PopupTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface PopupTopicRepository extends JpaRepository<PopupTopic, Long> {

    @Query("SELECT PT FROM PopupTopic PT WHERE PT.tokenId = :tokenId AND PT.topicCode = :code AND PT.popup = :popupId")
    PopupTopic findByTokenAndTopic(@Param("tokenId") Long tokenId , @Param("code") String code, @Param("popupId") Popup popupId);

    PopupTopic findByTokenIdAndTopicCodeAndPopupId(@Param("tokenId") Long tokenId , @Param("code") String code, @Param("popupId") Popup popupId);

}
