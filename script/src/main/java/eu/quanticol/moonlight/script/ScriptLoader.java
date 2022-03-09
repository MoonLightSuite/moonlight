package eu.quanticol.moonlight.script;

import eu.quanticol.moonlight.*;
import eu.quanticol.moonlight.domain.BooleanDomain;
import eu.quanticol.moonlight.domain.DoubleDomain;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.offline.signal.DataHandler;
import eu.quanticol.moonlight.offline.signal.RecordHandler;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.util.*;

public class ScriptLoader {

    private final List<MoonLightParseError> errors;
    private final CharStream source;
    private MoonLightScriptParser.ModelContext model;
    private ScriptValidator validator;

    public static ScriptLoader loaderFromFile(String fileName) throws IOException {
        return new ScriptLoader(CharStreams.fromFileName(fileName));
    }

    public static ScriptLoader loaderFromCode(String code) throws IOException {
        return new ScriptLoader(CharStreams.fromString(code));
    }

    public static MoonLightScript loadFromFile(String fileName) throws IOException, MoonLightScriptLoaderException {
        ScriptLoader loader = ScriptLoader.loaderFromFile(fileName);
        return loader.getScript();
    }

    public static MoonLightScript loadFromCode(String code) throws IOException, MoonLightScriptLoaderException {
        ScriptLoader loader = ScriptLoader.loaderFromCode(code);
        return loader.getScript();
    }

    public ScriptLoader(CharStream source) {
        this.source = source;
        this.errors = Collections.synchronizedList(new LinkedList<>());
    }

    public List<MoonLightParseError> getErrors() {
        return errors;
    }

    public boolean withErrors() {
        return !errors.isEmpty();
    }


    private  boolean loadModel() {
        MoonLightScriptLexer lexer = new MoonLightScriptLexer(this.source);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MoonLightScriptParser parser = new MoonLightScriptParser(tokens);
        parser.addErrorListener(new MoonLightParserErrorListener(this.errors));
        this.model = parser.model();
        return errors.isEmpty();
    }

    private MoonLightScript loadScript() throws MoonLightScriptLoaderException {
        if (!loadModel()||!validate()) {
            throw new MoonLightScriptLoaderException(errors);
        }
        if (validator.isSpatial()) {
            return generateSpatialScript();
        } else {
            return generateTemporalScript();
        }
    }

    private MoonLightScript generateSpatialScript() {
        MoonLightEnumerationRepository repository = getHandlers();
        Map<String, Double> constants = evalConstants(repository);
        RecordHandler signalHandler = getRecordHandler(repository, model.scriptSignal().signalVariables);
        RecordHandler edgeHandler = getRecordHandler(repository, model.scriptSpace().edgeVariables);
        SignalDomain<?> domain = getDomain();
        Map<String, RecordHandler> formulaParameters = getFormulaParameters(repository);
        Map<String, SpatialTemporalMonitorProducer> producers = new HashMap<>();
        DefaultNameResolver resolver = new DefaultNameResolver(repository,constants);
        String defaultMonitor = null;
        for (MoonLightScriptParser.ScriptFormulaContext f: this.model.formulas) {
            SpatialTemporalMonitoringGenerator evaluator = new SpatialTemporalMonitoringGenerator(producers,resolver,formulaParameters, formulaParameters.get(f.name.getText()), signalHandler, edgeHandler);
            producers.put(f.name.getText(), evaluator.eval(f.formula));
            if ((defaultMonitor == null)||(f.isDefault != null)) {
                defaultMonitor = f.name.getText();
            }
        }
        SpatialTemporalMonitorDefinition[] definitions = producers.entrySet().stream().map(ke -> new SpatialTemporalMonitorDefinition(ke.getKey(), formulaParameters.get(ke.getKey()), signalHandler, edgeHandler, producers.get(ke.getKey()))).toArray(SpatialTemporalMonitorDefinition[]::new);
        return new MoonLightSpatialTemporalScript(defaultMonitor, domain, definitions);
    }

    private MoonLightScript generateTemporalScript() {
        MoonLightEnumerationRepository repository = getHandlers();
        Map<String, Double> constants = evalConstants(repository);
        RecordHandler signalHandler = getRecordHandler(repository, model.scriptSignal().signalVariables);
        SignalDomain<?> domain = getDomain();
        Map<String, RecordHandler> formulaParameters = getFormulaParameters(repository);
        Map<String, TemporalMonitorProducer> producers = new HashMap<>();
        DefaultNameResolver resolver = new DefaultNameResolver(repository,constants);
        String defaultMonitor = null;
        for (MoonLightScriptParser.ScriptFormulaContext f: this.model.formulas) {
            TemporalMonitoringGenerator evaluator = new TemporalMonitoringGenerator(producers,resolver,formulaParameters,formulaParameters.get(f.name.getText()),signalHandler);
            producers.put(f.name.getText(), evaluator.eval(f.formula));
            if ((defaultMonitor == null)||(f.isDefault != null)) {
                defaultMonitor = f.name.getText();
            }
        }
        TemporalMonitorDefinition[] definitions = producers.entrySet().stream().map(ke -> new TemporalMonitorDefinition(ke.getKey(), formulaParameters.get(ke.getKey()), signalHandler, producers.get(ke.getKey()))).toArray(TemporalMonitorDefinition[]::new);
        return new MoonLightTemporalScript(defaultMonitor,domain,definitions);
    }

    private SignalDomain<?> getDomain() {
        String domainName = model.scriptDomain().semiring.getText();
        if (domainName.equals("minmax")) {
            return new DoubleDomain();
        }
        if (domainName.equals("boolean")) {
            return new BooleanDomain();
        }
        throw new IllegalStateException(String.format("Declared domain %s is unknown!",domainName));
    }

    private Map<String, RecordHandler> getFormulaParameters(MoonLightEnumerationRepository repository) {
        HashMap<String, RecordHandler> map = new HashMap<>();
        model.formulas.forEach(f -> {
            if ((f.parameters!=null)&&(f.parameters.size()>0)) {
                map.put(f.name.getText(),getRecordHandler(repository, f.parameters));
            } else {
                map.put(f.name.getText(),new RecordHandler());
            }
        });
        return map;
    }

    private RecordHandler getRecordHandler(MoonLightEnumerationRepository repository, List<MoonLightScriptParser.VariableDeclarationContext> parameters) {
        DataHandler<?>[] handlers = new DataHandler<?>[parameters.size()];
        Map<String, Integer> indexes = new HashMap<>();
        int counter = 0;
        for (MoonLightScriptParser.VariableDeclarationContext v: parameters) {
            indexes.put(v.name.getText(), counter);
            handlers[counter] = getHandler(repository,v.type);
            counter++;
        }
        return new RecordHandler(indexes, handlers);
    }

    private DataHandler<?> getHandler(MoonLightEnumerationRepository repository, MoonLightScriptParser.BasicTypeContext type) {
        if (type instanceof MoonLightScriptParser.IntegerTypeContext) {
            return DataHandler.INTEGER;
        }
        if (type instanceof MoonLightScriptParser.RealTypeContext) {
            return DataHandler.REAL;
        }
        if (type instanceof MoonLightScriptParser.BooleanTypeContext) {
            return DataHandler.BOOLEAN;
        }
        if (type instanceof MoonLightScriptParser.ReferenceTypeContext) {
            return repository.getHandler(((MoonLightScriptParser.ReferenceTypeContext) type).type.getText());
        }
        return null;
    }

    private Map<String, Double> evalConstants(MoonLightEnumerationRepository repository) {
        HashMap<String, Double> values = new HashMap<>();
        for( MoonLightScriptParser.ScriptConstantContext c: this.model.constants) {
            ExpressionEvaluator ev = new ExpressionEvaluator(errors, new DefaultNameResolver(repository, values));
            double res = ev.eval(c.value);
            if (!Double.isNaN(res)) {
                values.put(c.name.getText(), res);
            }
        }
        return values;
    }

    private MoonLightEnumerationRepository getHandlers() {
        MoonLightEnumerationRepository repository = new MoonLightEnumerationRepository();
        for (MoonLightScriptParser.ScriptTypeContext customType: model.types) {
            repository.add(customType.name.getText(), customType.elements.stream().map(te -> te.name.getText()).toArray(String[]::new));
        }
        return repository;
    }

    private boolean validate() {
        validator = new ScriptValidator(this.errors);
        return validator.validate(this.model);
    }


    public MoonLightScript getScript() throws MoonLightScriptLoaderException {
        return loadScript();
    }
}
