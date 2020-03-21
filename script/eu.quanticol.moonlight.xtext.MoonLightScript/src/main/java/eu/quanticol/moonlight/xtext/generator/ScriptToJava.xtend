package eu.quanticol.moonlight.xtext.generator

import eu.quanticol.moonlight.xtext.moonLightScript.Model
import eu.quanticol.moonlight.xtext.moonLightScript.Monitor
import eu.quanticol.moonlight.xtext.moonLightScript.SemiringExpression
import eu.quanticol.moonlight.xtext.moonLightScript.MinMaxSemiring
import eu.quanticol.moonlight.xtext.moonLightScript.BooleanSemiring
import eu.quanticol.moonlight.xtext.moonLightScript.StrelFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelOrFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelAndFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelSinceFormula
import eu.quanticol.moonlight.xtext.moonLightScript.Expression
import eu.quanticol.moonlight.xtext.moonLightScript.StrelUntilFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelAtomicFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelEventuallyFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelAlwaysFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelFormulaReference
import eu.quanticol.moonlight.xtext.moonLightScript.StrelHistoricallyFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelOnceFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelEscapeFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelSomewhereFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelEverywhereFormula
import eu.quanticol.moonlight.xtext.moonLightScript.StrelReachFormula
import eu.quanticol.moonlight.xtext.moonLightScript.TypeDefinition
import eu.quanticol.moonlight.xtext.moonLightScript.OrExpression
import eu.quanticol.moonlight.xtext.moonLightScript.AndExpression
import eu.quanticol.moonlight.xtext.moonLightScript.RelationExpression
import eu.quanticol.moonlight.xtext.moonLightScript.PlusExpression
import eu.quanticol.moonlight.xtext.moonLightScript.MinusExpression
import eu.quanticol.moonlight.xtext.moonLightScript.ModuloExpression
import eu.quanticol.moonlight.xtext.moonLightScript.MulOrDivExpression
import eu.quanticol.moonlight.xtext.moonLightScript.TrueLiteral
import eu.quanticol.moonlight.xtext.moonLightScript.FalseLiteral
import eu.quanticol.moonlight.xtext.moonLightScript.NotExpression
import eu.quanticol.moonlight.xtext.moonLightScript.UnaryPlusExpression
import eu.quanticol.moonlight.xtext.moonLightScript.UnaryMinusExpression
import eu.quanticol.moonlight.xtext.moonLightScript.IfThenElseExpression
import eu.quanticol.moonlight.xtext.moonLightScript.ReferencedValue

import static extension org.eclipse.xtext.EcoreUtil2.*
import eu.quanticol.moonlight.xtext.moonLightScript.BasicType
import eu.quanticol.moonlight.xtext.moonLightScript.IntegerType
import eu.quanticol.moonlight.xtext.moonLightScript.RealType
import eu.quanticol.moonlight.xtext.moonLightScript.BooleanType
import eu.quanticol.moonlight.xtext.moonLightScript.TypeReference
import org.eclipse.emf.common.util.EList
import eu.quanticol.moonlight.xtext.moonLightScript.VariableDeclaration
import eu.quanticol.moonlight.xtext.moonLightScript.IntegerLiteral
import eu.quanticol.moonlight.xtext.moonLightScript.RealLiteral
import eu.quanticol.moonlight.xtext.moonLightScript.StrelNotFormula
import eu.quanticol.moonlight.xtext.moonLightScript.UnaryMathFunction
import eu.quanticol.moonlight.xtext.moonLightScript.BinaryMathFunction
import eu.quanticol.moonlight.xtext.moonLightScript.StrelImplyFormula

class ScriptToJava {
	
	val edgeRecord = "edge"
	val signalRecord = "signal"
	val parameterRecord = "parameters"
	
	def getJavaCode( Model model, String packageName , String className ) {
		'''
		/**
		 * Code Generate by MoonLight tool.
		 */ 
		package «packageName»;
		
		import eu.quanticol.moonlight.*;
		import eu.quanticol.moonlight.monitoring.temporal.*;
		import eu.quanticol.moonlight.monitoring.spatiotemporal.*;
		import eu.quanticol.moonlight.signal.*;
		import eu.quanticol.moonlight.util.*;
		import eu.quanticol.moonlight.*;
		import eu.quanticol.moonlight.formula.*;
		import java.util.function.Function;
		import java.util.HashSet;
		import java.util.HashMap;
		
		
		public class «className» extends MoonLightScript {			
			
			«FOR generatedEnum: model.elements.filter(typeof(TypeDefinition))»
			public static enum «generatedEnum.name» {
				«FOR e: generatedEnum.elements SEPARATOR ','»	
				«e.name»
				«ENDFOR»
			}
			«ENDFOR»
						
			«FOR m: model.elements.filter(typeof(Monitor))»
			private final SignalDomain<«m.semiring.javaTypeOf»> «m.name.domainVariable» = new «m.semiring.domainOf»();
			«ENDFOR»
			
			«FOR m: model.elements.filter(typeof(Monitor))»
			«m.formula.generateGenerateFormulaBuilderDeclaration("main",m)»
			«FOR f: m.subformulas»
			«f.formula.generateGenerateFormulaBuilderDeclaration(f.name,m)»
			«ENDFOR»
			«ENDFOR»
			
			«FOR m: model.elements.filter(typeof(Monitor))»
			«m.generateMonitorDataHandlers»
			«ENDFOR»
			
			«FOR m: model.elements.filter(typeof(Monitor))»
			«m.generateMonitorDeclaration»
			«ENDFOR»
			
			public «className»() {
				super( new String[] { 
					«FOR m: model.elements.filter(typeof(Monitor)).filter[!it.isIsSpatial] SEPARATOR ','»
					"«m.name»"
					«ENDFOR»					
				}, 
				new String[] {
					«FOR m: model.elements.filter(typeof(Monitor)).filter[it.isIsSpatial] SEPARATOR ","»
					"«m.name»"
					«ENDFOR»					
				});	
			}
		
			@Override
			public TemporalScriptComponent<?> selectTemporalComponent( String name ) {
				«FOR m: model.elements.filter(typeof(Monitor)).filter[!it.isIsSpatial]»
				if ("«m.name»".equals( name ) ) {
					return 	MONITOR_«m.name»; 
				}
				«ENDFOR»
				return null;					
			}				

			public SpatioTemporalScriptComponent<?> selectSpatioTemporalComponent( String name ) {
				«FOR m: model.elements.filter(typeof(Monitor)).filter[it.isIsSpatial]»
				if ("«m.name»".equals( name ) ) {
					return 	MONITOR_«m.name»; 
				}
				«ENDFOR»
				return null;
			}

			public TemporalScriptComponent<?> selectDefaultTemporalComponent( ) {
				return «model.elements.filter(typeof(Monitor)).filter[!it.isIsSpatial].generateReferenceToDefaultMonitor»;	
			}
				
			public SpatioTemporalScriptComponent<?> selectDefaultSpatioTemporalComponent( ) {
				return «model.elements.filter(typeof(Monitor)).filter[it.isIsSpatial].generateReferenceToDefaultMonitor»;	
			}
		
		
		}
		'''
		
		
	}
	
	def generateReferenceToDefaultMonitor( Iterable<Monitor> list ) {
		if (list.empty) {
			return '''null'''
		} else {
			return '''MONITOR_«list.get(0).name»'''
		}
	}
	
	def CharSequence generateMonitorDataHandlers(Monitor monitor) {
		'''
		private RecordHandler «monitor.name.signalRecordHandlerName» = generate«monitor.name»SignalRecordHandler();
		
		private static RecordHandler generate«monitor.name»SignalRecordHandler() {
			«monitor.signalVariables.recordHandlerCreationCode»
		}
		
		private RecordHandler «monitor.name.parametersRecordHandlerName» = generate«monitor.name»ParametersRecordHandler();
		
		private static RecordHandler generate«monitor.name»ParametersRecordHandler() {
			«monitor.parameters.recordHandlerCreationCode»
		}
		«IF monitor.isIsSpatial» 
		private RecordHandler «monitor.name.edgeRecordHandlerName» = generate«monitor.name»EdgesRecordHandler();
		
		private static RecordHandler generate«monitor.name»EdgesRecordHandler() {
			«monitor.edgeVariables.recordHandlerCreationCode»
		}
		«ENDIF»
		
		private DataHandler<«monitor.semiring.javaTypeOf»> «monitor.name.dataOutputHandlerName» = «monitor.semiring.dataHandlerOf»;
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

	def  generateGenerateFormulaBuilderDeclaration(StrelFormula f, String name, Monitor monitor) {
		if (monitor.isIsSpatial) {
			'''
			private SpatioTemporalMonitor<Record,Record,«monitor.semiring.javaTypeOf»> «monitorSubFormulaName(monitor.name,name)»( Record parameters ) {
				return «f.spatioTemporalMonitorCode(monitor.name,monitor.name.domainVariable)»;	
			}			
			'''
		} else {
			'''
			private TemporalMonitor<Record,«monitor.semiring.javaTypeOf»> «monitorSubFormulaName(monitor.name,name)» ( Record parameters ) {
				return «f.temporalMonitorCode(monitor.name,monitor.name.domainVariable)»;	
			}
			'''
		}
	}
	
	
	def  generateMonitorDeclaration(Monitor monitor) {
		if (monitor.isIsSpatial) {
			'''private SpatioTemporalScriptComponent<«monitor.semiring.javaTypeOf»> MONITOR_«monitor.name» = new SpatioTemporalScriptComponent<>(
				"«monitor.name»" ,
				«monitor.name.edgeRecordHandlerName» ,
				«monitor.name.signalRecordHandlerName» ,
				«monitor.name.dataOutputHandlerName» ,
				«monitor.name.parametersRecordHandlerName» ,
				r -> «monitorSubFormulaName(monitor.name,"main")»( r )	
			);'''
		} else {
			'''private TemporalScriptComponent<«monitor.semiring.javaTypeOf»> MONITOR_«monitor.name» = new TemporalScriptComponent<>(
				"«monitor.name»" ,
				«monitor.name.signalRecordHandlerName» ,
				«monitor.name.dataOutputHandlerName» ,
				«monitor.name.parametersRecordHandlerName» ,
				r -> «monitorSubFormulaName(monitor.name,"main")»( r )	
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
	
	def CharSequence monitorSubFormulaName(String monitorName, String formulaName ) {
		'''«monitorName»_«formulaName»'''
	}

	def CharSequence monitorMainFormulaName(String monitorName, String formulaName ) {
		'''«monitorName»_main'''
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
			«signalRecord» -> «f.atomic.expressionToJava»
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
		«prefix»_FORMULA_«f.reference.name»'(' parameters ')'
		'''		
	}


	def dispatch CharSequence spatioTemporalMonitorCode(StrelFormula f, String prefix, String domain) {
	  throw new IllegalArgumentException("Unexpected formula in temporal monitoring ("+f.class+") in monitor "+prefix);
	} 
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelOrFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.orMonitor( 
	    «f.left.spatioTemporalMonitorCode(prefix,domain)» ,
	    «domain» , 
	    «f.right.spatioTemporalMonitorCode(prefix,domain)»
	  )
	  '''
	}
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelImplyFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.impliesMonitor( 
	    «f.left.spatioTemporalMonitorCode(prefix,domain)» ,
	    «domain» , 
	    «f.right.spatioTemporalMonitorCode(prefix,domain)»
	  )
	  '''
	}
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelNotFormula f, String prefix, String domain) {
		'''
		SpatioTemporalMonitor.notMonitor( 
			«f.argument.spatioTemporalMonitorCode(prefix,domain)» ,
			«domain»
		)
		'''
	}

	
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelAndFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.andMonitor( 
	    «f.left.spatioTemporalMonitorCode(prefix,domain)» ,
	    «domain» , 
	    «f.right.spatioTemporalMonitorCode(prefix,domain)»
	  )
	  '''
	}
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelSinceFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.sinceMonitor( 
	    «f.left.spatioTemporalMonitorCode(prefix,domain)» , 
	    «IF f.interval !== null»
	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    «ENDIF»
	    «f.right.spatioTemporalMonitorCode(prefix,domain)» ,
	    «domain» 
	  )
	  '''
	}
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelUntilFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.untilMonitor( 
	    «f.left.spatioTemporalMonitorCode(prefix,domain)» , 
	    «IF f.interval !== null»
	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    «ENDIF»
	    «f.right.spatioTemporalMonitorCode(prefix,domain)» ,
	    «domain» 
	  )
	  '''
	}
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelAtomicFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.atomicMonitor( 
	    «signalRecord» -> «f.atomic.expressionToJava»
	  )
	  '''
	}
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelEventuallyFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.eventuallyMonitor( 
	    «f.argument.spatioTemporalMonitorCode(prefix,domain)»,«IF f.interval !== null»
	    	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    	    «ENDIF»
	    	    «domain»
	    	  )
	  '''
	}	
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelAlwaysFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.globallyMonitor( 
	    «f.argument.spatioTemporalMonitorCode(prefix,domain)»,«IF f.interval !== null»
	    	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    	    «ENDIF»
	    	    «domain»
	    	  )
	  '''
	 }	
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelHistoricallyFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.historicallyMonitor( 
	    «f.argument.spatioTemporalMonitorCode(prefix,domain)»,«IF f.interval !== null»
	    	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    	    «ENDIF»
	    	    «domain»
	    	  )
	  '''
	}	
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelOnceFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.onceMonitor( 
	    «f.argument.spatioTemporalMonitorCode(prefix,domain)»,«IF f.interval !== null»
	    	    new Interval(«f.interval.from.expressionToJava»,«f.interval.to.expressionToJava»),
	    	    «ENDIF»
	    	    «domain»
	    	  )
	  '''
	}	
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelEscapeFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.escapeMonitor( 
	    «f.argument.spatioTemporalMonitorCode(prefix,domain)»,
	    m -> DistanceStructure.buildDistanceStructure(m, 
	    	«edgeRecord» -> (double) «IF f.distanceExpression !== null»«f.distanceExpression.expressionToJava»«ELSE»1.0«ENDIF»,
	    	(double) «f.interval.from.expressionToJava»,
	    	(double) «f.interval.to.expressionToJava»),
	    «domain»
	  )
	  '''		
	}	

	def dispatch CharSequence spatioTemporalMonitorCode(StrelSomewhereFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.somewhereMonitor( 
	    «f.argument.spatioTemporalMonitorCode(prefix,domain)»,
	    m -> DistanceStructure.buildDistanceStructure(m, 
	    	«edgeRecord» -> (double) «IF f.distanceExpression !== null»«f.distanceExpression.expressionToJava»«ELSE»1.0«ENDIF»,
	    	(double) «f.interval.from.expressionToJava»,
	    	(double) «f.interval.to.expressionToJava»),
	    «domain»
	  )
	  '''		
	}	
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelEverywhereFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.everywhereMonitor( 
	    «f.argument.spatioTemporalMonitorCode(prefix,domain)»,
	    m -> DistanceStructure.buildDistanceStructure(m, 
	    	«edgeRecord» -> (double) «IF f.distanceExpression !== null»«f.distanceExpression.expressionToJava»«ELSE»1.0«ENDIF»,
	    	(double) «f.interval.from.expressionToJava»,
	    	(double) «f.interval.to.expressionToJava»),
	    «domain»
	  )
	  '''		
	}		
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelReachFormula f, String prefix, String domain) {
	  '''
	  SpatioTemporalMonitor.reachMonitor( 
	    «f.left.spatioTemporalMonitorCode(prefix,domain)»,
	    m -> DistanceStructure.buildDistanceStructure(m, 
	    	«edgeRecord» -> (double) «IF f.distanceExpression !== null»«f.distanceExpression.expressionToJava»«ELSE»1.0«ENDIF»,
	    	(double) «f.interval.from.expressionToJava»,
	    	(double) «f.interval.to.expressionToJava»),
	    «f.right.spatioTemporalMonitorCode(prefix,domain)»,
	    «domain»
	  )
	  '''		
	}		
	
	
	def dispatch CharSequence spatioTemporalMonitorCode(StrelFormulaReference f , String prefix, String domain) {
	  '''
	  «prefix»_FORMULA_«f.reference.name»'(' parameters ')'
	  '''		
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
		val monitor = variable.getContainerOfType(typeof(Monitor))
		if (monitor.signalVariables.contains(variable)) {
			return '''((«variable.type.basicTypeOf») «signalRecord».get(«monitor.signalVariables.indexOf(variable)»,«variable.type.toJavaType»))'''
		}
		if (monitor.edgeVariables.contains(variable)) {
			return '''((«variable.type.basicTypeOf») «edgeRecord».get(«monitor.edgeVariables.indexOf(variable)»,«variable.type.toJavaType»))'''
		}
		if (monitor.parameters.contains(variable)) {
			return '''((«variable.type.basicTypeOf») «parameterRecord».get(«monitor.parameters.indexOf(variable)»,«variable.type.toJavaType»))'''
		}
	}
	
	def dispatch CharSequence getExpressionToJava(UnaryMathFunction expression) {
		'''(double) Math.«expression.fun.name»( «expression.arg.expressionToJava» )'''
	}

	def dispatch CharSequence getExpressionToJava(BinaryMathFunction expression) {
		'''(double) Math.«expression.fun.name»( «expression.arg1.expressionToJava» , «expression.arg2.expressionToJava» )'''
	}
	
}