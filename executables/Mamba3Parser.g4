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

parser grammar Mamba3Parser;

options { tokenVocab=Mamba3Lexer; }

program
    : function_declaration* 
    | datatype
    ;

function_declaration
    : KW_STATIC? KW_FUNCTION datatype identifier L_PAR param_list? R_PAR L_CBRACK stmtList? R_CBRACK 
    ;

stmtList
   : stmt
   | stmt stmtList
   ; 

stmt 
   : identifier_or_member EQ expr SEMICOLON 
   | expr SEMICOLON 
   | KW_RETURN expr? SEMICOLON 
   | datatype identifier variable_decl_assigment? SEMICOLON 
   | KW_VAR identifier variable_decl_assigment SEMICOLON 
   | KW_IF expr L_CBRACK stmtList? R_CBRACK elseif* elsestat? 
   | KW_FOREACH L_PAR? datatype? COLON? identifier KW_IN expr R_PAR? L_CBRACK stmtList? R_CBRACK 
   | KW_SWITCH expr L_CBRACK case_statement* R_CBRACK 
   | KW_TRY L_CBRACK stmt* R_CBRACK catch_statement* finally_statement? 
   | KW_WHILE expr L_CBRACK stmtList? R_CBRACK 
   | KW_FOR L_PAR? datatype identifier EQ expr SEMICOLON expr SEMICOLON expr R_PAR? L_CBRACK stmtList? R_CBRACK 
   | KW_BREAK SEMICOLON 
   | KW_THROW expr SEMICOLON 
   | KW_CONTINUE SEMICOLON 
   ;

variable_decl_assigment
   : EQ expr
   ;

catch_statement 
   : KW_CATCH datatype identifier L_CBRACK stmtList? R_CBRACK
   ;

finally_statement 
   : KW_FINALLY L_CBRACK stmtList? R_CBRACK
   ;
	
case_statement 
   : KW_CASE expr COLON stmtList? 
   | KW_DEFAULT COLON stmtList?
   ;
		
elseif 
   : KW_ELSEIF expr L_CBRACK stmtList? R_CBRACK
   ;

elsestat 
   : KW_ELSE L_CBRACK stmtList? R_CBRACK
   ;


lamda_parameter_with_datatype 
   : datatype identifier 
   | datatype identifier COMMA lamda_parameter_with_datatype
   ;
		
lamda_parameter_without_datatype 
   : identifier COMMA identifier 
   | identifier COMMA lamda_parameter_without_datatype
   ;
		
lamda_parameters 
   : identifier 
   | L_PAR lamda_parameter_with_datatype R_PAR 
   | L_PAR lamda_parameter_without_datatype R_PAR
   ;


expr 
   : factor_level6 
   | L_CBRACK constructor_type? inline_init_items? R_CBRACK
   | lamda_parameters LAMDA_OPERATOR factor_level6 
   | lamda_parameters LAMDA_OPERATOR L_CBRACK stmtList? R_CBRACK
   ;

factor_level6 
   : factor_level5 
   | factor_level5 QUESTION_MARK factor_level5 COLON factor_level4 
   | factor_level6 OR factor_level5
   ;

factor_level5 
   : factor_level4 
   | factor_level5 AND factor_level4 
   ;

factor_level4 
   : factor_level3 
   | factor_level4 NEQ factor_level3 
   | factor_level4 DEQ factor_level3 
   ;

factor_level3 
   : factor_level2 
   | factor_level3 GT factor_level2 
   | factor_level3 LT factor_level2 
   | factor_level3 GTEQ factor_level2 
   | factor_level3 LTEQ factor_level2
   ; 

factor_level2 
   : factor_level1 
   | factor_level2 PLUS factor_level1 
   | factor_level2 MINUS factor_level1
   ; 

factor_level1 
   : factor_level0 
   | factor_level1 MULT factor_level0 
   | factor_level1 DIV factor_level0 
   | factor_level1 MOD factor_level0 
   | factor_level1 POWER factor_level0 
   | factor_level0 KW_AS datatype
   ;		

factor_level0 
   : operand 
   | NOT factor_level0
   ;

operand 
   : KW_NULL 
   | integer 
   | decimal 
   | member 
   | member_with_expression 
   | string_literal 
   | character_literal 
   | parenthesis_expression 
   | identifier 
   | logic_operand 
   | function_call 
   ;

inline_init_items 
   : constructor_assigment 
   | argument_list
   ;

constructor_type
   : datatype COLON
   ;

constructor_assigment 
   : identifier EQ expr 
   | identifier EQ expr COMMA constructor_assigment
   ;

member_with_expression 
   : parenthesis_expression POINT id_or_function_call 
   | parenthesis_expression POINT member
   ;
	
member 
   : id_or_function_call POINT id_or_function_call 
   | id_or_function_call POINT member
   ;
	
parenthesis_expression 
   : L_PAR expr R_PAR
   ;
	
id_or_function_call  
   : identifier 
   | function_call
   ;
	
function_call 
   : identifier L_PAR argument_list? R_PAR
   ;

param_list
   : KW_OUT? datatype identifier 
   | KW_OUT? datatype identifier COMMA param_list
   ;
		
argument_list 
   : expr 
   | expr COMMA argument_list
   ;
	
logic_operand 
   : KW_TRUE 
   | KW_FALSE
   ;
		
identifier_or_member
   : member 
   | identifier
   ;

datatype
   : inner_datatype L_BRACK generic_parameters R_BRACK 
   | inner_datatype
   ;
		
generic_parameters 
   : datatype 
   | datatype COMMA generic_parameters
   ;

inner_datatype
   : identifier 
   | identifier POINT inner_datatype
   ;


identifier : IDENTIFIER; 

integer : NUMBER; 

decimal : FLOAT_LITERAL; 

string_literal : STRING_LITERAL; 

character_literal : CHARACTER_LITERAL; 



