package eu.quanticol.moonlight.script;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;
import java.util.stream.Collectors;

public class ScriptValidator {

    private final List<MoonLightParseError> errors;
    private final TypeEnvironment typeEnvironment;
    private final Map<String, ParserRuleContext> symbols;
    private boolean isSpatial = true;

    public ScriptValidator() {
        this(Collections.synchronizedList(new LinkedList<>()));
    }

    public ScriptValidator(List<MoonLightParseError> errors) {
        this.errors = errors;
        this.typeEnvironment = new TypeEnvironment();
        this.symbols = new HashMap<>();
    }

    public boolean withErrors() {
        return !errors.isEmpty();
    }

    public List<MoonLightParseError> getErrors() {
        return errors;
    }

    public boolean validate(MoonLightScriptParser.ModelContext ctx) {
        ctx.types.forEach(this::recordCustomType);
        ctx.constants.forEach(this::recordConstant);
        this.recordScriptSignal(ctx.scriptSignal());
        this.recordScriptSpace(ctx.scriptSpace());
        ctx.formulas.forEach(this::recordFormula);
        return !withErrors();
    }

    private void recordFormula(MoonLightScriptParser.ScriptFormulaContext scriptFormulaContext) {
        if (checkForDuplicatedName(scriptFormulaContext.name.getText(), scriptFormulaContext)&&
                scriptFormulaContext.parameters.stream().allMatch(lv -> checkForDuplicatedName(lv.name.getText(), lv))) {
            TypeEnvironment te = typeEnvironment.addLocal(getLocalContext(scriptFormulaContext.parameters));
            TypeChecker tc = new TypeChecker(errors,te, TypeChecker.EvaluationContext.STATE,isSpatial);
            tc.checkBoolean(scriptFormulaContext.formula);
        }

    }

    private Map<String,MoonLightType> getLocalContext(List<MoonLightScriptParser.VariableDeclarationContext> parameters) {
        return parameters.stream().collect(Collectors.toMap(lv -> lv.name.getText(), lv -> MoonLightType.typeOf(lv.type.getText())));
    }

    private void recordScriptSpace(MoonLightScriptParser.ScriptSpaceContext scriptSpace) {
        if (scriptSpace == null) {
            this.isSpatial = false;
        } else {
            //scriptSpace.locationVariables.forEach(this::recordLocationVariable);
            scriptSpace.edgeVariables.forEach(this::recordEdgeVariable);
        }
    }

    private void recordEdgeVariable(MoonLightScriptParser.VariableDeclarationContext variableDeclarationContext) {
        if (checkForDuplicatedName(variableDeclarationContext.name.getText(), variableDeclarationContext)) {
            typeEnvironment.addEdge(variableDeclarationContext.name.getText(), MoonLightType.typeOf(variableDeclarationContext.type.getText()));
            symbols.put(variableDeclarationContext.name.getText(), variableDeclarationContext);
        }
    }

    private void recordLocationVariable(MoonLightScriptParser.VariableDeclarationContext variableDeclarationContext) {
        if (checkForDuplicatedName(variableDeclarationContext.name.getText(), variableDeclarationContext)) {
            typeEnvironment.addLoction(variableDeclarationContext.name.getText(), MoonLightType.typeOf(variableDeclarationContext.type.getText()));
            symbols.put(variableDeclarationContext.name.getText(), variableDeclarationContext);
        }
    }

    private void recordScriptSignal(MoonLightScriptParser.ScriptSignalContext scriptSignal) {
        scriptSignal.signalVariables.forEach(this::recordSignalVariable);
    }

    private void recordSignalVariable(MoonLightScriptParser.VariableDeclarationContext variableDeclarationContext) {
        if (checkForDuplicatedName(variableDeclarationContext.name.getText(),variableDeclarationContext)) {
            typeEnvironment.addSignal(variableDeclarationContext.name.getText(), MoonLightType.typeOf(variableDeclarationContext.type.getText()));
            symbols.put(variableDeclarationContext.name.getText(), variableDeclarationContext);
        }
    }

    private void recordVariable(MoonLightScriptParser.VariableDeclarationContext variableDeclarationContext) {
//        r
//        typeEnvironment.recordVariable(
//                variableDeclarationContext.name.getText(),
//                buildType(variableDeclarationContext.type)
//        );
    }

    private MoonLightType buildType(MoonLightScriptParser.BasicTypeContext type) {
        return null;
        //return MoonLightType.typeOf(type);
    }

    private void recordConstant(MoonLightScriptParser.ScriptConstantContext scriptConstantContext) {
        if (checkForDuplicatedName(scriptConstantContext.name.getText(), scriptConstantContext)) {
            if (TypeChecker.checkType(errors, typeEnvironment, MoonLightType.REAL, scriptConstantContext.value)) {
                typeEnvironment.addConstant(scriptConstantContext.name.getText(),MoonLightType.REAL);
                symbols.put(scriptConstantContext.name.getText(), scriptConstantContext);
            }
        }
    }

    private void recordCustomType(MoonLightScriptParser.ScriptTypeContext scriptTypeContext) {
        if (checkForDuplicatedName(scriptTypeContext.name.getText(),scriptTypeContext)&&
            scriptTypeContext.elements.stream().allMatch(te -> checkForDuplicatedName(te.name.getText(),te))) {
            checkForDuplicatedName(scriptTypeContext.name.getText(),scriptTypeContext);
            scriptTypeContext.elements.forEach(te -> checkForDuplicatedName(te.name.getText(), te));
            typeEnvironment.recordType(scriptTypeContext);
            symbols.put(scriptTypeContext.name.getText(), scriptTypeContext);
            scriptTypeContext.elements.forEach(te -> symbols.put(te.name.getText(), te));
        }
    }


    private boolean checkForDuplicatedName(String name, ParserRuleContext declarationContext) {
        if (symbols.containsKey(name)) {
            errors.add(MoonLightParseError.nameAlreadyDeclared(name,symbols.get(name),declarationContext));
            return false;
        }
        return true;
    }


    public boolean isSpatial() {
        return isSpatial;
    }
}
