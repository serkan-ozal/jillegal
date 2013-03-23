/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.instrument.monitoring;

public class MonitoringServiceFactory {

	private static MonitoringService monitoringService = new MonitoringServiceImpl();
	
	private MonitoringServiceFactory() {
		
	}
	
	public static MonitoringService getMonitoringService() {
		return monitoringService;
	}
	
	public static void setMonitoringService(MonitoringService monitoringService) {
		MonitoringServiceFactory.monitoringService = monitoringService;
	}
	
}
