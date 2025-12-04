/*
 [The "BSD licence"]

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

lexer grammar Mamba3Lexer;

// Keywords

KW_AS:              'as'; 
KW_ASSIGN:          'assign';
KW_BREAK:           'break';
KW_CASE:            'case';
KW_CATCH:           'catch';
KW_CONTINUE:        'continue';
KW_DEFAULT:         'default';
KW_ELSE:            'else';
KW_ELSEIF:          'elseif'; 
KW_FALSE:           'false';
KW_FINALLY:         'finally';
KW_FOR:             'for';
KW_FOREACH:         'foreach';
KW_FUNCTION:        'function';  
KW_IF:              'if';
KW_IN:              'in';
KW_NULL:            'null'; 
KW_OUT:             'out'; 
KW_RETURN:          'return';
KW_STATIC:          'static';
KW_SWITCH:          'switch';
KW_THROW:           'throw';
KW_TRUE:            'true'; 
KW_TRY:             'try';
KW_VAR:             'var'; 
KW_WHILE:           'while';

// Literals

NUMBER:          ('0' | [1-9] (Digits?)); 

DECIMAL_LITERAL:    ('0' | [1-9] (Digits? | '_'+ Digits)) [lL]?;

FLOAT_LITERAL:      (Digits '.' Digits? | '.' Digits) ExponentPart? [fFdD]?
             |       Digits (ExponentPart [fFdD]? | [fFdD])
             ;


CHARACTER_LITERAL:       '\'' (~['\\\r\n] | EscapeSequence) '\'';

STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';


// Separators

L_PAR:             '(';
R_PAR:             ')';
L_CBRACK:             '{';
R_CBRACK:             '}';
L_BRACK:             '[';
R_BRACK:             ']';
SEMICOLON:               ';';
COMMA:              ',';
POINT:                '.';

// Operators

LAMDA_OPERATOR:    '=>';
EQ:             '=';
GT:                 '>';
LT:                 '<';
NOT:               '!';
QUESTION_MARK:           '?';
COLON:              ':';
DEQ:              '==';
LTEQ:                 '<=';
GTEQ:                 '>=';
NEQ:           '!=';
AND:                '&&';
OR:                 '||';
PLUS:                '+';
MINUS:                '-';
MULT:                '*';
DIV:                '/';
POWER:              '^';
MOD:                '%';



// Whitespace and comments

WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);

// Identifiers

IDENTIFIER:         Letter LetterOrDigit*;

// Fragment rules

fragment ExponentPart
    : [eE] [+-]? Digits
    ;

fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment HexDigits
    : HexDigit ((HexDigit | '_')* HexDigit)?
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;

fragment Digits
    : [0-9] ([0-9_]* [0-9])?
    ;

fragment LetterOrDigit
    : Letter
    | [0-9]
    ;

fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;
