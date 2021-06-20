package eu.quanticol.moonlight.script;

import java.util.*;

public class TypeEnvironment {

    private Map<String,String[]> customTypes;
    private Map<String,MoonLightType> typeEnvironment;
    private Map<String,MoonLightType[]> formulas;
    private Set<String> edges;
    private Set<String> signal;
    private Set<String> location;
    private Set<String> constants;

    public TypeEnvironment() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>());
    }


    public TypeEnvironment(Map<String, String[]> customTypes, Map<String, MoonLightType> typeEnvironment, Map<String, MoonLightType[]> formulas, Set<String> edges, Set<String> signal, Set<String> location, Set<String> constants) {
        this.customTypes = customTypes;
        this.typeEnvironment = typeEnvironment;
        this.formulas = formulas;
        this.edges = edges;
        this.signal = signal;
        this.location = location;
        this.constants = constants;
    }


    public synchronized void recordType(MoonLightScriptParser.ScriptTypeContext scriptTypeContext) {
        String name = scriptTypeContext.name.getText();
        customTypes.put(
                name,
                scriptTypeContext.elements.stream().map(te -> te.name.getText()).toArray(String[]::new)
        );
        MoonLightType custom = MoonLightType.customType(name);
        scriptTypeContext.elements.forEach(te -> typeEnvironment.put(name, custom));
    }

    public synchronized void addEdge(String name, MoonLightType type) {
        add(name, type);
        edges.add(name);
    }

    public synchronized void addLoction(String name, MoonLightType type) {
        add(name, type);
        location.add(name);
    }

    public synchronized void addSignal(String name, MoonLightType type) {
        add(name, type);
        signal.add(name);
    }

    public synchronized void addFormula(String name, MoonLightType[] args) {
        add(name, MoonLightType.BOOLEAN);
        formulas.put(name, args);
    }

    public synchronized void addConstant(String name, MoonLightType type) {
        add(name, type);
        constants.add(name);
    }

    public synchronized void add(String name, MoonLightType type) {
        this.typeEnvironment.put(name, type);
    }

    public boolean exists(String name) {
        return typeEnvironment.containsKey(name);
    }

    public boolean isValidIn(String name, TypeChecker.EvaluationContext evaluationContext) {
        switch (evaluationContext) {
            case NONE:
            case INTERVAL:
                return constants.contains(name);
            case EDGE:
                return edges.contains(name)||constants.contains(name);
            case STATE:
                return constants.contains(name)||signal.contains(name)||location.contains(name)||formulas.containsKey(name);
        }
        return false;
    }

    public int numberOfArguments(String name) {
        MoonLightType[] args = formulas.get(name);
        if (args != null) {
            return args.length;
        } else {
            return 0;
        }
    }

    public MoonLightType[] getArguments(String name) {
        return formulas.getOrDefault(name, new MoonLightType[0]);
    }

    public MoonLightType getTypeOf(String name) {
        return typeEnvironment.getOrDefault(name, MoonLightType.NONE);
    }

    public TypeEnvironment addLocal(Map<String, MoonLightType> localContext) {
        if (localContext.isEmpty()) {
            return this;
        } else {
            HashMap<String, MoonLightType> newTypeEnvironment = new HashMap<>(typeEnvironment);
            newTypeEnvironment.putAll(localContext);
            return new TypeEnvironment(customTypes,newTypeEnvironment,formulas,edges,signal,location,constants);
        }
    }
}
