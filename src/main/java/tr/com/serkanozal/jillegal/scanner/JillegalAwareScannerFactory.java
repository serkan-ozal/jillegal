/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.scanner;

public class JillegalAwareScannerFactory {
	
	private static JillegalAwareScanner jillegalAwareScanner = new JillegalAwareScannerImpl();
	
	private JillegalAwareScannerFactory() {
		
	}
	
	public static JillegalAwareScanner getJillegalAwareScanner() {
		return jillegalAwareScanner;
	}
	
	public static void setJillegalAwareScanner(JillegalAwareScanner jillegalAwareScanner) {
		JillegalAwareScannerFactory.jillegalAwareScanner = jillegalAwareScanner;
	}
	
}
