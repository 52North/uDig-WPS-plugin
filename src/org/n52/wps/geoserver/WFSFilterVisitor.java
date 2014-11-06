/***************************************************************
Copyright © 2007 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Theodor Foerster, ITC

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.geoserver;

import java.util.HashMap;
import java.util.Map;

import org.geotools.filter.AttributeExpression;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.visitor.AbstractFilterVisitor;

public class WFSFilterVisitor extends AbstractFilterVisitor {
	
	Map<String, String> filters = new HashMap<String, String>();

	@Override
	public void visit(CompareFilter filter) {
		super.visit(filter);
		String left = null;
		String right = null;
		if(filter.getLeftValue() instanceof AttributeExpression) {
			left = ((AttributeExpression)filter.getLeftValue()).getAttributePath();
		}
		if(filter.getRightValue() instanceof LiteralExpression) {
			LiteralExpression literal = ((LiteralExpression)filter.getRightValue());
			if(literal.getType() == LiteralExpression.LITERAL_DOUBLE) {
				right = Double.toString((Double)literal.getLiteral());
			}
			else if(literal.getType() == LiteralExpression.LITERAL_INTEGER) {
				right = Integer.toString((Integer)literal.getLiteral());
			}
			else if(literal.getType() == LiteralExpression.LITERAL_STRING) {
				right = (String)literal.getLiteral();
			}
		}
		if(right == null || left == null) {
			return;
		}
		filters.put(left, right);
	}
	
	public String getFilterValue(String property) {
		if(! filters.containsKey(property)) {
			return null;
		}
		return filters.get(property);
	}
	
	public Map<String, String> getFilters() {
		return filters;
	}
}
