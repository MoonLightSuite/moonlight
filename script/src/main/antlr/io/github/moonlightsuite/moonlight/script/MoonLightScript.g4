grammar MoonLightScript;

@header {
package io.github.moonlightsuite.moonlight.script;
}


model:
	(types += scriptType)*
	(constants += scriptConstant)*
	scriptSignal
	scriptSpace?
	scriptDomain
    (formulas += scriptFormula)+
;

scriptConstant: 'const' name=ID '=' value=expression ';';

scriptFormula:
    (isDefault='default')? 'formula' name=ID ('(' parameters += variableDeclaration (',' parameters += variableDeclaration)* ')')? '=' formula=expression ';'
;


variableDeclaration:
	type=basicType name=ID
;

basicType:
      'int'     # integerType
	| 'real'    # realType
	| 'bool'    # booleanType
	| type=ID   # referenceType
;


typeElement:
	name=ID
;

scriptType:
	'type' name=ID '=' elements += typeElement ('|' elements += typeElement )* ';'
;

semiringExpression :
	  'minmax'      # minMaxSemiring
	|  'boolean'    # booleanSemiring
;

scriptDomain:
	'domain' semiring=semiringExpression ';'
;

scriptSignal:
	'signal' '{'
		(signalVariables += variableDeclaration ';')+
	'}'
    ;

scriptSpace:
	'space' '{'
/*		('locations' '{' (locationVariables += variableDeclaration ';')+ '}')?*/
		('edges' '{'
		(edgeVariables += variableDeclaration ';')+
		 '}')
	 '}'
;

expression :
      '(' expression ')'                                                  # bracketExpression
    | '(' guard=expression '?'
            thenExpression = expression
            ':' elseExpression = expression                               # ifThenElseExpression
    | '{' expression '}'                                                  # bracketExpression
    | left=expression ('->') right=expression                             # implyExpression
    | left=expression ('|') right=expression                              # orExpression
    | left=expression ('&') right=expression                              # andExpression
    | left=expression 'until' (interval)? right=expression                # untilExpression
    | left=expression 'since' (interval)? right=expression                # sinceExpression
    | left=expression 'reach' ('(' distanceExpression=expression ')')?
      interval
      right=expression                                                    # reachExpression
    | 'escape' ('(' distanceExpression=expression ')')?
        interval argument=expression                             # escapeExpression
    | 'eventually' (interval)? argument=expression               # eventuallyExpression
    | 'globally'  (interval)? argument=expression                # globallyExpression
    | 'once'  (interval)? argument=expression                    # onceExpression
    | 'historically'  (interval)? argument=expression            # historicallyExpression
    | 'somewhere'  ('(' distanceExpression=expression ')')?
                   (interval)? argument=expression               # somewhereExpression
    | 'everywhere'  ('(' distanceExpression=expression ')')?
                    (interval)? argument=expression              # everywhereExpression
    | 'next' ('(' distanceExpression=expression ')')?
        interval argument=expression                                      # nextExpression
    | left=expression op=('<'|'<='|'=<'|'=='|'>='|'=>'|'>') right=expression        # relationExpression
    | left=expression op=('+'|'-'|'%') right=expression                   # sumDifExpression
    | left=expression op=('*'|'/') right=expression                       # mulDivExpression
    | '!' arg=expression                                                  # notExpression
    | name=ID ('(' (args += expression (',' args +=expression)*)? ')')?      # referenceExpression
    | INTEGER                                                             # intExpression
    | REAL                                                                # realExpression
    | 'inf'                                                               # infinityExpression
    | 'true'                                                              # trueExpression
    | 'false'                                                             # falseExpression
    | op=('+'|'-') arg=expression                                         # unaryExpression
    | fun=unaryMathFunction '(' argument=expression ')'                   # unaryMathCallExpression
    | fun=binaryMathFunction '(' left=expression ',' right=expression ')' # binaryMathCallExpression
    ;

interval : '[' from=expression ',' to=expression ']'
    ;

binaryMathFunction:
    'atan2'
    | 'hypot'
    | 'max'
    | 'min'
    | 'pow'
;

unaryMathFunction: 'abs'
    | 'acos'
    | 'asin'
    | 'atan'
    | 'cbrt'
    | 'ceil'
    | 'cos'
    | 'cosh'
    | 'exp'
    | 'expm1'
    | 'floor'
    | 'log'
    | 'log10'
    | 'log1p'
    | 'signum'
    | 'sin'
    | 'sinh'
    | 'sqrt'
    | 'tan'
    ;

fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;


COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN) // match anything between /* and */
    ;

WS  : [ \r\t\u000C\n]+ -> channel(HIDDEN)
    ;
