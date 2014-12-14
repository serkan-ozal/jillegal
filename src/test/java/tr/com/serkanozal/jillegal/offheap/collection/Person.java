/**
 * @author SERKAN OZAL
 *         
 *         E-Mail: <a href="mailto:serkanozal86@hotmail.com">serkanozal86@hotmail.com</a>
 *         GitHub: <a>https://github.com/serkan-ozal</a>
 */

package tr.com.serkanozal.jillegal.offheap.collection;

public class Person {

	private long id;
	private String username;
	private String firstName;
	private String lastName;
//	@OffHeapObject
//	private Date birthDate;
	private int accountNo;
	private double debt;
	
	public Person() {
		
	}
	
	public Person(long id, String username, String firstName, String lastName, 
//			Date birthDate, 
			int accountNo, double debt) {
		this.id = id;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
//		this.birthDate = birthDate;
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
	
//	public Date getBirthDate() {
//		return birthDate;
//	}
//	
//	public void setBirthDate(Date birthDate) {
//		this.birthDate = birthDate;
//	}

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
//				+ ", birthDate=" + birthDate 
				+ ", accountNo=" + accountNo
				+ ", debt=" + debt + "]";
	}
	
	@Override
	public int hashCode() {
		// TODO Handle integer overflow
		return (int) id;
	}
	
}
