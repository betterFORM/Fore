/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xpath.impl.saxon;

import net.sf.saxon.expr.*;
import net.sf.saxon.expr.flwor.TupleExpression;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.NumberInstruction;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.sort.ConditionalSorter;
import net.sf.saxon.expr.sort.SortExpression;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.PatternSponsor;
import net.sf.saxon.tree.util.FastStringBuffer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import net.sf.saxon.expr.instruct.SimpleContentConstructor;
//import net.sf.saxon.sort.TupleSorter;

/**
 * @author Nick Van den Bleeken
 * @author Ronald van Kuijk
 * @version $Id$
 */
public class SaxonXPathExpressionSerializer {
    public static String serialize(Expression expr, Map prefixMapping) {
        FastStringBuffer result = new FastStringBuffer(64);

        final Map<String, String> reversePrefixMapping = new HashMap<String, String>();
        for (Iterator<Map.Entry<String, String>> it = prefixMapping.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            reversePrefixMapping.put(entry.getValue(), entry.getKey());
        }

        serialize(result, expr, reversePrefixMapping);

        return result.toString();
    }

    /**
     * @param result
     * @param expr
     */
    private static void serialize(FastStringBuffer result, Expression expr, Map reversePrefixMapping) {
        if (expr instanceof Assignation) {
            serialize(result, ((Assignation) expr).getAction(), reversePrefixMapping);
        } else if (expr instanceof AxisExpression) {
            AxisExpression axisExpression = (AxisExpression) expr;
            result.append(Axis.axisName[axisExpression.getAxis()]);
            result.append("::");

            final NodeTest nodeTest = axisExpression.getNodeTest();
            if (nodeTest == null) {
                result.append("node()");
            } else if (nodeTest instanceof NameTest) {
              NameTest nt = (NameTest) nodeTest;
              NamePool namePool = nt.getNamePool();
              String localName = namePool.getLocalName(nt.getFingerprint());
              String prefix = namePool.getPrefix(nt.getFingerprint());
              String uri = namePool.getURI(nt.getFingerprint());
              
              localName = (uri != null && !uri.trim().isEmpty())
            		  	? ("{" + uri +"}" + localName)
            		  	: localName;
              
              localName = (prefix != null && ! prefix.trim().isEmpty()) 
            		  		  ? (prefix + ":"+localName) 
            				  : localName;
              
              result.append(fixPreFixes(localName, reversePrefixMapping));
            } else {
              result.append(fixPreFixes(nodeTest.toString(), reversePrefixMapping));
            }
        } else if (expr instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expr;
            serialize(result, binaryExpression.getOperands()[0], reversePrefixMapping);
            result.append(" " + Token.tokens[binaryExpression.getOperator()] + " ");
            serialize(result, binaryExpression.getOperands()[1], reversePrefixMapping);
        } else if (expr instanceof CompareToIntegerConstant) {
            CompareToIntegerConstant compareToIntegerConstant = (CompareToIntegerConstant) expr;
            serialize(result, compareToIntegerConstant.getOperand(), reversePrefixMapping);
            result.append(" " + Token.tokens[compareToIntegerConstant.getComparisonOperator()] + " ");
            result.append(Long.toString(compareToIntegerConstant.getComparand()));
        } else if (expr instanceof ConditionalSorter) {
            // XXX not yet supported
        } else if (expr instanceof ContextItemExpression) {
            result.append('.');
        } else if (expr instanceof ErrorExpression) {
            // Error do nothing
        } else if (expr instanceof FilterExpression) {
            FilterExpression filterExpression = (FilterExpression) expr;
            serialize(result, filterExpression.getControllingExpression(), reversePrefixMapping);
            result.append('[');
            serialize(result, filterExpression.getFilter(), reversePrefixMapping);
            result.append("]");

        } else if (expr instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall) expr;
            StructuredQName name = functionCall.getFunctionName();
            

            // From the docs:
            // saxon:item-at($seq as item()*, $index as numeric?) ==> item()?
            // This function returns the item at a given position in a sequence. 
            // The index counts from one. If the index is an empty sequence, or less than one,
            // or not a whole number, or greater than the length of the sequence, 
            // the result is an empty sequence.
            //
            // This function is provided largely because it is used internally by the Saxon optimizer.
            // For user applications, it is better to use $seq[$index] which will return the 
            // same result provided there are no context-dependencies in $index, or subsequence($seq, $index, 1)
            // which will return the same result in all cases where $index evaluates to an integer.
            
            // Since it returns saxon:item-at here we rewrite this.
            
            if (name.getPrefix() != null && name.getPrefix().length() > 0) {
          
            	if(!(name.getPrefix().equals("saxon") && name.getLocalPart().equals("item-at"))) {
            		result.append(name.getPrefix());
                	result.append(":");
            	}
            }
            if(name.getPrefix().equals("saxon") && name.getLocalPart().equals("item-at")) {
        		result.append("subsequence");
        	} else {
        		result.append(name.getLocalPart());
        	}
            result.append("(");

            Iterator iter = functionCall.iterateSubExpressions();
            boolean first = true;
            while (iter.hasNext()) {
                result.append(first ? "" : ", ");
                SaxonXPathExpressionSerializer.serialize(result, (Expression) iter.next(), reversePrefixMapping);
                first = false;
            }
            
        	if(name.getPrefix().equals("saxon") && name.getLocalPart().equals("item-at")) {
        		result.append(",1");
        	}

            result.append(")");
        } else if (expr instanceof Instruction) {
            // This is not an XPath expression
        } else if (expr instanceof IntegerRangeTest) {
            // XXX not yet supported
        } else if (expr instanceof IsLastExpression) {
            result.append("position() eq last()");
        } else if (expr instanceof Literal) {
            Literal literal = (Literal) expr;
            result.append(literal.getValue().toString());
        } else if (expr instanceof NumberInstruction) {
            // This is not an XPath expression
        } else if (expr instanceof SlashExpression) {
            SlashExpression slashExpression = (SlashExpression) expr;
//            result.append('(');
            serialize(result, slashExpression.getControllingExpression(), reversePrefixMapping);
            result.append('/');
            serialize(result, slashExpression.getControlledExpression() , reversePrefixMapping);
//            result.append(')');
        }  else if (expr instanceof PatternSponsor) {
            // XXX not yet supported
        } else if (expr instanceof SimpleExpression) {
            // This is not an XPath expression
        }
/*
    else if (expr instanceof SimpleMappingExpression) {
	    // XXX not yet supported
	}
*/
        else if (expr instanceof ParentNodeExpression) {
            result.append("..");
        } else if (expr instanceof RootExpression) {
            // do nothing
        } else if (expr instanceof SortExpression) {
            // XXX not yet supported
        } else if (expr instanceof TailExpression) {
            // XXX not yet supported
        } else if (expr instanceof TupleExpression) {
            // This is not an XPath expression
        } else if (expr instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expr;
            serialize(result, unaryExpression.getBaseExpression(), reversePrefixMapping); // Not sure if this is correct in all cases
        } else if (expr instanceof VariableReference) {
            VariableReference variableReference = (VariableReference) expr;
            Binding binding = variableReference.getBinding();
            if (binding instanceof LetExpression) {
              LetExpression let = (LetExpression) binding;
              serialize(result, let.getSequence(), reversePrefixMapping);
            }
        }
    }

    private static String fixPreFixes(String xpath, Map<String, String> reversePrefixMapping) {
        FastStringBuffer result = new FastStringBuffer(xpath.length());

        Matcher m = Pattern.compile("\\{[^\\}]+\\}").matcher(xpath);

        int iLast = 0;
        while (m.find()) {
            result.append(xpath.substring(iLast, m.start()));
            final String match = m.group();
            result.append(reversePrefixMapping.get(match.substring(1, match.length() - 1)));
            result.append(':');
            iLast = m.end();
        }

        result.append(xpath.substring(iLast));
        return result.toString();
    }
}
