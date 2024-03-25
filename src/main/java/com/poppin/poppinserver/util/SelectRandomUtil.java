package com.poppin.poppinserver.util;

import com.poppin.poppinserver.domain.PreferedPopup;
import com.poppin.poppinserver.domain.TastePopup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class SelectRandomUtil {
    public String selectRandomPreference(PreferedPopup preferedPopup) {
        List<String> preferences = new ArrayList<>();

        // PreferedPopup의 모든 속성을 검사하고 true로 설정된 항목들을 리스트에 추가
        if (preferedPopup.getMarket()) preferences.add("market");
        if (preferedPopup.getDisplay()) preferences.add("display");
        if (preferedPopup.getExperience()) preferences.add("experience");
        if (preferedPopup.getWantFree()) preferences.add("wantFree");

        // 랜덤하게 하나의 항목을 선택
        if (preferences.isEmpty()) {
            return null; // 또는 기본값
        }
        Random random = new Random();
        int randomIndex = random.nextInt(preferences.size());

        return preferences.get(randomIndex);
    }

    public String selectRandomTaste(TastePopup tastePopup) {
        List<String> tastes = new ArrayList<>();

        // TastePopup의 모든 속성을 검사하고 true로 설정된 항목들을 리스트에 추가
        if (tastePopup.getFasionBeauty()) tastes.add("fasionBeauty");
        if (tastePopup.getCharacters()) tastes.add("characters");
        if (tastePopup.getFoodBeverage()) tastes.add("foodBeverage");
        if (tastePopup.getWebtoonAni()) tastes.add("webtoonAni");
        if (tastePopup.getInteriorThings()) tastes.add("interiorThings");
        if (tastePopup.getMovie()) tastes.add("movie");
        if (tastePopup.getMusical()) tastes.add("musical");
        if (tastePopup.getSports()) tastes.add("sports");
        if (tastePopup.getGame()) tastes.add("game");
        if (tastePopup.getItTech()) tastes.add("itTech");
        if (tastePopup.getKpop()) tastes.add("kpop");
        if (tastePopup.getAlchol()) tastes.add("alchol");
        if (tastePopup.getAnimalPlant()) tastes.add("animalPlant");

        // 랜덤하게 하나의 항목을 선택
        if (tastes.isEmpty()) {
            return null; // 또는 기본값
        }
        Random random = new Random();
        int randomIndex = random.nextInt(tastes.size());

        return tastes.get(randomIndex);
    }
}
