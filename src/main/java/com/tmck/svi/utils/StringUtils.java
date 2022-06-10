package com.tmck.svi.utils;

import java.util.Collection;

/**
 * Generic String Utilities
 * 
 * @author tim
 *
 */
public class StringUtils {

	public static final String [] EMPTY_ARRAY = {}; 

	public static String merge(String [] str) {

		assert str != null : "no string array";
		
		int len = 0;
		
		for(int i = 0; i < str.length; i++) {
			assert str[i] != null : "no string array in array at index: " + i;
			len += str[i].length();
		}
		
		StringBuffer buf = new StringBuffer(len);
		
		for(int i = 0; i < str.length; i++) {
			buf.append(str[i]);
		}
		
		return buf.toString();
		
	}

	public static String substitute(String source, char charToReplace, char substituteChar) {
		
		assert source != null : "no source String";
		
		StringBuffer buf = new StringBuffer(source.length());
		int len = source.length();
		
		for(int i = 0; i < len; i++) {
			char c = source.charAt(i);
			if(c == charToReplace) {
				c = substituteChar;				
			}
			buf.append(c);
		}
		
		return buf.toString();
		
	}

	public static String substitute(String source, String strToReplace, String substituteStr) {
		
		assert source != null : "no source String";
		assert strToReplace != null : "no strToReplace";
		assert substituteStr != null : "no substituteStr";
		
		StringBuffer buf = new StringBuffer(source.length());
		
		int oldIdx = 0;
		int index = 0;
		
		while((index = source.indexOf(strToReplace, oldIdx)) >= 0) {
						
			String str = source.substring(oldIdx, index);
			buf.append(str + substituteStr);
			
			oldIdx = index + strToReplace.length();
			
		}
		
		String appended = source.substring(oldIdx);
		buf.append(appended);
		
		return buf.toString();
		
	}


	public static String[] convertCollectionToArray(Collection<String> list) {
		assert list != null : "no list";
		return (String []) list.toArray(new String [list.size()]);
	}

	public static String stripQuotes(String str) {
		return str.replace("\"", "");		
	}
	
	public static int indexOfCharacter(String line, int index) {
		line = line.toLowerCase();
		for(int i = index; i < line.length(); i++) {
			char c = line.charAt(i);
			switch(c) {
				case 'a' : 	case 'b' :  case 'c' : 	case 'd' : 	case 'e' : 
				case 'f' : 	case 'g' : 	case 'h' : 	case 'i' : 	case 'j' : 
				case 'k' : 	case 'l' : 	case 'm' : 	case 'n' : 	case 'o' : 
				case 'p' :  case 'q' : 	case 'r' : 	case 's' : 	case 't' : 
				case 'u' :  case 'v' : 	case 'w' : 	case 'x' : 	case 'y' : 
				case 'z' : return i; 
			}
		}
		return - 1;
	}	
	
	public static int indexOfWhitespace(String line, int index) {
		for(int i = index; i < line.length(); i++) {
			char c = line.charAt(i);
			switch(c) {
				case ' '  : 
				case '\t' : 
				case '\n' : 
				case '\r' : return i; 
			}
		}
		return - 1;
	}

	public static int newIndexOf(String line, char c, int currentIndex) {
		int charIndex = line.indexOf(c);
		if(currentIndex < 0) {
			return charIndex;
		}
		if(charIndex < currentIndex && charIndex > -1) { 
			return charIndex;			
		}
		return currentIndex;
	}

    public static String mergeWithNewLines(String[] str) {
        assert str != null : "no string array";
        
        int len = 0;
        
        for(int i = 0; i < str.length; i++) {
            assert str[i] != null : "no string array in array at index: " + i;
            len += str[i].length();
        }
        
        StringBuffer buf = new StringBuffer(len);
        
        for(int i = 0; i < str.length; i++) {
            buf.append(str[i]);
            buf.append(System.getProperty("line.separator"));
        }
        
        return buf.toString();

    }

	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}

	public static boolean isEmpty(String str) {
		return str == null || "".equals(str);
	}

	public static String[] split_banthar(String s, char delimeter) {
		 
		int count = 1;
	
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == delimeter)
				count++;
	
		String[] array = new String[count];
	
		int a = -1;
		int b = 0;
	
		for (int i = 0; i < count; i++) {
	
			while (b < s.length() && s.charAt(b) != delimeter)
				b++;
	
			array[i] = s.substring(a+1, b);
			a = b;
			b++;
			
		}
	
		return array;
	
	}

	public static int getLength(String[] strs) {
		int max = 0;
		for(int i = 0; i < strs.length; i++) {
			if(max < strs[i].length()) {
				max = strs[i].length();
			}
		}
		return max;
	}
	
}
