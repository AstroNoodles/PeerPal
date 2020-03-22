package members;

import org.languagetool.JLanguageTool;
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

public class LanguageLearn {

    public static void main(String[] args){
        System.out.println("Hello World From Maven Java");
        new LanguageLearn().languageDetector("hola como estas? Mi nombre es Michael");
    }

    public void languageDetector(String phrase){
        try {
            JLanguageTool tool = new JLanguageTool(new Spanish());
            List<RuleMatch> errors = tool.check(phrase);
            for(RuleMatch error : errors){
                System.out.println(String.format("The error at \"%s\" is at pos %d to pos %d", phrase, error.getFromPos(), error.getToPos()));
                System.out.println(String.format("Would you like to correct it with %s?", error.getSuggestedReplacements()));
            }
        } catch (IOException io){
            io.printStackTrace();
        }
    }
}
