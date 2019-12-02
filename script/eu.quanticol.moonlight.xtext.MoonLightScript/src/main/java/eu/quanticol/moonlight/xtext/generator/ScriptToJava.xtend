package eu.quanticol.moonlight.xtext.generator

import eu.quanticol.moonlight.xtext.moonLightScript.Model
import eu.quanticol.moonlight.xtext.moonLightScript.Monitor
import eu.quanticol.moonlight.xtext.moonLightScript.TypeDefinition
import eu.quanticol.moonlight.xtext.moonLightScript.SemiringExpression
import eu.quanticol.moonlight.xtext.moonLightScript.TropicalSemiring
import eu.quanticol.moonlight.xtext.moonLightScript.MinMaxSemiring
import eu.quanticol.moonlight.xtext.moonLightScript.BooleanSemiring
import eu.quanticol.moonlight.xtext.moonLightScript.PairSemiring

class ScriptToJava {
	
	def getJavaCode( Model model, String packageName , String className ) {
		'''
		/**
		 * Code Generate by MoonLight tool.
		 */ 
		package «packageName»;
		
		import eu.quanticol.moonlight.MoonLightScript;
		import eu.quanticol.moonlight.monitoring.temporal.*;
		import eu.quanticol.moonlight.monitoring.spatiotemporal.*;
		import eu.quanticol.moonlight.signal.*;
		import eu.quanticol.moonlight.util.*;
		import java.util.HashSet;
		import java.util.HashMap;
		
		
		public class «className» implements MoonLightScript {
			
			private HashSet<String> temporalMonitors = new HashSet<>();
			private HashSet<String> spatialMonitors = new HashSet<>();
			private HashMap<String,String> monitorInfo = new HashMap<>();
			
			«FOR m: model.elements.filter(typeof(Monitor))»
			«m.generateMonitorDeclaration»
			«ENDFOR»
			
			«FOR generatedEnum: model.elements.filter(typeof(TypeDefinition))»
			public static enum «generatedEnum.name» {
				«FOR e: generatedEnum.elements SEPARATOR ','»	
				«e.name»
				«ENDFOR»
			}
			«ENDFOR»
			
			public «className»() {
					
			}
		
			@Override
			public void monitor(String label, String inputFile, String outputFile) {
				// TODO Auto-generated method stub
				
			}
		
			@Override
			public String[] getMonitors() {
				return new String[] {«FOR m: model.elements.filter(typeof(Monitor)).map[it.name].sort SEPARATOR ','»
					"«m»"
					«ENDFOR»
				};
			}
		
			@Override
			public String getInfo(String monitor) {
				return "";
			}
		
		}
		'''
		
		
	}
	
	def  generateMonitorDeclaration(Monitor monitor) {
		if (monitor.isIsSpatial) {
			'''private SpatioTemporalMonitor<Assignment,Assignment,«monitor.semiring.javaTypeOf»> MONITOR_«monitor.name» = null;'''
		} else {
			'''private TemporalMonitor<Assignment,«monitor.semiring.javaTypeOf»> MONITOR_«monitor.name» = null;'''
		}
	}
	
	def CharSequence javaTypeOf(SemiringExpression e) {
		switch e {
		TropicalSemiring: '''Double'''
		MinMaxSemiring: '''Double'''
		BooleanSemiring: '''Boolean'''
		PairSemiring: '''Pair<«e.left.javaTypeOf»,«e.right.javaTypeOf»>'''
		default: '''?'''			
		}
	}
	
	
}