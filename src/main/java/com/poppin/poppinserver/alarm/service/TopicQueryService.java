package com.poppin.poppinserver.alarm.service;

import com.poppin.poppinserver.alarm.domain.PopupTopic;
import com.poppin.poppinserver.alarm.repository.PopupTopicRepository;
import com.poppin.poppinserver.alarm.usecase.TopicQueryUseCase;
import com.poppin.poppinserver.core.exception.CommonException;
import com.poppin.poppinserver.core.exception.ErrorCode;
import com.poppin.poppinserver.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicQueryService implements TopicQueryUseCase {

    private final PopupTopicRepository popupTopicRepository;

    @Override
    public List<PopupTopic> findPopupTopicByUser(User user) {
        return popupTopicRepository.findByUser(user)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POPUP));
    }
}
