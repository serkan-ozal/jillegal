/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.memory;

import java.beans.PropertyChangeSupport;

public class DirectMemoryServiceFactory {

	public static final String DIRECT_MEMORY_SERVICE_$_SET_OBJECT_FIELD_ACCESSOR = 
				"DirectMemoryService_setObjectField";
	
	private static DirectMemoryService directMemoryService = new DirectMemoryServiceImpl();
	private static DirectMemoryService_setObjectField directMemoryService_setObjectField = 
				new DirectMemoryService_setObjectField();
	
	private DirectMemoryServiceFactory() {
		
	}
	
	public static void init() {
		System.getProperties().
			put(	DIRECT_MEMORY_SERVICE_$_SET_OBJECT_FIELD_ACCESSOR, 
					directMemoryService_setObjectField);
	}
	
	public static DirectMemoryService getDirectMemoryService() {
		return directMemoryService;
	}
	
	public static void setDirectMemoryService(DirectMemoryService directMemoryService) {
		DirectMemoryServiceFactory.directMemoryService = directMemoryService;
	}

	@SuppressWarnings("serial")
	private static class DirectMemoryService_setObjectField extends PropertyChangeSupport {

		private DirectMemoryService_setObjectField() {
			super("");
		}

		@Override
		public void fireIndexedPropertyChange(String propertyName, int index,
				Object oldValue, Object newValue) {
			directMemoryService.setObjectField(oldValue, index, newValue);
		}
		
	}
	
}
