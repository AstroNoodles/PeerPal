package com.github.astronoodles.peerpal.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public enum ErrorType {
    GEN("gén", "Gender"), NUM("núm", "Quantity"), ART("art", "Article"), BEING("b", "Being"),
    VP("v-p", "Subject Conjugation"), VF("v-f", "Verb Form"), VT("v-t", "Verb Tense"), VM("v-m", "Verb Mode"),
    GUST("gust", "Reflexive Verb"), PREF("p-ref", "Reflexive Pronoun"), POI("p-oi", "Indirect Object Pronoun"), POD("p-od", "Indirect Direct Object Pronoun"),
    PREP("prep", "Preposition"), PP("p/p", "Por / Para"), OP("o-p", "Word Order"),
    ORT("ort", "Spelling"), AC("ac", "Accent"), VOC("voc", "Vocabulary"), FC("f-c", "False Cognitive"), ADD("^", ""), ANGL("angl", "Anglicism");

    private final String codeName;
    private final String errorName;
    private static final Map<String, ErrorType> explErrorMap = readExplanationsFromCSV();

    ErrorType(String codeName, String errorName) {
        this.codeName = codeName;
        this.errorName = errorName;
    }

    public String getCodeName() {
        return codeName;
    }

    public String getErrorName() {
        return errorName;
    }

    // better version of method to use - reads from csv file instead of hard-coding from map
    public static Map<String, ErrorType> readExplanationsFromCSV() {
        Map<String, ErrorType> explErrorMap = new HashMap<>(21);
        Path explanationsPath = Paths.get("./src/main/java/com/github/astronoodles/peerpal/" +
                "extras/explanation_error.csv");
        try {
            List<String> csvLines = Files.readAllLines(explanationsPath);
            csvLines.stream().forEach((explanationError) -> {
                String[] explanationErrorArray = explanationError.split(", ");
                System.out.println(explanationErrorArray[0]);
                System.out.println(explanationErrorArray[1]);
                explErrorMap.put(explanationErrorArray[0], ErrorType.valueOf(explanationErrorArray[1].trim()));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return explErrorMap;
    }

}

