package com.neofinance.quickdoc.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class KeyUtil {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static UUID randomUUID() {
        return UUID.randomUUID();
    }

    public static String stringUUID() {
        return randomUUID().toString();
    }

    public static long longID(long range) {
        return System.currentTimeMillis() * range + (long) (Math.random() * range);
    }

    public static long longID() {
        return longID(1000000L);
    }

    /**
     * 生成如下格式UUID
     * SHA256 UUID = 3155DF76F0991F64B10F62FDEA4CEE36E391B95010BD42F50B3C2DD1119072C1
     *
     * @return
     */
    public static String getSHA256UUID() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest salt = MessageDigest.getInstance("SHA-256");
        salt.update(randomUUID().toString().getBytes("UTF-8"));
        return bytesToHex(salt.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
/*
    public static void main(String[] args) throws Exception {
        UUID uuid = randomUUID();
        String randomUUIDString = uuid.toString();
        String sha256 = getSHA256UUID();

        System.out.println("Random UUID String = " + randomUUIDString);
        System.out.println("UUID version       = " + uuid.version());
        System.out.println("UUID variant       = " + uuid.variant());
        System.out.println("SHA256 UUID        = " + sha256);
        System.err.println("LONG ID(10000)     = " + longID());

        // 测试随机数冲突范围
        for (long base = 100; base <= 1000000; base = base * 10) {
            long range = base;
            int conflict = 0;
            Map<Long, Long> map = new HashMap<Long, Long>();
            for (int i = 0; i < range; i++) {
                long id = longID(range);
                if (map.get(id) == null) {
                    map.put(id, id);
                } else {
                    conflict++;
                }
            }
            System.err.println("LONG ID(" + range + ")     = " + longID(range));
            System.err.println("Total conflict times: " + conflict + "in range{" + range + "}");
            System.err.println("Total conflict ratios: " + (double) conflict / range);
        }
    }*/
}
