package gmocoin.autoFX.Collabo;

import java.util.*;

import org.json.JSONObject;

public interface ISession {
    /**
     * セッションIDを取得
     * 
     * @return
     */
    public String getSessionId();

    /**
     * セッションIDを設定
     * 
     * @param sessionId
     */
    public void setSessionId(String sessionId);

    /**
     * 変数を足す
     * 
     * @param key
     * @param val
     * @return
     */
    public Object setAttribute(Object key, Object val);

    /**
     * 変数を取得
     * 
     * @param key
     * @return
     */
    public Object getAttribute(Object key);

    /**
     * getリクエストを発送
     * 
     * @param url
     * @param ParamMap
     * @return
     */
    public StringBuffer sendGet(String url, Map<String, String> ParamMap);

    /**
     * postリクエストを発送
     * 
     * @param url
     * @param ParamMap
     * @return
     */
    public StringBuffer sendPost(String url, Map<String, String> ParamMap);
    public StringBuffer sendPost(String url, JSONObject ParamJson);
    /**
     * セッションタイムアウトを判断
     * 
     * @param html
     * @return
     */
    public boolean isTimeOut();

    /**
     * サービスを取得
     * 
     * @return
     */
    public IService getService();

    /**
     * ログアウト
     */
    public void invalidate();

    /**
     * 変数を削除
     * 
     * @param name
     */
    public void removeAttribute(String name);

    /**
     * ログイン状態を取得
     * 
     * @return
     */
    public int getLoginState();

    /**
     * ログイン成功を判断
     * 
     * @return
     */
    public boolean isLoginSuccess();
    /**
     * 是否需要验证码
     * @return
     */
    public boolean isWaitVeriCd();
    public void setVeriCd(String veriCd);
}
