package br.univel.jshare.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidadorIp {

	private Pattern pattern;
    private Matcher matcher;
    
    private static final String IPADDRESS_PATTERN =
    		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
    		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
    		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
    		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public ValidadorIp(){
    	pattern = Pattern.compile(IPADDRESS_PATTERN);
    }
    
    public boolean validar(final String ip){
    	matcher = pattern.matcher(ip);
    	return matcher.matches();
    }

}
