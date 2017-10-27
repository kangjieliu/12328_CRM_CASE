package com.Test;

import org.apache.commons.lang3.StringUtils;

public class test {
	private String te(){
		return "11";
	}
	public static void main(String[] args) {
		String strNodeId = "01-01-012";
		String str = " ";
		
		System.out.println(StringUtils.substringAfter(strNodeId, "-"));
		String strPnode = strNodeId.substring(0, strNodeId.indexOf("-"));
		System.out.println(StringUtils.substringBefore(strNodeId,"-"));
		System.out.println(StringUtils.countMatches("eafefes", "e"));
		System.out.println(StringUtils.isNotBlank(str));
	}
}
