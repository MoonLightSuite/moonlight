package eu.quanticol.moonlight.xtext.generator

class ScriptToJava {
	
	def getJavaCode( String packageName , String className ) {
		'''
		/**
		 * Code Generate by MoonLight tool.
		 */ 
		package «packageName»;

		public class «className» implements MoonLightScrit {
			
			public void monitor( String label, String inputFile , String outputFile );
				
			public void monitor( String label, Signal<?> signal , String outputFile );
			
			public void monitor( String label, LocationService<?> service , SpatioTemporalSignal<?> signal , String outputFile );
			
			public String[] getMonitors();
				
			public String getInfo( String monitor );			
			
		}
		'''
		
		
	}
	
	
}