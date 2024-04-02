import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringHasher {
    
    private static MessageDigest messageDigest = initMessageDigest();

    private static MessageDigest initMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException nse) {
            return null;
        }
    }

    public static String Hash(String str) {
        if (messageDigest != null && str != null) {
            byte[] bytes = messageDigest.digest(str.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : bytes) {
                int unsignedByte = b & 0xff;
                int firstDigit = unsignedByte >> 4;
                int secondDigit = unsignedByte & 0xF;
                hexString.append(Character.forDigit(firstDigit, 16));
                hexString.append(Character.forDigit(secondDigit, 16));
            }
            return hexString.toString();
        }
        return "";
    }

}
