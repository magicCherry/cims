package com.mi.cims.constant;

/**
 * ClassName:	PatternRegexp
 * Function:  	正则表达式
 *
 * @author    	孙忠飞
 * @date 	  	2020年12月21日 下午4:44:46
 * @version	  	V1.0.0
 */
public interface PatternRegexp {

	// 管理员用户登录ID
    public static final String loginId = "[A-Za-z0-9_]{1,32}";
    
	// 手机号
    public static final String phone = "[0-9]{11}";
    
	// 邮箱
    public static final String mail = "([a-zA-Z0-9_-])+(\\.[a-zA-Z0-9_-]+)*@([a-zA-Z0-9_-])+(\\.[a-zA-Z0-9_-]+)+";

}

