package eu.quanticol.moonlight.script;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.LinkedList;
import java.util.List;

public class MoonLightParserErrorListener extends BaseErrorListener {

    private final List<MoonLightParseError> errors;

    public MoonLightParserErrorListener() {
        this(new LinkedList<>());
    }

    public MoonLightParserErrorListener(List<MoonLightParseError> errors) {
        this.errors = errors;
    }

    public boolean withErrors() {
        return !errors.isEmpty();
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        errors.add(new MoonLightParseError(msg,line,charPositionInLine));
    }

    public List<MoonLightParseError> getSyntaxErrorList() {
        return errors;
    }

}
