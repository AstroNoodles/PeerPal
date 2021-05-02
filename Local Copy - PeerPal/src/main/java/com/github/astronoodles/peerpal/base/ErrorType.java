package com.github.astronoodles.peerpal.base;

import java.util.HashMap;
import java.util.Map;

public enum ErrorType {
        GEN("gén", "Gender"), NUM("núm", "Quantity"), ART("art", "Article"), BEING("b", "Being"),
        VP("v-p", "Subject Conjugation"), VF("v-f", "Verb Form"), VT("v-t", "Verb Tense"), VM("v-m", "Verb Mode"),
        GUST("gust", "Reflexive Verb"), PREF("p-ref", "Reflexive Pronoun"), POI("p-oi", "Indirect Object Pronoun"), POD("p-od", "Indirect Direct Object Pronoun"),
        PREP("prep", "Preposition"), PP("p/p", "Por / Para"), OP("o-p", "Word Order"),
    ORT("ort", "Spelling"), AC("ac", "Accent"), VOC("voc", "Vocabulary"), FC("f-c", "False Cognitive"), ADD("^", ""), ANGL("angl", "Anglicism");

        private final String codeName;
        private final String errorName;

        ErrorType(String codeName, String errorName){
            this.codeName = codeName;
            this.errorName = errorName;
        }

    public String getCodeName() {
        return codeName;
    }
    public String getErrorName() {return errorName;}


    public static ErrorType explanationToError(String explanation){
            Map<String, ErrorType> explErrorMap = new HashMap<>(21);
            explErrorMap.put("concordancia de género", ErrorType.GEN);
            explErrorMap.put("género", ErrorType.VP);
            explErrorMap.put("falta de concordancia de número", ErrorType.NUM);
            explErrorMap.put("Error de concordancia", ErrorType.ART);
            explErrorMap.put("falta de concordancia de persona", ErrorType.VP);
            explErrorMap.put("Pronombre átono sin verbo.", ErrorType.VF);
            explErrorMap.put("mayúsculo", ErrorType.ORT);
            explErrorMap.put("mayúscula", ErrorType.ORT);
            explErrorMap.put("tilde", ErrorType.AC);
            explErrorMap.put("símbolo", ErrorType.ADD);

            for(String keyWord : explErrorMap.keySet()){
                if(explanation.contains(keyWord)){
                    return explErrorMap.get(keyWord);
                }
            }
        return ErrorType.GEN;
    }
}

