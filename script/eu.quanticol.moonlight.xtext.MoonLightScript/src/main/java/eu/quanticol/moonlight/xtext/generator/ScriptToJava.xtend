package eu.quanticol.moonlight.xtext.generator

import eu.quanticol.moonlight.xtext.moonLightScript.AndExpression
import eu.quanticol.moonlight.xtext.moonLightScript.BasicType
import eu.quanticol.moonlight.xtext.moonLightScript.BinaryMathFunction
import eu.quanticol.moonlight.xtext.moonLightScript.BooleanSemiring
import eu.quanticol.moonlight.xtext.moonLightScript.BooleanType
import eu.quanticol.moonlight.xtext.moonLightScript.Expression
import eu.quanticol.moonlight.xtext.moonLightScript.FalseLiteral
import eu.quanticol.moonlight.xtext.moonLightScript.FormulaDeclaration
import eu.quanticol.moonlight.xtext.moonLightScript.IfThenElseExpression
import eu.quanticol.moonlight.xtext.moonLightScript.IntegerLiteral
import eu.quanticol.moonlight.xtext.moonLightScript.IntegerType
import eu.quanticol.moonlight.xtext.moonLightScript.MinMaxSemiring
import eu.quanticol.moonlight.xtext.moonLightScript.MinusExpression
import eu.quanticol.moonlight.xtext.moonLightScript.Model
import eu.quanticol.moonlight.xtext.moonLightScript.ModuloExpression
import eu.quanticol.moonlight.xtext.moonLightScript.MulOrDivExpression
import eu.quanticol.moonlight.xtext.moonLightScript.NotExpression
import eu.quanticol.moonlight.xtext.moonLightScript.OrExpression
import eu.quanticol.moonlight.xtext.moonLightScript.PlusExpression
import eu.quanticol.moonlight.xtext.moonLightScript.RealLiteral
import eu.quanticol.moonlight.xtext.moonLightScript.RealType
import eu.quanticol.moonlight.xtext.moonLightScript.ReferencedValue
import eu.quanticol.moonlight.xtext.moonLightScript.RelationExpression
import eu.quanticol.moonlight.xtext.moonLightScript.SemiringExpression
import eu.quanticol.moonlight.xtext.moonLightScript.StrelAlwaysFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelAndFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelAtomicFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelEscapeFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelEventuallyFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelEverywhereFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelFormulaReference
import eu.quanticol.moonlight.xtext.moonLightScript.StrelHistoricallyFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelImplyFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelNotFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelOnceFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelOrFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelReachFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelSinceFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelSomewhereFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelUntilFormula
import eu.quanticol.moonlight.xtext.moonLightScript.TrueLiteral
import eu.quanticol.moonlight.xtext.moonLightScript.TypeReference
import eu.quanticol.moonlight.xtext.moonLightScript.UnaryMathFunction
import eu.quanticol.moonlight.xtext.moonLightScript.UnaryMinusExpression
import eu.quanticol.moonlight.xtext.moonLightScript.UnaryPlusExpression
import eu.quanticol.moonlight.xtext.moonLightScript.VariableDeclaration
import org.eclipse.emf.common.util.EList

import static extension org.eclipse.xtext.EcoreUtil2.*

class ScriptToJava {
	
	val edgeRecord = "edge"
	val signalRecord = "signal"
	val edgeRecordHandler = "_edge_handler_"
	val outputHandler = "_output_handler_"
	val signalHandler = "_signal_handler_"
	val signalDomain = "_signal_domain_"
	
	def getJavaCode( Model model, String packageName , String className ) {
		'''
		/**
		 * Code Generate by MoonLight tool.
		 */ 
		package «packageName»;
		
		import eu.quanticol.moonlight.*;
		import eu.quanticol.moonlight.monitoring.temporal.*;
		import eu.quanticol.moonlight.monitoring.spatialtemporal.*;
		import eu.quanticol.moonlight.signal.*;
		import eu.quanticol.moonlight.util.*;
		import eu.quanticol.moonlight.*;
		import eu.quanticol.moonlight.formula.*;
		import java.util.function.Function;
		import java.util.HashSet;
		import java.util.HashMap;
		
		
		public class «className» extends «IF model.isIsSpatial»MoonLightSpatialTemporalScript«ELSE»MoonLightTemporalScript«ENDIF» {			
			
			«FOR generatedEnum: model.types»
			public static enum «generatedEnum.name» {
				«FOR e: generatedEnum.elements SEPARATOR ','»	
				«e.name»
				«ENDFOR»
			}
			«ENDFOR»
						
			private final SignalDomain<«model.semiring.javaTypeOf»> «signalDomain» = new «model.semiring.domainOf»();

			«IF model.isIsSpatial» 
			private RecordHandler «edgeRecordHandler» = generateEdgesRecordHandler();
			
			private static RecordHandler generateEdgesRecordHandler() {
				«model.edgeVariables.recordHandlerCreationCode»
			}
			«ENDIF»
			
			private DataHandler<«model.semiring.javaTypeOf»> «outputHandler» = «model.semiring.dataHandlerOf»;
			
			
			private RecordHandler «signalHandler» = generateSignalRecordHandler();
					
			private static RecordHandler generateSignalRecordHandler() {
				«model.signalVariables.recordHandlerCreationCode»
			}
			
			«FOR f: model.formulas»
			«f.generateGenerateFormulaBuilderDeclaration(model)»
			«ENDFOR»
			
			«FOR f: model.formulas»
			«f.generateMonitorDataHandlers»
			«ENDFOR»
			
			«FOR f: model.formulas»
			«f.generateMonitorDeclaration(model)»
			«ENDFOR»
			
			public «className»() {
				super( new String[] { 
					«FOR f: model.formulas SEPARATOR ','»
					"«f.name»"
					«ENDFOR»					
				});	
			}
		
		«IF model.isIsSpatial»
			public SpatialTemporalScriptComponent<?> selectSpatialTemporalComponent( String name ) {
				«FOR f: model.formulas»
				if ("«f.name»".equals( name ) ) {
					return 	MONITOR_«f.name»; 
				}
				«ENDFOR»
				return null;
			}
			
			public SpatialTemporalScriptComponent<?> selectDefaultSpatialTemporalComponent( ) {
				return «generateReferenceToDefaultFormula(model.formulas)»;
			}
		«ELSE»			
			@Override
			public TemporalScriptComponent<?> selectTemporalComponent( String name ) {
				«FOR f: model.formulas»
				if ("«f.name»".equals( name ) ) {
					return 	MONITOR_«f.name»; 
				}
				«ENDFOR»
				return «generateReferenceToDefaultFormula(model.formulas)»;					
			}				

			public TemporalScriptComponent<?> selectDefaultTemporalComponent( ) {
				return «generateReferenceToDefaultFormula(model.formulas)»;
			}
		«ENDIF»						
		
		}
		'''
		
		
	}
	
	def generateReferenceToDefaultFormula( Iterable<FormulaDeclaration> list ) {
		var f = list.findFirst[it.isIsDefault];
		if (f === null) {
			f = list.get(0)
		}
		if (f === null) {
			return '''null'''
		} else {
			return '''MONITOR_«f.name»'''
		}
	}
	
	def CharSequence generateMonitorDataHandlers(FormulaDeclaration formulaDeclaration) {
		'''
		private RecordHandler «formulaDeclaration.name.parametersRecordHandlerName» = generate«formulaDeclaration.name»ParametersRecordHandler();
		
		private static RecordHandler generate«formulaDeclaration.name»ParametersRecordHandler() {
			«formulaDeclaration.parameters.recordHandlerCreationCode»
		}
		'''
	}
	
	def getDataHandlerOf(SemiringExpression e) {
		switch e {
//		TropicalSemiring: '''Double'''
		MinMaxSemiring: '''DataHandler.REAL'''
		BooleanSemiring: '''DataHandler.BOOLEAN'''
//		PairSemiring: '''Pair<«e.left.javaTypeOf»,«e.right.javaTypeOf»>'''
		default: '''?'''			
		}
	}
	
	def dataOutputHandlerName(String string) {
		return string+"_output_data_handler_";
	}
	
	def recordHandlerCreationCode(EList<VariableDeclaration> variableList) {
		'''
		int counter = 0;
		HashMap<String,Integer> variableIndex = new HashMap<>();
		«FOR v: variableList»
		variableIndex.put( "«v.name»" , counter++ );
		«ENDFOR»
		return new RecordHandler( variableIndex «FOR v:variableList», «v.type.generateDataHandler»«ENDFOR»);
		'''
	}
	
	def getGenerateDataHandler(BasicType t) {
		switch t {
			IntegerType: '''DataHandler.INTEGER'''
			RealType: '''DataHandler.REAL'''
			BooleanType: '''DataHandler.BOOLEAN'''
			TypeReference: '''null'''
		}
		
	}
	
	def signalRecordHandlerName(String string) {
		return string+"_signal_handler_";
	}

	def parametersRecordHandlerName(String string) {
		return string+"_parameters_handler_";
	}
	
	def edgeRecordHandlerName(String string) {
		return string+"_edge_handler_";
	}

	def getDomainVariable(String name) {
		return "_domain_"+name
	}

	def  generateGenerateFormulaBuilderDeclaration(FormulaDeclaration f, Model model) {
		if (model.isIsSpatial) {
			'''
			private SpatialTemporalMonitor<Record,Record,«model.semiring.javaTypeOf»> «monitorSubFormulaName(f.name)»( Record parameters ) {				
				return «monitorSubFormulaName(f.name)»( 
					«FOR p:f.parameters SEPARATOR ','»
					((«p.type.basicTypeOf») parameters.get(«f.parameters.indexOf(p)»,«p.type.toJavaType»))
					«ENDFOR»					
				);				
			}
			
			private SpatialTemporalMonitor<Record,Record,«model.semiring.javaTypeOf»> «monitorSubFormulaName(f.name)»( 
				«FOR p:f.parameters SEPARATOR ','»
				«p.type.basicTypeOf» «p.name»
				«ENDFOR»
			) {
				return «f.formula.SpatialTemporalMonitorCode(f.name,signalDomain)»;	
			}			
			'''
		} else {
			'''
			private TemporalMonitor<Record,«model.semiring.javaTypeOf»> «monitorSubFormulaName(f.name)» ( Record parameters ) {
				return «monitorSubFormulaName(f.name)»( 
					«FOR p:f.parameters SEPARATOR ','»
					((«p.type.basicTypeOf») parameters.get(«f.parameters.indexOf(p)»,«p.type.toJavaType»))
					«ENDFOR»					
				);				
			}
						
			private TemporalMonitor<Record,«model.semiring.javaTypeOf»> «monitorSubFormulaName(f.name)»( 
				«FOR p:f.parameters SEPARATOR ','»
				«p.type.basicTypeOf» «p.name»
				«ENDFOR»
			) {
				return «f.formula.temporalMonitorCode(f.name,signalDomain)»;	
			}
			'''
		}
	}
	
	
	def  generateMonitorDeclaration(FormulaDeclaration formulaDeclaration, Model model) {
		if (model.isIsSpatial) {
			'''private SpatialTemporalScriptComponent<«model.semiring.javaTypeOf»> MONITOR_«formulaDeclaration.name» = new SpatialTemporalScriptComponent<>(
				"«formulaDeclaration.name»" ,
				«edgeRecordHandler» ,
				«signalHandler» ,
				«outputHandler» ,
				«formulaDeclaration.name.parametersRecordHandlerName» ,
				r -> «monitorSubFormulaName(formulaDeclaration.name)»( r )	
			);'''
		} else {
			'''private TemporalScriptComponent<«model.semiring.javaTypeOf»> MONITOR_«formulaDeclaration.name» = new TemporalScriptComponent<>(
				"«formulaDeclaration.name»" ,
				«signalHandler» ,
				«outputHandler» ,
				«formulaDeclaration.name.parametersRecordHandlerName» ,
				r -> «monitorSubFormulaName(formulaDeclaration.name)»( r )	
			);'''
		}
	}
	
	def CharSequence javaTypeOf(SemiringExpression e) {
		switch e {
//		TropicalSemiring: '''Double'''
		MinMaxSemiring: '''Double'''
		BooleanSemiring: '''Boolean'''
//		PairSemiring: '''Pair<«e.left.javaTypeOf»,«e.right.javaTypeOf»>'''
		default: '''?'''			
		}
	}

	def CharSequence toJavaType( BasicType t ) {
		switch t {
			IntegerType: '''Integer.class'''
			RealType: '''Double.class'''
			BooleanType: '''Boolean.class'''
			TypeReference: '''«t.type.name».class'''
		}
	}	

	def CharSequence basicTypeOf( BasicType t ) {
		switch t {
			IntegerType: '''int'''
			RealType: '''double'''
			BooleanType: '''boolean'''
			TypeReference: '''«t.type.name»'''
		}
	}	


	def CharSequence domainOf(SemiringExpression e) {
		switch e {
//		TropicalSemiring: '''Double'''
		MinMaxSemiring: '''DoubleDomain'''
		BooleanSemiring: '''BooleanDomain'''
//		PairSemiring: '''Pair<«e.left.javaTypeOf»,«e.right.javaTypeOf»>'''
		default: '''?'''			
		}
	}
	
	def CharSequence monitorSubFormulaName(String formulaName ) {
		'''_FORMULA_«formulaName»'''
	}

	def dispatch CharSequence temporalMonitorCode(StrelFormula f, String prefix, String domain) {
		throw new IllegalArgumentException("Unexpected formula in temporal monitoring ("+f.class+") in monitor "+prefix);
	} 
	
	def dispatch CharSequence temporalMonitorCode(StrelOrFormula f, String prefix, String domain) {
		'''
		TemporalMonitor.orMonitor( 
			«f.left.temporalMonitorCode(prefix,domain)» ,
			«domain» , 
			«f.right.temporalMonitorCode(prefix,domain)»
		)
		'''
	}
	
	def dispatch CharSequence temporalMonitorCode(StrelImplyFormula f, String prefix, String domain) {
		'''
		TemporalMonitor.impliesMonitor( 
			«f.left.temporalMonitorCode(prefix,domain)» ,
			«domain» , 
			«f.right.temporalMonitorCode(prefix,domain)»
		)
		'''
	}
	

	def dispatch CharSequence temporalMonitorCode(StrelAndFormula f, String prefix, String domain) {
		'''
		TemporalMonitor.andMonitor( 
			«f.left.temporalMonitorCode(prefix,domain)» ,
			«domain» , 
			«f.right.temporalMonitorCode(prefix,domain)»
		)
		'''
	}

	def dispatch CharSequence temporalMonitorCode(StrelSinceFormula f, String prefix, String domain) {
		'''
		TemporalMonitor.sinceMonitor( 
			«f.left.temporalMonitorCode(prefix,domain)» , 
			«IF f.interval !== null»
			new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
			«ENDIF»
			«f.right.temporalMonitorCode(prefix,domain)» ,
			«domain» 
		)
		'''
	}
	
	def dispatch CharSequence temporalMonitorCode(StrelUntilFormula f, String prefix, String domain) {
		'''
		TemporalMonitor.untilMonitor( 
			«f.left.temporalMonitorCode(prefix,domain)» , 
			«IF f.interval !== null»
			new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
			«ENDIF»
			«f.right.temporalMonitorCode(prefix,domain)» ,
			«domain» 
		)
		'''
	}
	
	def dispatch CharSequence temporalMonitorCode(StrelAtomicFormula f, String prefix, String domain) {
		'''
		TemporalMonitor.atomicMonitor( 
			«signalRecord» -> «f.atomic.buildAtomicFormula»
		)
		'''
	}

	def dispatch CharSequence temporalMonitorCode(StrelNotFormula f, String prefix, String domain) {
		'''
		TemporalMonitor.notMonitor( 
			«f.argument.temporalMonitorCode(prefix,domain)» ,
			«domain»
		)
		'''
	}

	def dispatch CharSequence temporalMonitorCode(StrelEventuallyFormula f, String prefix, String domain) {
		'''
		TemporalMonitor.eventuallyMonitor( 
			«f.argument.temporalMonitorCode(prefix,domain)»,
			«domain»«IF f.interval !== null»
			, new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»)
			«ENDIF»
		)
		'''		
	}	

	def dispatch CharSequence temporalMonitorCode(StrelAlwaysFormula f, String prefix, String domain) {
		'''
		TemporalMonitor.globallyMonitor( 
			«f.argument.temporalMonitorCode(prefix,domain)»,
			«domain»«IF f.interval !== null»
			, new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»)
			«ENDIF»
		)
		'''		
	}	
	
	def dispatch CharSequence temporalMonitorCode(StrelHistoricallyFormula f, String prefix, String domain) {
		'''
		TemporalMonitor.historicallyMonitor( 
			«f.argument.temporalMonitorCode(prefix,domain)»,
			«domain»«IF f.interval !== null»
			, new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»)
			«ENDIF»
		)
		'''		
	}	

	def dispatch CharSequence temporalMonitorCode(StrelOnceFormula f, String prefix, String domain) {
		'''
		TemporalMonitor.onceMonitor( 
			«f.argument.temporalMonitorCode(prefix,domain)»,
			«domain»«IF f.interval !== null»
			, new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»)
			«ENDIF»
		)
		'''		
	}	
	
	
	def dispatch CharSequence temporalMonitorCode(StrelFormulaReference f , String prefix, String domain) {
		'''
		_FORMULA_«f.reference.name»( «FOR p:f.arguments SEPARATOR ','»«p.expressionToJava»«ENDFOR» )
		'''		
	}


	def dispatch CharSequence SpatialTemporalMonitorCode(StrelFormula f, String prefix, String domain) {
	  throw new IllegalArgumentException("Unexpected formula in temporal monitoring ("+f.class+") in monitor "+prefix);
	} 
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelOrFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.orMonitor( 
	    «f.left.SpatialTemporalMonitorCode(prefix,domain)» ,
	    «domain» , 
	    «f.right.SpatialTemporalMonitorCode(prefix,domain)»
	  )
	  '''
	}
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelImplyFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.impliesMonitor( 
	    «f.left.SpatialTemporalMonitorCode(prefix,domain)» ,
	    «domain» , 
	    «f.right.SpatialTemporalMonitorCode(prefix,domain)»
	  )
	  '''
	}
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelNotFormula f, String prefix, String domain) {
		'''
		SpatialTemporalMonitor.notMonitor( 
			«f.argument.SpatialTemporalMonitorCode(prefix,domain)» ,
			«domain»
		)
		'''
	}

	
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelAndFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.andMonitor( 
	    «f.left.SpatialTemporalMonitorCode(prefix,domain)» ,
	    «domain» , 
	    «f.right.SpatialTemporalMonitorCode(prefix,domain)»
	  )
	  '''
	}
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelSinceFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.sinceMonitor( 
	    «f.left.SpatialTemporalMonitorCode(prefix,domain)» , 
	    «IF f.interval !== null»
	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    «ENDIF»
	    «f.right.SpatialTemporalMonitorCode(prefix,domain)» ,
	    «domain» 
	  )
	  '''
	}
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelUntilFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.untilMonitor( 
	    «f.left.SpatialTemporalMonitorCode(prefix,domain)» , 
	    «IF f.interval !== null»
	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    «ENDIF»
	    «f.right.SpatialTemporalMonitorCode(prefix,domain)» ,
	    «domain» 
	  )
	  '''
	}
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelAtomicFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.atomicMonitor( 
	    «signalRecord» -> «f.atomic.buildAtomicFormula»
	  )
	  '''
	}
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelEventuallyFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.eventuallyMonitor( 
	    «f.argument.SpatialTemporalMonitorCode(prefix,domain)»,«IF f.interval !== null»
	    	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    	    «ENDIF»
	    	    «domain»
	    	  )
	  '''
	}	
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelAlwaysFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.globallyMonitor( 
	    «f.argument.SpatialTemporalMonitorCode(prefix,domain)»,«IF f.interval !== null»
	    	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    	    «ENDIF»
	    	    «domain»
	    	  )
	  '''
	 }	
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelHistoricallyFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.historicallyMonitor( 
	    «f.argument.SpatialTemporalMonitorCode(prefix,domain)»,«IF f.interval !== null»
	    	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    	    «ENDIF»
	    	    «domain»
	    	  )
	  '''
	}	
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelOnceFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.onceMonitor( 
	    «f.argument.SpatialTemporalMonitorCode(prefix,domain)»,«IF f.interval !== null»
	    	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    	    «ENDIF»
	    	    «domain»
	    	  )
	  '''
	}	
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelEscapeFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.escapeMonitor( 
	    «f.argument.SpatialTemporalMonitorCode(prefix,domain)»,
	    m -> DistanceStructure.buildDistanceStructure(m, 
	    	«edgeRecord» -> (double) «IF f.distanceExpression !== null»«f.distanceExpression.expressionToJava»«ELSE»1.0«ENDIF»,
	    	(double) «f.interval.from.expressionToJava»,
	    	(double) «f.interval.to.expressionToJava»),
	    «domain»
	  )
	  '''		
	}	

	def dispatch CharSequence SpatialTemporalMonitorCode(StrelSomewhereFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.somewhereMonitor( 
	    «f.argument.SpatialTemporalMonitorCode(prefix,domain)»,
	    m -> DistanceStructure.buildDistanceStructure(m, 
	    	«edgeRecord» -> (double) «IF f.distanceExpression !== null»«f.distanceExpression.expressionToJava»«ELSE»1.0«ENDIF»,
	    	(double) «f.interval.from.expressionToJava»,
	    	(double) «f.interval.to.expressionToJava»),
	    «domain»
	  )
	  '''		
	}	
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelEverywhereFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.everywhereMonitor( 
	    «f.argument.SpatialTemporalMonitorCode(prefix,domain)»,
	    m -> DistanceStructure.buildDistanceStructure(m, 
	    	«edgeRecord» -> (double) «IF f.distanceExpression !== null»«f.distanceExpression.expressionToJava»«ELSE»1.0«ENDIF»,
	    	(double) «f.interval.from.expressionToJava»,
	    	(double) «f.interval.to.expressionToJava»),
	    «domain»
	  )
	  '''		
	}		
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelReachFormula f, String prefix, String domain) {
	  '''
	  SpatialTemporalMonitor.reachMonitor( 
	    «f.left.SpatialTemporalMonitorCode(prefix,domain)»,
	    m -> DistanceStructure.buildDistanceStructure(m, 
	    	«edgeRecord» -> (double) «IF f.distanceExpression !== null»«f.distanceExpression.expressionToJava»«ELSE»1.0«ENDIF»,
	    	(double) «f.interval.from.expressionToJava»,
	    	(double) «f.interval.to.expressionToJava»),
	    «f.right.SpatialTemporalMonitorCode(prefix,domain)»,
	    «domain»
	  )
	  '''		
	}		
	
	
	def dispatch CharSequence SpatialTemporalMonitorCode(StrelFormulaReference f , String prefix, String domain) {
	  '''
		_FORMULA_«f.reference.name»( «FOR p:f.arguments SEPARATOR ','»«p.expressionToJava»«ENDFOR» )
	  '''		
	}
	
	
	def dispatch CharSequence buildAtomicFormula(Expression expression) {
		'''«signalDomain».valueOf(«expression.expressionToJava»)'''		
	}

	def dispatch CharSequence buildAtomicFormula(OrExpression expression) {
		'''«signalDomain».disjunction((«expression.left.buildAtomicFormula»),(«expression.right.buildAtomicFormula»))'''	
	}

	def dispatch CharSequence buildAtomicFormula(AndExpression expression) {
		'''«signalDomain».conjunction((«expression.left.buildAtomicFormula»),(«expression.right.buildAtomicFormula»))'''	
	}

	def CharSequence relationMethod( String op ) {
		switch (op) {
			case '<': '''computeLessThan'''
			case '<=': '''computeLessOrEqual'''
			case '==': '''computeEqualTo'''
			case '>': '''computeGreaterThan'''
			case '>=':	'''computeGreaterOrEqualThan'''
		}
		
	}

	def dispatch CharSequence buildAtomicFormula(RelationExpression expression) {
		'''«signalDomain».«expression.op.relationMethod»((«expression.left.expressionToJava»),(«expression.right.expressionToJava»))'''	
	}

	def dispatch CharSequence buildAtomicFormula(NotExpression expression) {
		'''«signalDomain».negation(«expression.argument.expressionToJava»)'''	
	}

	def dispatch CharSequence getExpressionToJava(Expression expression) {
		'''1.0'''
		
	}

	def dispatch CharSequence getExpressionToJava(OrExpression expression) {
		'''((«expression.left.expressionToJava»)||(«expression.right.expressionToJava»))'''	
	}

	def dispatch CharSequence getExpressionToJava(AndExpression expression) {
		'''((«expression.left.expressionToJava»)&&(«expression.right.expressionToJava»))'''	
	}

	def dispatch CharSequence getExpressionToJava(RelationExpression expression) {
		'''((«expression.left.expressionToJava»)«expression.op»(«expression.right.expressionToJava»))'''	
	}

	def dispatch CharSequence getExpressionToJava(PlusExpression expression) {
		'''((«expression.left.expressionToJava»)+(«expression.right.expressionToJava»))'''	
	}

	def dispatch CharSequence getExpressionToJava(MinusExpression expression) {
		'''((«expression.left.expressionToJava»)-(«expression.right.expressionToJava»))'''	
	}

	def dispatch CharSequence getExpressionToJava(ModuloExpression expression) {
		'''((«expression.left.expressionToJava»)%(«expression.right.expressionToJava»))'''	
	}

	def dispatch CharSequence getExpressionToJava(MulOrDivExpression expression) {
		'''((«expression.left.expressionToJava»)«expression.op»(«expression.right.expressionToJava»))'''	
	}
	
	def dispatch CharSequence getExpressionToJava(TrueLiteral expression) {
		'''true'''	
	}

	def dispatch CharSequence getExpressionToJava(FalseLiteral expression) {
		'''false'''	
	}

	def dispatch CharSequence getExpressionToJava(IntegerLiteral expression) {
		'''«expression.value»'''	
	}

	def dispatch CharSequence getExpressionToJava(RealLiteral expression) {
		'''«expression.value»'''	
	}

	def dispatch CharSequence getExpressionToJava(NotExpression expression) {
		'''!(«expression.argument.expressionToJava»)'''	
	}

	def dispatch CharSequence getExpressionToJava(UnaryPlusExpression expression) {
		'''«expression.argument.expressionToJava»'''	
	}

	def dispatch CharSequence getExpressionToJava(UnaryMinusExpression expression) {
		'''-(«expression.argument.expressionToJava»)'''	
	}
	
	def dispatch CharSequence getExpressionToJava(IfThenElseExpression expression) {
		'''(«expression.guard.expressionToJava»?«expression.thenCase.expressionToJava»:«expression.elseCase.expressionToJava»)'''	
	}
	
	def dispatch CharSequence getExpressionToJava(ReferencedValue expression) {
		val variable = expression.reference
		val model = variable.getContainerOfType(typeof(Model))
		val formula = variable.getContainerOfType(typeof(FormulaDeclaration))
		if (model.signalVariables.contains(variable)) {
			return '''((«variable.type.basicTypeOf») «signalRecord».get(«model.signalVariables.indexOf(variable)»,«variable.type.toJavaType»))'''
		}
		if (model.edgeVariables.contains(variable)) {
			return '''((«variable.type.basicTypeOf») «edgeRecord».get(«model.edgeVariables.indexOf(variable)»,«variable.type.toJavaType»))'''
		}
		if (formula.parameters.contains(variable)) {
			return '''«variable.name»'''
		}
	}
	
	def dispatch CharSequence getExpressionToJava(UnaryMathFunction expression) {
		'''(double) Math.«expression.fun.name»( «expression.arg.expressionToJava» )'''
	}

	def dispatch CharSequence getExpressionToJava(BinaryMathFunction expression) {
		'''(double) Math.«expression.fun.name»( «expression.arg1.expressionToJava» , «expression.arg2.expressionToJava» )'''
	}
	
}