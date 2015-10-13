package services.commons;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectManipulation {
	public static Object loadObject(String name) throws ClassNotFoundException,
			FileNotFoundException, IOException {
		ObjectInputStream iIn = new ObjectInputStream(new FileInputStream(name));
		Object a = iIn.readObject();
		iIn.close();
		return a;
	}

	public static void saveObject(Object a, String name)
			throws FileNotFoundException, IOException {
		ObjectOutputStream oOut = new ObjectOutputStream(new FileOutputStream(
				name));
		oOut.writeObject(a);
		oOut.close();
	}
}
