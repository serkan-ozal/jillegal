/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.javassist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tr.com.serkanozal.jillegal.instrument.AbstractInstrumenter;
import tr.com.serkanozal.jillegal.instrument.GeneratedClass;
import tr.com.serkanozal.jillegal.instrument.Instrumenter;
import tr.com.serkanozal.jillegal.instrument.interceptor.InterceptorServiceFactory;
import tr.com.serkanozal.jillegal.instrument.interceptor.InterceptorServiceImpl;
import tr.com.serkanozal.jillegal.instrument.interceptor.clazz.ClassInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.constructor.AfterConstructorInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.constructor.BeforeConstructorInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.method.AfterMethodInterceptor;
import tr.com.serkanozal.jillegal.instrument.interceptor.method.BeforeMethodInterceptor;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class JavassistInstrumenter<T> extends AbstractInstrumenter<T> {
	
    private ClassPool cp = ClassPool.getDefault();
    private CtClass clazz;
    private List<Class<?>> additionalClasses = new ArrayList<Class<?>>();
    
    public JavassistInstrumenter(Class<T> cls) throws NotFoundException {
        super(cls);
        clazz = cp.get(cls.getName());
        init();
    }
    
    public ClassPool getClassPool() {
        return cp;
    }
    
    public CtClass getClazz() {
        return clazz;
    }
    
    public Class<T> getSourceClass() {
        return sourceClass;
    }
    
    private void init() {
        injectIntercepterCodes();
    }
    
    private void injectIntercepterCodes() {
        try {
            addAdditionalClass(InterceptorServiceImpl.class);
            
            CtConstructor[] ccList = clazz.getDeclaredConstructors();
            if (ccList != null) {
                for (CtConstructor cc : ccList) {
                    injectIntercepterCode(cc);
                }    
            }
  
            CtMethod[] cmList = clazz.getDeclaredMethods();
            if (cmList != null) {
                for (CtMethod cm : cmList) {
                    injectIntercepterCode(cm);
                }    
            }
        }
        catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }
    
    private void injectIntercepterCode(CtConstructor cc) {
        String beforeIntercepterCode = "InterceptorServiceFactory.getInterceptorService().notifyBeforeConstructorInterceptors((Object)$0, $signature, $params);\n";
        String afterIntercepterCode = "InterceptorServiceFactory.getInterceptorService().notifyAfterConstructorInterceptors((Object)$0, $signature, $params);\n";
        String signature = generateSignatureExpression(cc);

        beforeIntercepterCode = beforeIntercepterCode.replace("$signature", signature);
        beforeIntercepterCode = beforeIntercepterCode.replace("$params", "$args");

        afterIntercepterCode = afterIntercepterCode.replace("$signature", signature);
        afterIntercepterCode = afterIntercepterCode.replace("$params", "$args");

        try {
            cc.insertAt(0, beforeIntercepterCode);
        }
        catch (CannotCompileException e) {
            e.printStackTrace();
        }

        try {
            cc.insertAfter(afterIntercepterCode);
        }
        catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }
    
    private void injectIntercepterCode(CtMethod cm) {
        boolean isStatic = Modifier.isStatic(cm.getModifiers());
        String  obj = isStatic ? "null" : "this";
        
        String beforeIntercepterCode = "InterceptorServiceFactory.getInterceptorService().notifyBeforeMethodInterceptors((Object)" + obj + ", $signature, $params);\n";
        String afterIntercepterCode = "InterceptorServiceFactory.getInterceptorService().notifyAfterMethodInterceptors((Object)" + obj + ", $signature, $params);\n";
        String signature = generateSignatureExpression(cm);

        beforeIntercepterCode = beforeIntercepterCode.replace("$signature", signature);
        beforeIntercepterCode = beforeIntercepterCode.replace("$params", "$args");
        
        afterIntercepterCode = afterIntercepterCode.replace("$signature", signature);
        afterIntercepterCode = afterIntercepterCode.replace("$params", "$args");
        
        try {
            cm.insertBefore(beforeIntercepterCode);
        }
        catch (CannotCompileException e) {
            e.printStackTrace();
        }
        try {
            cm.insertAfter(afterIntercepterCode);
        }
        catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings( "unused" )
    private String generateParametersExpression(CtBehavior cb) {
        String params = "";
        try {
            CtClass[] paramTypes = cb.getParameterTypes();
            if (paramTypes == null || paramTypes.length == 0) {
                params = "null";
            }    
            else {
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i > 0) {
                        params += ", ";
                    }    
                    if (paramTypes[i].isPrimitive()) {
                        Class<?> paramClass = getNonPrimitiveType(paramTypes[i].getSimpleName());
                        params = "new " + paramClass.getName() + "(" + "$" + (i + 1) + ")";
                    }
                    else {
                        params = params + "$" + (i + 1);
                    }    
                }
                params = "new Object[]{" + params + "}";
            }
        }
        catch (NotFoundException e) {
            params = "null";
        }
        return params;
    }
    
    private Class<?> getNonPrimitiveType(String clsName) {
        clsName = clsName.toLowerCase().trim();
        if (clsName.equals("boolean")) {
            return Boolean.class;
        }    
        else if (clsName.equals("byte")) {
            return Byte.class;
        }    
        else if (clsName.equals("char")) {
            return Character.class;
        }    
        else if (clsName.equals("shor")) {
            return Short.class;
        }    
        else if (clsName.equals("int")) {
            return Integer.class;
        }    
        else if (clsName.equals("float")) {
            return Float.class;
        }    
        else if (clsName.equals("long")) {
            return Long.class;
        }    
        else if (clsName.equals("double")) {
            return Double.class;
        }    
        else { 
            return null;
        }    
    }
    
    private String generateSignatureExpression(CtBehavior cb) {
        if (cb instanceof CtConstructor) {
            return "\"" + processConstructorSignature(cb.getLongName()) + "\"";
        }    
        else if (cb instanceof CtMethod) {
            return "\"" + processMethodSignature(cb.getLongName()) + "\"";
        }    
        else {
            return "\"" + cb.getLongName()+ "\"";
        }    
    }
    
    @Override
    public JavassistInstrumenter<T> addAdditionalClass(Class<?> additionalClass) throws CannotCompileException {
        additionalClasses.add(additionalClass);
        cp.importPackage(additionalClass.getPackage().getName());
        cp.appendClassPath(new ClassClassPath(additionalClass));
        return this;
    }
    
    @Override
    public JavassistInstrumenter<T> addConstructor(String code, Class<?> [] parameters) throws NotFoundException, CannotCompileException {
        CtClass[] ccParams = convertTypes(parameters);
        CtConstructor cc = new CtConstructor(ccParams, clazz);
        cc.setBody(code);
        clazz.addConstructor(cc);
        injectIntercepterCode(cc);
        return this;
    }
    
    @Override
    public Instrumenter<T> updateConstructor(String code, Class<?> [] parameters) throws NotFoundException, CannotCompileException {
        CtClass[] ctParamTypes = convertTypes(parameters);
        CtConstructor targetConstructor = clazz.getDeclaredConstructor(ctParamTypes);
        if (targetConstructor != null) {
            targetConstructor.setBody(code);
            injectIntercepterCode(targetConstructor);
        }    
        return this;
    }
    
    @Override
    public Instrumenter<T> deleteConstructor(Class<?> [] parameters) throws NotFoundException {
        CtClass[] ctParamTypes = convertTypes(parameters);
        CtConstructor targetConstructor = clazz.getDeclaredConstructor(ctParamTypes);
        if (targetConstructor != null) {
            clazz.removeConstructor(targetConstructor);
        }    
        return this;
    }
    
    @Override
    public JavassistInstrumenter<T> insertBeforeConstructors(String code) throws CannotCompileException {
        return insertToConstructors(code, true);
    }
    
    @Override
    public JavassistInstrumenter<T> insertAfterConstructors(String code) throws CannotCompileException {
        return insertToConstructors(code, false);
    }
    
    private JavassistInstrumenter<T> insertToConstructors(String code, boolean isBefore) throws CannotCompileException {
        CtConstructor[] ccList = clazz.getConstructors();
        for (CtConstructor cc : ccList) {
            if (isBefore) {
                cc.insertBefore(code);
            }    
            else {
                cc.insertAfter(code);
            }    
        }
        return this;
    }
    
    @Override
    public Instrumenter<T> insertBeforeConstructors(BeforeConstructorInterceptor<T> interceptor) {
    	InterceptorServiceFactory.getInterceptorService().addBeforeConstructorsInterceptor(sourceClass, interceptor);
        return this;
    }
    
    @Override
    public Instrumenter<T> insertAfterConstructors(AfterConstructorInterceptor<T> interceptor) {
    	InterceptorServiceFactory.getInterceptorService().addAfterConstructorsInterceptor(sourceClass, interceptor);
        return this;
    }
    
    @Override
    public Instrumenter<T> insertBeforeConstructor(String code, Class<?> [] paramTypes) throws CannotCompileException, NotFoundException {
        return insertToConstructor(code, true, paramTypes);
    }
    
    @Override
    public Instrumenter<T> insertAfterConstructor(String code, Class<?> [] paramTypes) throws CannotCompileException, NotFoundException {
        return insertToConstructor(code, false, paramTypes);
    }
    
    private Instrumenter<T> insertToConstructor(String code, boolean isBefore, Class<?> [] paramTypes) throws CannotCompileException, NotFoundException {
        CtClass[] ctParamTypes = convertTypes(paramTypes);
        CtConstructor targetConstructor = clazz.getDeclaredConstructor(ctParamTypes);
        if (targetConstructor != null) {
            if (isBefore) {
                targetConstructor.insertBefore(code);
            }    
            else {
                targetConstructor.insertAfter(code);
            }    
        }
        return this;
    }
    
    @Override
    public Instrumenter<T> insertBeforeConstructor(BeforeConstructorInterceptor<T> interceptor, Class<?> [] paramTypes) throws NotFoundException {
        CtClass[] ctParamTypes = convertTypes(paramTypes);
        CtConstructor targetConstructor = clazz.getDeclaredConstructor(ctParamTypes);
        if (targetConstructor != null) {
        	InterceptorServiceFactory.getInterceptorService().
        		addBeforeConstructorInterceptor(sourceClass, processConstructorSignature(targetConstructor.getLongName()), interceptor);
        }
        return this;
    }
    
    @Override
    public Instrumenter<T> insertAfterConstructor(AfterConstructorInterceptor<T> interceptor, Class<?> [] paramTypes) throws NotFoundException {
        CtClass[] ctParamTypes = convertTypes(paramTypes);
        CtConstructor targetConstructor = clazz.getDeclaredConstructor(ctParamTypes);
        if (targetConstructor != null) {
        	InterceptorServiceFactory.getInterceptorService().
        		addAfterConstructorInterceptor(sourceClass, processConstructorSignature(targetConstructor.getLongName()), interceptor);
        }
        return this;
    }
    
    @Override
    public Instrumenter<T> addMethod(String methodName, Class<?> returnType, String code, Class<?> [] parameters) throws NotFoundException, CannotCompileException {
        CtClass[] ccParams = convertTypes(parameters);
        CtMethod cm = null;
        if (returnType == null) {
            cm = new CtMethod(CtClass.voidType, methodName, ccParams, clazz);
        }    
        else {  
            cm = new CtMethod(cp.get(returnType.getName()), methodName, ccParams, clazz);
        }    
        cm.setBody(code);
        clazz.addMethod(cm);
        injectIntercepterCode(cm);
        return this;
    }
    
    @Override
    public Instrumenter<T> updateMethod(String methodName, String code, Class<?> [] parameters) throws NotFoundException, CannotCompileException {
        CtClass[] ctParamTypes = convertTypes(parameters);
        CtMethod targetMethod = clazz.getDeclaredMethod(methodName, ctParamTypes);
        if (targetMethod != null) {
            targetMethod.setBody(code);
            injectIntercepterCode(targetMethod);
        }    
        return this;
    }
    
    @Override
    public Instrumenter<T> deleteMethod(String methodName, Class<?> [] parameters) throws NotFoundException {
        CtClass[] ctParamTypes = convertTypes(parameters);
        CtMethod targetMethod = clazz.getDeclaredMethod(methodName, ctParamTypes);
        if (targetMethod != null) {
            clazz.removeMethod(targetMethod);
        }    
        return this;
    }
    
    @Override
    public JavassistInstrumenter<T> insertBeforeMethods(String code) throws CannotCompileException {
        return insertToMethods(code, true);
    }
    
    @Override
    public JavassistInstrumenter<T> insertAfterMethods(String code) throws CannotCompileException {
        return insertToMethods(code, false);
    }
    
    private JavassistInstrumenter<T> insertToMethods(String code, boolean isBefore) throws CannotCompileException {
        CtMethod[] cmList = clazz.getMethods();
        for (CtMethod cm : cmList) {
            if (isBefore) {
                cm.insertBefore(code);
            }    
            else {
                cm.insertAfter(code);
            }    
        }
        return this;
    }
    
    @Override
    public Instrumenter<T> insertBeforeMethods(BeforeMethodInterceptor<T> interceptor) {
    	InterceptorServiceFactory.getInterceptorService().addBeforeMethodsInterceptor(sourceClass, interceptor);
        return this;
    }
    
    @Override
    public Instrumenter<T> insertAfterMethods(AfterMethodInterceptor<T> interceptor) {
    	InterceptorServiceFactory.getInterceptorService().addAfterMethodsInterceptor(sourceClass, interceptor);
        return this;
    }
    
    @Override
    public JavassistInstrumenter<T> insertBeforeMethod(String methodName, String code, Class<?> [] paramTypes) throws CannotCompileException, NotFoundException {
        return insertToMethod(methodName, code, true);
    }
    
    @Override
    public JavassistInstrumenter<T> insertAfterMethod(String methodName, String code, Class<?> [] paramTypes) throws CannotCompileException, NotFoundException {
        return insertToMethod(methodName, code, false);
    }
    
    private JavassistInstrumenter<T> insertToMethod(String methodName, String code, boolean isBefore, Class<?> ... paramTypes) 
                throws NotFoundException, CannotCompileException {
        CtClass[] ctParamTypes = convertTypes(paramTypes);
        CtMethod targetMethod = clazz.getDeclaredMethod(methodName, ctParamTypes);
        if (targetMethod != null) {
            if (isBefore) {
                targetMethod.insertBefore(code);
            }    
            else {   
                targetMethod.insertAfter(code);
            }    
        }
        return this;
    }
    
    @Override
    public Instrumenter<T> insertBeforeMethod(String methodName, BeforeMethodInterceptor<T> interceptor, Class<?> [] paramTypes) 
    		throws NotFoundException {
        CtClass[] ctParamTypes = convertTypes(paramTypes);
        CtMethod targetMethod = clazz.getDeclaredMethod(methodName, ctParamTypes);
        if (targetMethod != null) {
        	InterceptorServiceFactory.getInterceptorService().
        		addBeforeMethodInterceptor(sourceClass, processMethodSignature(targetMethod.getLongName()), interceptor);
        }
        return this;
    }
    
    @Override
    public Instrumenter<T> insertAfterMethod(String methodName, AfterMethodInterceptor<T> interceptor, Class<?> [] paramTypes) throws NotFoundException {
        CtClass[] ctParamTypes = convertTypes(paramTypes);
        CtMethod targetMethod = clazz.getDeclaredMethod(methodName, ctParamTypes);
        if (targetMethod != null) {
        	InterceptorServiceFactory.getInterceptorService().
        		addAfterMethodInterceptor(sourceClass, processMethodSignature(targetMethod.getLongName()), interceptor);
        }
        return this;
    }
    
    private CtClass[] convertTypes(Class<?>[] types) throws NotFoundException
    {
        CtClass[] ctTypes = null;
        if (types != null) {
            ctTypes = new CtClass[types.length];
            for (int i = 0; i < types.length; i++) {
                Class<?> typeClass = types[i];
                CtClass ctClass = cp.get(typeClass.getName());
                ctTypes[i] = ctClass;
            }
        }
        else {
            ctTypes = new CtClass[0];
        }
        return ctTypes;
    }

    private String processConstructorSignature(String signature) {
        int paramStartIndex = signature.lastIndexOf("(");
        String params = signature.substring(paramStartIndex,  signature.length());
        String classFullName = signature.substring(0,  paramStartIndex);
        String className = classFullName.substring(classFullName.lastIndexOf(".") + 1, classFullName.length());
        return classFullName + "#" + className + params;
    }
    
    private String processMethodSignature(String signature) {
        int paramStartIndex = signature.lastIndexOf("(");
        String params = signature.substring(paramStartIndex, signature.length());
        String methodFullName = signature.substring(0,  paramStartIndex);
        int methodStartIndex = methodFullName.lastIndexOf(".");
        String methodName = methodFullName.substring(methodStartIndex + 1, methodFullName.length());
        String classFullName = methodFullName.substring(0,  methodStartIndex);
        return classFullName + "#" + methodName + params;
    }
    
    @Override
    public Instrumenter<T> addClassInterceptor(ClassInterceptor<T> interceptor) {
        InterceptorServiceFactory.getInterceptorService().addClassInterceptor(sourceClass, interceptor);
        return this;
    }
    
    @Override
    public GeneratedClass<T> build() throws IOException, CannotCompileException {
        clazz.defrost();
        return new GeneratedClass<T>(sourceClass, clazz.toBytecode());
    }
    
    @Override
    public void saveToFile() throws NotFoundException, IOException, CannotCompileException {
        clazz.defrost();
        clazz.rebuildClassFile();
        clazz.writeFile();
    }
    
    @Override
    public void saveToFile(String dirName) throws NotFoundException, IOException, CannotCompileException {
        clazz.defrost();
        clazz.rebuildClassFile();
        clazz.writeFile(dirName);
    }
}
