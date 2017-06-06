package smo.zooket.Util;

import java.security.MessageDigest;

/**
 * Created by Smo on 5/7/2017.
 */
public class Utils {

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String ts) {
        try {
            ts = ts + "Supermarket";
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(ts.getBytes("UTF-8"), 0, ts.length());
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        } catch (Exception ex) {
            return "";
        }
    }
}

