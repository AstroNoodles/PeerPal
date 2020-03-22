import java.util.HashMap;
import java.util.Map;

public enum ErrorType {
        GEN("gén"), NUM("núm"), ART("art"), BEING("b"), VP("v-p"), VF("v-f"), VT("v-t"), VM("v-m"),
        GUST("gust"), PREF("p-ref"), POI("p-oi"), POD("p-od"), PREP("prep"), PP("p/p"), OP("o-p"),
        ORT("ort"), AC("ac"), VOC("voc"), FC("f-c"), ADD("^"), ANGL("angl");

        private String name;

        ErrorType(String name){
            this.name = name;
        }

    public String getName() {
        return name;
    }


    public static ErrorType explanationToError(String explanation){
            Map<String, ErrorType> explErrorMap = new HashMap<>(21);
            explErrorMap.put("género", ErrorType.GEN);
            explErrorMap.put("mayusculo", ErrorType.ORT);
            explErrorMap.put("tilde", ErrorType.AC);
            explErrorMap.put("símbolo", ErrorType.ADD);
            explErrorMap.put("concordancia de género", ErrorType.VP);


            for(String keyWord : explErrorMap.keySet()){
                if(explanation.contains(keyWord)){
                    return explErrorMap.get(keyWord);
                }
            }
        return ErrorType.ORT;
    }
}

