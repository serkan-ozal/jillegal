/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory;

public class DirectMemoryServiceFactory {

	private static DirectMemoryService directMemoryService = new DirectMemoryServiceImpl();
	
	private DirectMemoryServiceFactory() {
		
	}
	
	public static DirectMemoryService getDirectMemoryService() {
		return directMemoryService;
	}
	
	public static void setDirectMemoryService(DirectMemoryService directMemoryService) {
		DirectMemoryServiceFactory.directMemoryService = directMemoryService;
	}
	
}
