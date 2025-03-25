/******************************
* Parser for OCL 2.4 with extensions for textual notations
* for UML classes and usecases. 
*
* Arrow operators ->op are used consistently for any OCL 
* operator, not just collection operators. 
* 
* Copyright (c) 2003--2025 Kevin Lano
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0
*
* SPDX-License-Identifier: EPL-2.0
* *****************************/

grammar OCL;	
	
specification
  : 'package' identifier '{' classifier* '}' EOF
  ;

classifier
    : classDefinition
    | interfaceDefinition
    | usecaseDefinition
    | datatypeDefinition
    | enumeration
    ;

interfaceDefinition
    :	'interface' identifier ('extends' identifier)? '{' classBody? '}'
    ; 

classDefinition
    :	'class' identifier ('extends' identifier)? 
     ('implements' idList)? '{' classBody? '}'
    ; 

classBody
    : classBodyElement+
    ; 

classBodyElement
    : attributeDefinition 
    | operationDefinition
    | invariant 
    | stereotype
    ; 

attributeDefinition 
    :  'attribute' identifier ('identity' | 'derived')? ':' type ( ':=' expression )? ';' 
    | 'static' 'attribute' identifier ':' type ( ':=' expression )? ';'
    ; 

operationDefinition
      : ('static')? 'operation' identifier 
        '(' parameterDeclarations? ')' ':' type 
        'pre:' expression 'post:' expression 
        ('activity:' statementList)? ';'
      ;

parameterDeclarations
      : (parameterDeclaration ',')* parameterDeclaration
      ;

parameterDeclaration
      : identifier ':' type
      ;

idList
     : (identifier ',')* identifier
     ; 

usecaseDefinition
      : 'usecase' identifier (':' type)? '{' usecaseBody? '}' 
      | 'usecase' identifier '(' parameterDeclarations ')' (':' type)? '{' usecaseBody? '}'
      ;

usecaseBody
      : usecaseBodyElement+
      ; 

usecaseBodyElement
      : 'parameter' identifier ':' type ';' 
      | 'precondition' expression ';' 
      | 'extends' identifier ';' 
      | 'extendedBy' identifier ';' 
      | 'activity:' statementList ';' 
      | '::' expression 
      | stereotype
      ;

invariant
      : 'invariant' expression ';'
      ; 

stereotype
      : 'stereotype' identifier ';'  
      | 'stereotype' identifier '=' STRING1_LITERAL ';'  
      | 'stereotype' identifier '=' STRING2_LITERAL ';'  
      | 'stereotype' identifier '=' identifier ';' 
      ;

datatypeDefinition
      : 'datatype' identifier '=' type
      ; 

enumeration 
      : 'enumeration' identifier '{' enumerationLiterals '}'
      ;  

enumerationLiterals
      : enumerationLiteral (';' enumerationLiteral)*
      ;

enumerationLiteral
      : 'literal' identifier
      ;

type
    : 'Sequence' '(' type ')'  
    | 'Set' '(' type ')'  
    | 'Bag' '(' type ')' 
    | 'OrderedSet' '(' type ')' 
    | 'SortedSet' '(' type ')' 
    | 'Ref' '(' type ')'  
    | 'Map' '(' type ',' type ')' 
    | 'SortedMap' '(' type ',' type ')' 
    | 'Function' '(' type ',' type ')' 
    | ID
    ; 


expressionList
    : (expression ',')* expression
    ; 

expression
    : logicalExpression  
    | conditionalExpression  
    | lambdaExpression  
    | letExpression
    ;


// Basic expressions can appear on the LHS of . or ->

basicExpression
    : 'null' 
    | basicExpression '.' identifier 
    | basicExpression '(' expressionList? ')'  
    | basicExpression '[' expression ']' 
    | identifier '@pre'  
    | INT  
    | FLOAT_LITERAL
    | STRING1_LITERAL
    | STRING2_LITERAL
    | identifier   
    |	'(' expression ')'
    ; 

conditionalExpression
    : 'if' expression 'then' expression 'else' expression 'endif'
    ; 

lambdaExpression 
    : 'lambda' identifier ':' type 'in' expression
    ; 

// A let is just an application of a lambda:

letExpression
    : 'let' identifier ':' type '=' expression 'in' expression
    ; 

logicalExpression
    : 'not' logicalExpression  
    | logicalExpression 'and' logicalExpression  
    | logicalExpression '&' logicalExpression 
    | logicalExpression 'or' logicalExpression  
    | logicalExpression 'xor' logicalExpression  
    | logicalExpression '=>' logicalExpression  
    | logicalExpression 'implies' logicalExpression  
    | equalityExpression
    ; 

equalityExpression 
    : additiveExpression 
        ('=' | '<' | '>' | '>=' | '<=' | '/=' | '<>' |
         ':'| '/:' | '<:') additiveExpression 
    | additiveExpression
    ; 

additiveExpression
    : additiveExpression '+' additiveExpression 
    | additiveExpression '-' factorExpression
    | factorExpression ('..' | '|->') factorExpression                         
    | factorExpression
    ; 

factorExpression 
    : factor2Expression ('*' | '/' | 'mod' | 'div') 
                                   factorExpression
    | factor2Expression
    ; 


// arrowExpressions can appear on LHS of ->
// ->subrange is used for ->substring and ->subSequence

factor2Expression
  : '-' factor2Expression 
  | '+' factor2Expression 
  | '?' factor2Expression
  | '!' factor2Expression 
  | arrowExpression
  ; 

arrowExpression
  : arrowExpression '->size()' 
  | arrowExpression '->copy()'  
  | arrowExpression ('->isEmpty()' | 
                       '->notEmpty()' | 
                       '->asSet()' | '->asBag()' | 
                       '->asOrderedSet()' | 
                       '->asSequence()' | 
                       '->sort()' ) 
   | arrowExpression '->any()'   
   | arrowExpression '->log()'  
   | arrowExpression '->exp()' 
   | arrowExpression '->sin()'  
   | arrowExpression '->cos()' 
   | arrowExpression '->tan()'  
   | arrowExpression '->asin()'  
   | arrowExpression '->acos()' 
   | arrowExpression '->atan()'  
   | arrowExpression '->log10()' 
   | arrowExpression '->first()'  
   | arrowExpression '->last()' 
   | arrowExpression '->front()'  
   | arrowExpression '->tail()' 
   | arrowExpression '->reverse()'  
   | arrowExpression '->tanh()'  
   | arrowExpression '->sinh()' 
   | arrowExpression '->cosh()' 
   | arrowExpression '->floor()'  
   | arrowExpression '->ceil()' 
   | arrowExpression '->round()' 
   | arrowExpression '->abs()' 
   | arrowExpression '->oclType()' 
   | arrowExpression '->allInstances()' 
   | arrowExpression '->oclIsUndefined()' 
   | arrowExpression '->oclIsInvalid()' 
   | arrowExpression '->oclIsNew()' 
   | arrowExpression '->sum()'  
   | arrowExpression '->prd()'  
   | arrowExpression '->max()'  
   | arrowExpression '->min()'  
   | arrowExpression '->sqrt()'  
   | arrowExpression '->cbrt()'  
   | arrowExpression '->sqr()' 
   | arrowExpression '->characters()'  
   | arrowExpression '->toInteger()'  
   | arrowExpression '->toReal()' 
   | arrowExpression '->toBoolean()' 
   | arrowExpression '->display()' 
   | arrowExpression '->toUpperCase()'  
   | arrowExpression '->toLowerCase()' 
   | arrowExpression ('->unionAll()' | '->intersectAll()' |
                       '->concatenateAll()')
 
   | arrowExpression ('->pow' | '->gcd') '(' expression ')' 
   | arrowExpression ('->at' | '->union' | '->intersection' 
            | '->includes' | '->excludes' | '->including' 
            | '->excluding' | '->excludingKey' 
            | '->excludingValue' | '->includesAll'  
            | '->symmetricDifference' 
            | '->excludesAll' | '->prepend' | '->append'  
            | '->count' | '->apply' ) 
                                   '(' expression ')' 
   | arrowExpression ('->hasMatch' | '->isMatch' |
                       '->firstMatch' | '->indexOf' | 
                       '->lastIndexOf' | '->split' | 
                       '->hasPrefix' | 
                       '->hasSuffix' | 
                       '->equalsIgnoreCase' ) 
                                    '(' expression ')' 
   | arrowExpression ('->oclAsType' | '->oclIsTypeOf' | 
                       '->oclIsKindOf' | 
                       '->oclAsSet') '(' expression ')' 
   | arrowExpression '->collect' '(' ( identifier '|' )? expression ')' 
   | arrowExpression '->select' '(' ( identifier '|' )? expression ')' 
   | arrowExpression '->reject' '(' ( identifier '|' )? expression ')' 
   | arrowExpression '->forAll' '(' ( identifier '|' )? expression ')' 
   | arrowExpression '->exists' '(' ( identifier '|' )? expression ')' 
   | arrowExpression '->exists1' '(' ( identifier '|' )? expression ')' 
   | arrowExpression '->one' '(' ( identifier '|' )? expression ')' 
   | arrowExpression '->any' '(' ( identifier '|' )? expression ')' 
   | arrowExpression '->closure' '(' identifier '|' expression ')' 
   | arrowExpression '->sortedBy' '(' ( identifier '|')? expression ')' 
   | arrowExpression '->isUnique' '(' ( identifier '|')? expression ')' 

   | arrowExpression '->subrange' '(' expression ',' expression ')'  
   | arrowExpression '->replace' '(' expression ',' expression ')'  
   | arrowExpression '->replaceAll' '(' expression ',' expression ')' 
   | arrowExpression '->replaceAllMatches' '(' expression ',' expression ')'  
   | arrowExpression '->replaceFirstMatch' '(' expression ',' expression ')'  
   | arrowExpression '->insertAt' '(' expression ',' expression ')'  
   | arrowExpression '->insertInto' '(' expression ',' expression ')'  
   | arrowExpression '->setAt' '(' expression ',' expression ')' 
   | arrowExpression '->iterate' '(' identifier ';' identifier '=' expression '|' expression ')'  
   | arrowExpression '.' identifier ( '(' expressionList? ')' | '[' expression ']' )?  
   | setExpression 
   | basicExpression
   ; 

setExpression 
    : 'OrderedSet{' expressionList? '}'  
    | 'Bag{' expressionList? '}'  
    | 'Set{' expressionList? '}' 
    | 'SortedSet{' expressionList? '}' 
    | 'Sequence{' expressionList? '}' 
    | 'Map{' expressionList? '}'
    | 'SortedMap{' expressionList? '}'
    ; 

statement 
   : 'skip' 
   | 'return' 
   | 'continue'  
   | 'break' 
   | 'var' ID ':' type 
   | 'if' expression 'then' statement 'else' statement  
   | 'while' expression 'do' statement 
   | 'for' ID ':' expression 'do' statement 
   | 'repeat' statement 'until' expression 
   | 'return' expression 
   | basicExpression ':=' expression 
   | 'execute' expression 
   | 'call' basicExpression 
   | '(' statementList ')'
   ; 

statementList
   : statement (';' statement)*  
   ;  

nlpscript 
   : (nlpstatement ';')+ nlpstatement
   ; 

nlpstatement
   : loadStatement 
   | assignStatement 
   | storeStatement 
   | analyseStatement 
   | displayStatement
   ;

loadStatement:
  'load' expression 'into' basicExpression;

assignStatement:
  basicExpression ':=' expression; 

storeStatement:
  'store' expression 'in' identifier;

analyseStatement:
  'analyse' expression 'using' expression;

displayStatement:
  'display' expression 'on' identifier;


identifier: ID ;

FLOAT_LITERAL:  Digits '.' Digits ;

STRING1_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';

STRING2_LITERAL:     '\'' (~['\\\r\n] | EscapeSequence)* '\'';

NULL_LITERAL:       'null';

MULTILINE_COMMENT: '/*' .*? '*/' -> channel(HIDDEN);


fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

fragment HexDigits
    : HexDigit ((HexDigit | '_')* HexDigit)?
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;


fragment Digits
    : [0-9]+
    ;

INTEGRAL : '\u222B'; 
SIGMA : '\u2211';

NEWLINE : [\r\n]+ -> skip ;
INT     : [0-9]+ ;
ID  :   [a-zA-Z_$]+[a-zA-Z0-9_$]* ;      // match identifiers
WS  :   [ \t\n\r]+ -> skip ;
