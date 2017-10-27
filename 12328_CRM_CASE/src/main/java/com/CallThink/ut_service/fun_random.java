package com.CallThink.ut_service;

import java.util.Random;

public class fun_random {

	/**
	 * 获取指定范围的随机数
	 * @param min  最小数
	 * @param max 最大数
	 * @return
	 */
	public static int getRandomNum(int min, int max) {
		// int max=9999;//int min=1000;
		if(max<min) return 0;
		Random random = new Random();
		return random.nextInt(max) % (max - min + 1) + min;
	}

	public static String getRandomValue(int min, int max) {
		return Integer.toString(getRandomNum(min,max));
	}

	/**
	 * 公司经常会用到获取四位随机数的情况,所以直接封装函数来处理
	 * @return
	 */
	public static String getPadLeft() {
		return getRandomValue(1000, 9999);
	}

}
