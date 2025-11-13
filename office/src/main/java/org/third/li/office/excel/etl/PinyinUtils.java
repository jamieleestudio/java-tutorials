package org.third.li.office.excel.etl;

import net.sourceforge.pinyin4j.PinyinHelper;

public class PinyinUtils {

    public static String getFirstLetter(String chinese) {
        StringBuilder result = new StringBuilder();
        for(char c : chinese.toCharArray()) {
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
            if(pinyinArray != null) {
                result.append(pinyinArray[0].charAt(0));
            } else {
                result.append(c);
            }
        }
        return result.toString().toUpperCase();
    }

}
