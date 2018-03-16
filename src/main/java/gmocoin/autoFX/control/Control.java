package gmocoin.autoFX.control;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Control {
	private static Control instance;
	private Properties prop;  
	private Control(){
		prop = new Properties();
		try {
			prop.load(new FileInputStream("control.properities"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static Control getInstance(){
		if(instance == null){
			instance = new Control();
		}
		return instance;
	}
	public String getProperty(String key){
		return prop.getProperty(key);
	}
	public void setProperty(String key,String val){
		prop.setProperty(key,val);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream("control.properities");
			prop.store(fos, "the primary key of article table");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
