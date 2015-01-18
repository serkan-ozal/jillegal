/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.collection;

import java.util.Date;

import tr.com.serkanozal.jillegal.offheap.OffHeapAwareObject;
import tr.com.serkanozal.jillegal.offheap.memory.DirectMemoryService;
import tr.com.serkanozal.jillegal.offheap.service.OffHeapService;

public class Person implements OffHeapAwareObject {

	protected static final long MILLI_SECONDS_IN_A_DAY = 24 * 60 * 60 * 1000;
	protected static final long MILLI_SECONDS_IN_A_MONTH = 30 * MILLI_SECONDS_IN_A_DAY;
	protected static final long MILLI_SECONDS_IN_A_YEAR = 365 * MILLI_SECONDS_IN_A_DAY;
	
	private long id;
	private String username;
	private String firstName;
	private String lastName;
//	@OffHeapObject
//	private Date birthDate;
	private long birthDate;
	private int accountNo;
	private double debt;
	
	public Person() {
		
	}
	
	public Person(long id, String username, String firstName, String lastName, 
			long birthDate, int accountNo, double debt) {
		this.id = id;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthDate = birthDate;
		this.accountNo = accountNo;
		this.debt = debt;
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public long getBirthDate() {
		return birthDate;
	}
	
	public String getBirthDateFormatted() {
		int year = 1970 + (int) (birthDate / MILLI_SECONDS_IN_A_YEAR);
		int month = (int) ((birthDate / MILLI_SECONDS_IN_A_MONTH) % 12);
		int day = (int) (((birthDate / MILLI_SECONDS_IN_A_DAY) % 365) % 29); // TODO Handle years with 366 days
		return 
			((day < 10) ? "0" : "") + day + "/" + 
			((month < 10) ? "0" : "") + month + "/" + 
			year;
	}
	
	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}
	
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate.getTime();
	}

	public int getAccountNo() {
		return accountNo;
	}
	
	public void setAccountNo(int accountNo) {
		this.accountNo = accountNo;
	}
	
	public double getDebt() {
		return debt;
	}
	
	public void setDebt(double debt) {
		this.debt = debt;
	}

	@Override
	public String toString() {
		return "Person [id=" + id 
				+ ", username=" + username
				+ ", firstName=" + firstName 
				+ ", lastName=" + lastName
				+ ", birthDate=" + getBirthDateFormatted()
				+ ", accountNo=" + accountNo
				+ ", debt=" + debt + "]";
	}
	
	@Override
	public int hashCode() {
		// TODO Handle integer overflow
		return (int) id;
	}

	@Override
	public void onGet(OffHeapService offHeapService, DirectMemoryService directMemoryService) {
		
	}

	@Override
	public void onFree(OffHeapService offHeapService, DirectMemoryService directMemoryService) {
		offHeapService.freeString(username);
		offHeapService.freeString(firstName);
		offHeapService.freeString(lastName);
	}
	
}
