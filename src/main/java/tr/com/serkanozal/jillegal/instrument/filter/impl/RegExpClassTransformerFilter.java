/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.filter.impl;

import java.lang.instrument.ClassFileTransformer;


import javassist.CtClass;

public class RegExpClassTransformerFilter extends BaseClassTransformerFilter {
	
    private String regExp;
  
    public RegExpClassTransformerFilter(String regExp, ClassFileTransformer classTransformer) {
        super(classTransformer);
        this.regExp = regExp;
    }
   
    public String getRegExp() {
        return regExp;
    }
    
    public void setRegExp(String regExp) {
        this.regExp = regExp;
    }
    
    @Override
    public boolean useFilter(CtClass cc) {
        return cc.getName().matches( regExp);
    }
    
}
