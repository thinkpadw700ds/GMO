package gmocoin.autoFX.Collabo.sp;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo {
	private int userId;
	private int accountOpenStatus;
	private String address;
	private String addressCode;
	private String addressCover;
	private String birthday;
	private String firstName;
	private String firstNameKana;
	private String gender;
	private String lastName;
	private String lastNameKana;
	private String mailAddress;
	private String postalCode;
	private String tel;
	public UserInfo(JSONObject json){
		try {
			this.userId = json.getInt("userId");
			this.accountOpenStatus = json.getInt("accountOpenStatus");
			this.address = json.getString("address");
			this.addressCode = json.getString("addressCode");
			this.addressCover = json.getString("addressCover");
			this.birthday = json.getString("birthday");
			this.firstName = json.getString("firstName");
			this.firstNameKana = json.getString("firstNameKana");
			this.gender = json.getString("gender");
			this.lastName = json.getString("lastName");
			this.lastNameKana = json.getString("lastNameKana");
			this.mailAddress = json.getString("mailAddress");
			this.postalCode = json.getString("postalCode");
			this.tel = json.getString("tel");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int getUserId() {
		return userId;
	}
	public int getAccountOpenStatus() {
		return accountOpenStatus;
	}
	public String getAddress() {
		return address;
	}
	public String getAddressCode() {
		return addressCode;
	}
	public String getAddressCover() {
		return addressCover;
	}
	public String getBirthday() {
		return birthday;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getFirstNameKana() {
		return firstNameKana;
	}
	public String getGender() {
		return gender;
	}
	public String getLastName() {
		return lastName;
	}
	public String getLastNameKana() {
		return lastNameKana;
	}
	public String getMailAddress() {
		return mailAddress;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public String getTel() {
		return tel;
	}
	
}
