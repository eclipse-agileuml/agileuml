// Generated from PlantUML.g4 by ANTLR 4.8
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PlantUMLParser}.
 */
public interface PlantUMLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#classDiagram}.
	 * @param ctx the parse tree
	 */
	void enterClassDiagram(PlantUMLParser.ClassDiagramContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#classDiagram}.
	 * @param ctx the parse tree
	 */
	void exitClassDiagram(PlantUMLParser.ClassDiagramContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#cdelement}.
	 * @param ctx the parse tree
	 */
	void enterCdelement(PlantUMLParser.CdelementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#cdelement}.
	 * @param ctx the parse tree
	 */
	void exitCdelement(PlantUMLParser.CdelementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#classDefinition}.
	 * @param ctx the parse tree
	 */
	void enterClassDefinition(PlantUMLParser.ClassDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#classDefinition}.
	 * @param ctx the parse tree
	 */
	void exitClassDefinition(PlantUMLParser.ClassDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#classElement}.
	 * @param ctx the parse tree
	 */
	void enterClassElement(PlantUMLParser.ClassElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#classElement}.
	 * @param ctx the parse tree
	 */
	void exitClassElement(PlantUMLParser.ClassElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#enumDefinition}.
	 * @param ctx the parse tree
	 */
	void enterEnumDefinition(PlantUMLParser.EnumDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#enumDefinition}.
	 * @param ctx the parse tree
	 */
	void exitEnumDefinition(PlantUMLParser.EnumDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#circleInterface}.
	 * @param ctx the parse tree
	 */
	void enterCircleInterface(PlantUMLParser.CircleInterfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#circleInterface}.
	 * @param ctx the parse tree
	 */
	void exitCircleInterface(PlantUMLParser.CircleInterfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#attribute}.
	 * @param ctx the parse tree
	 */
	void enterAttribute(PlantUMLParser.AttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#attribute}.
	 * @param ctx the parse tree
	 */
	void exitAttribute(PlantUMLParser.AttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#internalAttribute}.
	 * @param ctx the parse tree
	 */
	void enterInternalAttribute(PlantUMLParser.InternalAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#internalAttribute}.
	 * @param ctx the parse tree
	 */
	void exitInternalAttribute(PlantUMLParser.InternalAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#method}.
	 * @param ctx the parse tree
	 */
	void enterMethod(PlantUMLParser.MethodContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#method}.
	 * @param ctx the parse tree
	 */
	void exitMethod(PlantUMLParser.MethodContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#internalMethod}.
	 * @param ctx the parse tree
	 */
	void enterInternalMethod(PlantUMLParser.InternalMethodContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#internalMethod}.
	 * @param ctx the parse tree
	 */
	void exitInternalMethod(PlantUMLParser.InternalMethodContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#parameters}.
	 * @param ctx the parse tree
	 */
	void enterParameters(PlantUMLParser.ParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#parameters}.
	 * @param ctx the parse tree
	 */
	void exitParameters(PlantUMLParser.ParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(PlantUMLParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(PlantUMLParser.ParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#stereotype}.
	 * @param ctx the parse tree
	 */
	void enterStereotype(PlantUMLParser.StereotypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#stereotype}.
	 * @param ctx the parse tree
	 */
	void exitStereotype(PlantUMLParser.StereotypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#visibility}.
	 * @param ctx the parse tree
	 */
	void enterVisibility(PlantUMLParser.VisibilityContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#visibility}.
	 * @param ctx the parse tree
	 */
	void exitVisibility(PlantUMLParser.VisibilityContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#modifier}.
	 * @param ctx the parse tree
	 */
	void enterModifier(PlantUMLParser.ModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#modifier}.
	 * @param ctx the parse tree
	 */
	void exitModifier(PlantUMLParser.ModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(PlantUMLParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(PlantUMLParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#inheritance}.
	 * @param ctx the parse tree
	 */
	void enterInheritance(PlantUMLParser.InheritanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#inheritance}.
	 * @param ctx the parse tree
	 */
	void exitInheritance(PlantUMLParser.InheritanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#leftAncestorSymbol}.
	 * @param ctx the parse tree
	 */
	void enterLeftAncestorSymbol(PlantUMLParser.LeftAncestorSymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#leftAncestorSymbol}.
	 * @param ctx the parse tree
	 */
	void exitLeftAncestorSymbol(PlantUMLParser.LeftAncestorSymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#rightAncestorSymbol}.
	 * @param ctx the parse tree
	 */
	void enterRightAncestorSymbol(PlantUMLParser.RightAncestorSymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#rightAncestorSymbol}.
	 * @param ctx the parse tree
	 */
	void exitRightAncestorSymbol(PlantUMLParser.RightAncestorSymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#interfaceInheritance}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceInheritance(PlantUMLParser.InterfaceInheritanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#interfaceInheritance}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceInheritance(PlantUMLParser.InterfaceInheritanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#leftImplementsSymbol}.
	 * @param ctx the parse tree
	 */
	void enterLeftImplementsSymbol(PlantUMLParser.LeftImplementsSymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#leftImplementsSymbol}.
	 * @param ctx the parse tree
	 */
	void exitLeftImplementsSymbol(PlantUMLParser.LeftImplementsSymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#rightImplementsSymbol}.
	 * @param ctx the parse tree
	 */
	void enterRightImplementsSymbol(PlantUMLParser.RightImplementsSymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#rightImplementsSymbol}.
	 * @param ctx the parse tree
	 */
	void exitRightImplementsSymbol(PlantUMLParser.RightImplementsSymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#association}.
	 * @param ctx the parse tree
	 */
	void enterAssociation(PlantUMLParser.AssociationContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#association}.
	 * @param ctx the parse tree
	 */
	void exitAssociation(PlantUMLParser.AssociationContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#associationSymbol}.
	 * @param ctx the parse tree
	 */
	void enterAssociationSymbol(PlantUMLParser.AssociationSymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#associationSymbol}.
	 * @param ctx the parse tree
	 */
	void exitAssociationSymbol(PlantUMLParser.AssociationSymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#leftAssociationSymbol}.
	 * @param ctx the parse tree
	 */
	void enterLeftAssociationSymbol(PlantUMLParser.LeftAssociationSymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#leftAssociationSymbol}.
	 * @param ctx the parse tree
	 */
	void exitLeftAssociationSymbol(PlantUMLParser.LeftAssociationSymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#aggregation}.
	 * @param ctx the parse tree
	 */
	void enterAggregation(PlantUMLParser.AggregationContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#aggregation}.
	 * @param ctx the parse tree
	 */
	void exitAggregation(PlantUMLParser.AggregationContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#aggregationSymbol}.
	 * @param ctx the parse tree
	 */
	void enterAggregationSymbol(PlantUMLParser.AggregationSymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#aggregationSymbol}.
	 * @param ctx the parse tree
	 */
	void exitAggregationSymbol(PlantUMLParser.AggregationSymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#leftAggregationSymbol}.
	 * @param ctx the parse tree
	 */
	void enterLeftAggregationSymbol(PlantUMLParser.LeftAggregationSymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#leftAggregationSymbol}.
	 * @param ctx the parse tree
	 */
	void exitLeftAggregationSymbol(PlantUMLParser.LeftAggregationSymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#composition}.
	 * @param ctx the parse tree
	 */
	void enterComposition(PlantUMLParser.CompositionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#composition}.
	 * @param ctx the parse tree
	 */
	void exitComposition(PlantUMLParser.CompositionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#compositionSymbol}.
	 * @param ctx the parse tree
	 */
	void enterCompositionSymbol(PlantUMLParser.CompositionSymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#compositionSymbol}.
	 * @param ctx the parse tree
	 */
	void exitCompositionSymbol(PlantUMLParser.CompositionSymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#leftCompositionSymbol}.
	 * @param ctx the parse tree
	 */
	void enterLeftCompositionSymbol(PlantUMLParser.LeftCompositionSymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#leftCompositionSymbol}.
	 * @param ctx the parse tree
	 */
	void exitLeftCompositionSymbol(PlantUMLParser.LeftCompositionSymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#dependency}.
	 * @param ctx the parse tree
	 */
	void enterDependency(PlantUMLParser.DependencyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#dependency}.
	 * @param ctx the parse tree
	 */
	void exitDependency(PlantUMLParser.DependencyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#dependencySymbol}.
	 * @param ctx the parse tree
	 */
	void enterDependencySymbol(PlantUMLParser.DependencySymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#dependencySymbol}.
	 * @param ctx the parse tree
	 */
	void exitDependencySymbol(PlantUMLParser.DependencySymbolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#lineAnnotation}.
	 * @param ctx the parse tree
	 */
	void enterLineAnnotation(PlantUMLParser.LineAnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#lineAnnotation}.
	 * @param ctx the parse tree
	 */
	void exitLineAnnotation(PlantUMLParser.LineAnnotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#direction}.
	 * @param ctx the parse tree
	 */
	void enterDirection(PlantUMLParser.DirectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#direction}.
	 * @param ctx the parse tree
	 */
	void exitDirection(PlantUMLParser.DirectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(PlantUMLParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(PlantUMLParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#stringExpression}.
	 * @param ctx the parse tree
	 */
	void enterStringExpression(PlantUMLParser.StringExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#stringExpression}.
	 * @param ctx the parse tree
	 */
	void exitStringExpression(PlantUMLParser.StringExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PlantUMLParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntegerLiteral(PlantUMLParser.IntegerLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link PlantUMLParser#integerLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntegerLiteral(PlantUMLParser.IntegerLiteralContext ctx);
}