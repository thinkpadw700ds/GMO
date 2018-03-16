package gmocoin.autoFX.Collabo.abs;

import java.util.*;

import org.json.JSONObject;

import gmocoin.autoFX.Collabo.IHeadParams;
import gmocoin.autoFX.Collabo.IService;
import gmocoin.autoFX.Collabo.ISession;
import gmocoin.autoFX.Collabo.common.*;
import gmocoin.autoFX.Collabo.sp.*;

public abstract class AbsSession implements ISession {
    private String sessionId;
    private Map<Object, Object> attr;
    protected IService service;

    protected ReadHtml readHtml;
    protected String usercd;
    protected String pass;

    public static final int LOGIN_SUCCESS = 1;
    public static final int USER_INVALID = 2;
    public static final int REQ_ERROR = 3;
    public static final int WAIT_VERI_CD = 4;

    private int loginState;

    public AbsSession(String usercd, String pass, IHeadParams headParams) {

        this.attr = new HashMap<Object, Object>();
        this.usercd = usercd;
        this.pass = pass;
        this.readHtml = new ReadHtml(headParams);
        this.loginState = doLogin();
    }
    
    protected void reLogin(){
    	this.loginState = doLogin();
    }

    protected abstract int doLogin();

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Object setAttribute(Object key, Object val) {
        return attr.put(key, val);
    }

    public Object getAttribute(Object key) {
        return attr.get(key);
    }

    public void removeAttribute(String name) {
        attr.remove(name);
    }

    public int getLoginState() {
        return loginState;
    }

    public StringBuffer sendGet(String url, Map<String, String> ParamMap) {

        StringBuffer html = new StringBuffer();
        Map<String, List<String>> header = readHtml.sendGet(url, ParamMap, sessionId, html);
        if (isTimeOut()) {
            this.loginState = doLogin();
            html = new StringBuffer();
            header = readHtml.sendPost(url, ParamMap, sessionId, html);
        }
        return html;
    }

    public StringBuffer sendPost(String url, Map<String, String> ParamMap) {
        StringBuffer html = new StringBuffer();
        Map<String, List<String>> header = readHtml.sendPost(url, ParamMap, sessionId, html);
        if (isTimeOut()) {
            this.loginState = doLogin();
            html = new StringBuffer();
            header = readHtml.sendPost(url, ParamMap, sessionId, html);
        }
        return html;
    }

	@Override
	public StringBuffer sendPost(String url, JSONObject ParamJson) {
		StringBuffer html = new StringBuffer();
        Map<String, List<String>> header = readHtml.sendPost(url, ParamJson, sessionId, html);
        if (isTimeOut()) {
            this.loginState = doLogin();
            html = new StringBuffer();
            header = readHtml.sendPost(url, ParamJson, sessionId, html);
        }
        return html;
	}

    public IService getService() {
        return service;
    }

    public boolean isLoginSuccess() {
        return this.loginState == LOGIN_SUCCESS;
    }

    public boolean isWaitVeriCd() {
    	return this.loginState == WAIT_VERI_CD;
    }
}
