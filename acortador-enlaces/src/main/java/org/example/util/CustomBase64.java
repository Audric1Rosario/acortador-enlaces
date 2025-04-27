package org.example.util;

import org.example.util.exception.CustomBase64DecodingException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

//Singleton
public abstract class CustomBase64 {
    //Java no trabaja con unsigned, so...
    //sacado de java.util.Base64
    private static final char[] charset = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
    };

    private static Map<Character, BigInteger> bigIntSet;
    static {
        bigIntSet = new HashMap<>();
        for (int i = 0; i < 64; i++) {
            bigIntSet.put(charset[i], BigInteger.valueOf(i));
        }
    }

    private static final BigInteger bInt64 = BigInteger.valueOf(64);

    public static String encode(BigInteger integer) {
        if(integer.equals(BigInteger.ZERO))
            return "A";
        StringBuilder encoded = new StringBuilder();
        while (!integer.equals(BigInteger.ZERO)) {
            encoded.append(charset[integer.mod(bInt64).intValue()]);
            integer = integer.divide(bInt64);
        }
        return encoded.reverse().toString();
    }

    public static BigInteger decode(String encoded) throws CustomBase64DecodingException {
        int i = encoded.length()-1;
        BigInteger bigInteger = bigIntSet.get(encoded.charAt(i));
        if(bigInteger==null)
            throw new CustomBase64DecodingException(String.format("Invalid char in %s at position %d.",encoded,i));
        BigInteger power = bInt64;
        try {
            for (i -= 1; i >= 0; i--) {
                bigInteger = bigInteger.add(bigIntSet.get(encoded.charAt(i)).multiply(power));
                power = power.multiply(bInt64);
            }
        } catch (NullPointerException e) {
            throw new CustomBase64DecodingException(String.format("Invalid char in %s at position %d.",encoded,i));
        }
        return bigInteger;
    }
}
