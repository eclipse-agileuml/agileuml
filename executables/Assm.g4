grammar Assm;		
prog:	(stat ('\\n' | NEWLINE) )* ;

stat: 'LOAD' expr
    | 'MULT' expr
    | 'ADD' expr
    | 'SUB' expr
    | 'DIVF' expr
    | 'DIVX' expr
    | 'ABS'
    | 'CHS'
    | 'SIGN'
    | 'DELTA'
    | 'NOP'
    | 'RETURN'
    | 'VARX' expr
    | 'VARF' expr
    | 'VARA' expr
    | 'B' expr
    | 'BM' expr
    | 'BP' expr
    | 'BZ' expr
    | 'BNZ' expr
    | 'CALL' expr
    | 'LREG' expr
    | 'SREG' expr
    | 'STORE' expr 
    | 'LABEL' expr ':';

expr:	 '*' expr
    |	expr ('+'|'-') expr
    |	INT   { System.out.println("parsed integer: " + $INT.int); } 
    | FLOAT_LITERAL
    | ID    { System.out.println("parsed identifier: " + $ID.text); } 
    |	'(' expr ')'
    ;

FLOAT_LITERAL:  Digits '.' Digits ;

fragment Digits
    : [0-9]+
    ;

NEWLINE : [\r\n]+ ;
INT     : [0-9]+ ;
ID  :   [a-zA-Z]+[a-zA-Z0-9]* ;      // match identifiers
WS  :   [ \t]+ -> skip ;
